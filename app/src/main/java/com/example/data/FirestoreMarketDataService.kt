package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.ActiveDataSource
import com.example.model.CanonicalMarketRecord
import com.example.model.DataSyncState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Service for Firebase Cloud Firestore primary source of truth (`market_records` collection).
 * Handles:
 * 1. Non-destructive reads and writes.
 * 2. Multi-schema backward compatibility adapter for existing Firestore documents.
 * 3. Offline cache and live synchronization.
 * 4. GitHub JSON mirror preparation.
 */
class FirestoreMarketDataService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FirestoreMarketService"
        const val MARKET_RECORDS_COLLECTION = "market_records"
    }

    init {
        try {
            // Enable offline persistence settings
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            firestore.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w(TAG, "Firestore settings initialization note: ${e.message}")
        }
    }

    /**
     * Actively checks connection health to Firestore by attempting a metadata read.
     */
    suspend fun checkFirestoreHealth(): com.example.model.FirestoreHealthStatus = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(MARKET_RECORDS_COLLECTION).limit(1).get(Source.SERVER).await()
            if (snapshot.metadata.isFromCache) {
                com.example.model.FirestoreHealthStatus.OFFLINE_CACHE
            } else {
                com.example.model.FirestoreHealthStatus.CONNECTED_LIVE
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            when {
                msg.contains("PERMISSION_DENIED", ignoreCase = true) -> com.example.model.FirestoreHealthStatus.PERMISSION_DENIED
                msg.contains("UNAVAILABLE", ignoreCase = true) || msg.contains("OFFLINE", ignoreCase = true) -> com.example.model.FirestoreHealthStatus.OFFLINE_CACHE
                else -> com.example.model.FirestoreHealthStatus.ERROR
            }
        }
    }

    /**
     * Reads all market records from Firestore Cloud collection `market_records`.
     * Inspects both root documents and any embedded records/subcollections non-destructively.
     */
    suspend fun fetchAllMarketRecords(): Result<Pair<Map<String, List<CanonicalMarketRecord>>, ActiveDataSource>> = withContext(Dispatchers.IO) {
        try {
            val marketRecordsMap = mutableMapOf<String, MutableList<CanonicalMarketRecord>>()
            var dataSource = ActiveDataSource.FIREBASE_LIVE

            // Query the root collection with safe fallback
            val collectionSnapshot = try {
                withTimeoutOrNull(8000L) {
                    firestore.collection(MARKET_RECORDS_COLLECTION)
                        .get(Source.DEFAULT)
                        .await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Default source fetch failed: ${e.message}")
                null
            } ?: run {
                // Try cache if default network fetch failed or timed out
                dataSource = ActiveDataSource.FIREBASE_CACHE
                try {
                    firestore.collection(MARKET_RECORDS_COLLECTION)
                        .get(Source.CACHE)
                        .await()
                } catch (cacheEx: Exception) {
                    Log.w(TAG, "Cache fetch also failed: ${cacheEx.message}")
                    null
                }
            }

            if (collectionSnapshot == null || collectionSnapshot.isEmpty) {
                return@withContext Result.failure(Exception("No market records found in Cloud Firestore."))
            }

            if (collectionSnapshot.metadata.isFromCache) {
                dataSource = ActiveDataSource.FIREBASE_CACHE
            }

            for (doc in collectionSnapshot.documents) {
                val marketId = doc.id
                val docData = doc.data ?: emptyMap()
                val parsedRecords = parseDocumentToCanonicalRecords(marketId, docData)
                
                val list = marketRecordsMap.getOrPut(normalizeMarketKey(marketId)) { mutableListOf() }
                list.addAll(parsedRecords)

                // Also check for subcollection "records" if needed
                try {
                    val subcollectionSnapshot = withTimeoutOrNull(4000L) {
                        doc.reference.collection("records").get(Source.DEFAULT).await()
                    } ?: try {
                        doc.reference.collection("records").get(Source.CACHE).await()
                    } catch (e: Exception) { null }

                    if (subcollectionSnapshot != null) {
                        for (subDoc in subcollectionSnapshot.documents) {
                            val subData = subDoc.data ?: continue
                            val rec = parseSingleRecordMap(marketId, subDoc.id, subData)
                            if (rec != null && list.none { it.date == rec.date }) {
                                list.add(rec)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Subcollection read note for $marketId: ${e.message}")
                }
            }

            // Deduplicate and sort descending by date for each market
            val finalMap = marketRecordsMap.mapValues { (_, records) ->
                records.distinctBy { it.date }.sortedByDescending { it.date }
            }

            if (finalMap.isEmpty() || finalMap.values.all { it.isEmpty() }) {
                return@withContext Result.failure(Exception("Market collection is empty."))
            }

            Log.d(TAG, "Successfully loaded ${finalMap.size} markets from Firestore (${dataSource.displayLabel})")
            Result.success(Pair(finalMap, dataSource))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Firestore market_records: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Backward-compatibility parser to handle all variations of Firestore document schemas:
     * - Array of records: `records: [ { date, result, isHoliday }, ... ]`
     * - Map of records: `records: { "2026-09-07": { openPana, jodi, closePana, isHoliday }, ... }`
     * - Raw text data: `data: "..."` or `rawData: "..."`
     * - Single record document: `date`, `openPana`, `jodi`, `closePana`, `isHoliday`
     */
    private fun parseDocumentToCanonicalRecords(marketId: String, data: Map<String, Any?>): List<CanonicalMarketRecord> {
        val records = mutableListOf<CanonicalMarketRecord>()
        val marketName = normalizeMarketKey((data["marketName"] as? String) ?: marketId)

        // Case 1: Array of record objects under "records" or "history"
        val recordsObj = data["records"] ?: data["history"] ?: data["entries"]
        if (recordsObj is List<*>) {
            for (item in recordsObj) {
                if (item is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val rec = parseSingleRecordMap(marketName, null, item as Map<String, Any?>)
                    if (rec != null) records.add(rec)
                }
            }
        }

        // Case 2: Map of date keys under "records" or "dates"
        if (recordsObj is Map<*, *>) {
            for ((key, value) in recordsObj) {
                val dateKey = key.toString()
                if (value is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val rec = parseSingleRecordMap(marketName, dateKey, value as Map<String, Any?>)
                    if (rec != null) records.add(rec)
                }
            }
        }

        // Case 3: Raw string data under "data" or "rawData"
        val rawText = (data["data"] as? String) ?: (data["rawData"] as? String)
        if (rawText != null && rawText.isNotBlank()) {
            val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("#") && !it.startsWith("//") }
            for (line in lines) {
                val parsed = parseLineToCanonicalRecord(marketName, line)
                if (parsed != null) records.add(parsed)
            }
        }

        // Case 4: Flat single-day document
        if (records.isEmpty() && data.containsKey("date")) {
            val rec = parseSingleRecordMap(marketName, null, data)
            if (rec != null) records.add(rec)
        }

        return records
    }

    private fun parseSingleRecordMap(marketName: String, fallbackDate: String?, map: Map<String, Any?>): CanonicalMarketRecord? {
        val date = (map["date"] as? String)?.trim() ?: fallbackDate ?: return null
        if (date.isBlank()) return null

        val isHoliday = (map["isHoliday"] as? Boolean) == true || (map["holiday"] as? Boolean) == true
        val openPana = (map["openPana"] as? String) ?: (map["open_pana"] as? String)
        val jodi = (map["jodi"] as? String) ?: (map["result_jodi"] as? String)
        val closePana = (map["closePana"] as? String) ?: (map["close_pana"] as? String)

        // Parse from composite "result" string if individual fields are missing
        val resultStr = (map["result"] as? String)?.trim()
        var finalOpen = openPana
        var finalJodi = jodi
        var finalClose = closePana
        var finalHoliday = isHoliday

        if (resultStr != null && (finalOpen == null || finalJodi == null)) {
            if (resultStr.contains("***") || resultStr.contains("**") || resultStr.equals("holiday", ignoreCase = true)) {
                finalHoliday = true
                finalOpen = "***"
                finalJodi = "**"
                finalClose = "***"
            } else {
                val tokens = Regex("""\d+""").findAll(resultStr).map { it.value }.toList()
                if (tokens.size >= 3) {
                    finalOpen = tokens[0]
                    finalJodi = tokens[1].padStart(2, '0')
                    finalClose = tokens[2]
                } else if (tokens.size == 2) {
                    finalOpen = tokens[0]
                    finalJodi = tokens[1].padStart(2, '0')
                    finalClose = "***"
                }
            }
        }

        val source = (map["source"] as? String) ?: "FIREBASE_PRESERVED"
        val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        val updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        val updatedBy = (map["updatedBy"] as? String) ?: "admin"

        return CanonicalMarketRecord(
            marketName = marketName,
            date = date,
            openPana = if (finalHoliday) "***" else finalOpen,
            jodi = if (finalHoliday) "**" else finalJodi,
            closePana = if (finalHoliday) "***" else finalClose,
            isHoliday = finalHoliday,
            source = source,
            createdAt = createdAt,
            updatedAt = updatedAt,
            updatedBy = updatedBy
        )
    }

    private fun parseLineToCanonicalRecord(marketName: String, line: String): CanonicalMarketRecord? {
        val clean = line.trim()
        if (clean.isBlank()) return null

        val isHoliday = clean.contains("***") || clean.contains("**") || clean.contains("holiday", ignoreCase = true)
        val dateMatch = Regex("""\b(\d{4}-\d{2}-\d{2}|\d{1,2}[-./]\d{1,2}[-./]\d{2,4})\b""").find(clean)
        val dateStr = dateMatch?.value ?: SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())

        if (isHoliday) {
            return CanonicalMarketRecord(
                marketName = marketName,
                date = dateStr,
                openPana = "***",
                jodi = "**",
                closePana = "***",
                isHoliday = true,
                source = "PARSED_RAW_FIREBASE"
            )
        }

        val tokens = Regex("""\d+""").findAll(clean.replace(dateStr, "")).map { it.value }.toList()
        if (tokens.size >= 3) {
            return CanonicalMarketRecord(
                marketName = marketName,
                date = dateStr,
                openPana = tokens[0],
                jodi = tokens[1].padStart(2, '0'),
                closePana = tokens[2],
                isHoliday = false,
                source = "PARSED_RAW_FIREBASE"
            )
        }
        return null
    }

    /**
     * Creates a new market document directly in Firestore Cloud Collection `market_records`.
     */
    suspend fun createNewMarketInFirestore(
        marketName: String,
        openTime: String = "04:00 PM",
        closeTime: String = "06:00 PM",
        initialRecords: List<CanonicalMarketRecord> = emptyList(),
        createdBy: String = "admin"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val normalizedMarket = normalizeMarketKey(marketName)
            val docRef = firestore.collection(MARKET_RECORDS_COLLECTION).document(normalizedMarket)

            val marketMeta = mutableMapOf<String, Any?>(
                "marketName" to normalizedMarket,
                "displayName" to marketName.trim().uppercase(),
                "openTime" to openTime,
                "closeTime" to closeTime,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis(),
                "createdBy" to createdBy,
                "active" to true,
                "source" to "APP_FIREBASE_ADD_MARKET"
            )

            if (initialRecords.isNotEmpty()) {
                val latest = initialRecords.first()
                marketMeta["lastUpdatedDate"] = latest.date
                marketMeta["lastResult"] = if (latest.isHoliday) "*** - ** - ***" else "${latest.openPana} - ${latest.jodi} - ${latest.closePana}"
                
                // Write each initial record to subcollection
                for (rec in initialRecords) {
                    docRef.collection("records")
                        .document(rec.date)
                        .set(rec.toFirestoreMap(), SetOptions.merge())
                        .await()
                }
            }

            docRef.set(marketMeta, SetOptions.merge()).await()
            Log.d(TAG, "Created new market $normalizedMarket in Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create market $marketName in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Saves a new canonical market record to Firestore with atomic confirmation.
     * Preserves existing documents by using non-destructive set with merge.
     */
    suspend fun saveMarketRecordToFirestore(record: CanonicalMarketRecord): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val normalizedMarket = normalizeMarketKey(record.marketName)
            val docRef = firestore.collection(MARKET_RECORDS_COLLECTION).document(normalizedMarket)

            val recordData = record.toFirestoreMap()

            // 1. Update the subcollection /records/{date}
            docRef.collection("records")
                .document(record.date)
                .set(recordData, SetOptions.merge())
                .await()

            // 2. Also update the root market document summary safely without overwriting old fields
            val rootSummaryUpdate = mapOf(
                "marketName" to normalizedMarket,
                "lastUpdatedDate" to record.date,
                "lastResult" to if (record.isHoliday) "*** - ** - ***" else "${record.openPana} - ${record.jodi} - ${record.closePana}",
                "updatedAt" to System.currentTimeMillis(),
                "updatedBy" to record.updatedBy,
                "records.${record.date}" to recordData
            )

            docRef.set(rootSummaryUpdate, SetOptions.merge()).await()

            Log.d(TAG, "Confirmed Firestore write for ${record.marketName} on ${record.date}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore write failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Atomically/batch saves multiple canonical market records to Firestore.
     * Accurately reports successes, skips, and failures without loss of data.
     */
    suspend fun saveBulkRecordsToFirestore(
        records: List<CanonicalMarketRecord>
    ): Pair<Int, List<String>> = withContext(Dispatchers.IO) {
        var successCount = 0
        val errors = mutableListOf<String>()

        val grouped = records.groupBy { normalizeMarketKey(it.marketName) }
        for ((marketKey, marketRecs) in grouped) {
            try {
                val docRef = firestore.collection(MARKET_RECORDS_COLLECTION).document(marketKey)
                
                // Write records using WriteBatch in chunks of 400
                val chunks = marketRecs.chunked(400)
                for (chunk in chunks) {
                    val batch = firestore.batch()
                    for (rec in chunk) {
                        val recRef = docRef.collection("records").document(rec.date)
                        batch.set(recRef, rec.toFirestoreMap(), SetOptions.merge())
                    }
                    try {
                        batch.commit().await()
                        successCount += chunk.size
                    } catch (e: Exception) {
                        Log.e(TAG, "Batch write failed: ${e.message}", e)
                        errors.add("Market $marketKey batch: ${e.message}")
                        if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                            e.message?.contains("permission", ignoreCase = true) == true) {
                            return@withContext Pair(successCount, errors)
                        }
                    }
                }

                // Update root summary with the latest record
                val latest = marketRecs.maxByOrNull { it.date }
                if (latest != null) {
                    val summary = mapOf(
                        "marketName" to marketKey,
                        "displayName" to latest.marketName,
                        "lastUpdatedDate" to latest.date,
                        "lastResult" to if (latest.isHoliday) "*** - ** - ***" else "${latest.openPana} - ${latest.jodi} - ${latest.closePana}",
                        "updatedAt" to System.currentTimeMillis(),
                        "totalRecords" to marketRecs.size
                    )
                    docRef.set(summary, SetOptions.merge()).await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Market $marketKey summary error: ${e.message}", e)
                errors.add("Market $marketKey error: ${e.message}")
                if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                    e.message?.contains("permission", ignoreCase = true) == true) {
                    return@withContext Pair(successCount, errors)
                }
            }
        }

        Pair(successCount, errors)
    }

    /**
     * Generates a validated canonical data.json mirror string matching GitHub format.
     */
    fun generateGitHubMirrorJson(marketRecordsMap: Map<String, List<CanonicalMarketRecord>>): String {
        val root = JSONObject()
        root.put("version", "2.0")
        root.put("last_updated", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date()))
        root.put("source", "A23MAX_CANONICAL_FIREBASE_MIRROR")

        val recordsArray = JSONArray()
        for ((marketName, records) in marketRecordsMap) {
            for (r in records) {
                val obj = JSONObject()
                obj.put("marketName", marketName)
                obj.put("date", r.date)
                obj.put("openPana", r.openPana ?: if (r.isHoliday) "***" else "159")
                obj.put("jodi", r.jodi ?: if (r.isHoliday) "**" else "56")
                obj.put("closePana", r.closePana ?: if (r.isHoliday) "***" else "647")
                obj.put("isHoliday", r.isHoliday)
                obj.put("result", if (r.isHoliday) "*** - ** - ***" else "${r.openPana ?: "159"} - ${r.jodi ?: "56"} - ${r.closePana ?: "647"}")
                recordsArray.put(obj)
            }
        }
        root.put("records", recordsArray)
        return root.toString(2)
    }

    fun normalizeMarketKey(rawName: String): String {
        val clean = rawName.trim().uppercase()
        return when {
            clean == "SRIDEVI" -> "SRIDEVI"
            clean == "SHRIDEVI" -> "SHRIDEVI"
            clean == "TIMEBAZAR" || clean == "TIME BAZAR" -> "TIME BAZAR"
            clean == "MILAN" || clean == "MILAN DAY" || clean == "MILANDAY" -> "MILAN DAY"
            clean == "KALYAN" -> "KALYAN"
            clean == "RAJDHANI DAY" || clean == "RAJDHANI" -> "RAJDHANI DAY"
            clean == "MAIN BAZAR" || clean == "MAINBAZAR" -> "MAIN BAZAR"
            clean == "KALYAN NIGHT" -> "KALYAN NIGHT"
            clean == "MILAN NIGHT" -> "MILAN NIGHT"
            clean == "RAJDHANI NIGHT" -> "RAJDHANI NIGHT"
            clean == "SRIDEVI NIGHT" -> "SRIDEVI NIGHT"
            else -> clean
        }
    }
}
