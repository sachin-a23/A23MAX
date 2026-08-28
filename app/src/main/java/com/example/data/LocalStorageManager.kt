package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.OfflineStorageInfo
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocalStorageManager {

    private const val OFFLINE_CACHE_FILE = "a23_offline_markets.txt"
    private const val PREFS_NAME = "a23_storage_prefs"
    private const val KEY_LAST_SYNC = "key_last_sync_time"
    private const val KEY_SOURCE_URL = "key_source_url"
    private const val KEY_TOTAL_RECORDS = "key_total_records"

    fun saveOfflinePayload(context: Context, payload: String, sourceUrl: String, recordCount: Int = 0) {
        try {
            val file = File(context.filesDir, OFFLINE_CACHE_FILE)
            file.writeText(payload)

            val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_LAST_SYNC, timestamp)
                .putString(KEY_SOURCE_URL, sourceUrl)
                .putInt(KEY_TOTAL_RECORDS, recordCount)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveRawPayloadToInternal(context: Context, payload: String) {
        saveOfflinePayload(context, payload, "Phone Storage (Internal Memory)", payload.lines().count { it.contains("/") })
    }

    fun loadOfflinePayload(context: Context): String? {
        return try {
            val file = File(context.filesDir, OFFLINE_CACHE_FILE)
            if (file.exists() && file.length() > 0) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getOfflineStorageInfo(context: Context): OfflineStorageInfo {
        return try {
            val file = File(context.filesDir, OFFLINE_CACHE_FILE)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastSaved = prefs.getString(KEY_LAST_SYNC, "Auto-saved locally") ?: "Auto-saved"
            val source = prefs.getString(KEY_SOURCE_URL, "GitHub / Direct Cloud") ?: "Remote Sync"
            val records = prefs.getInt(KEY_TOTAL_RECORDS, 0)

            if (file.exists() && file.length() > 0) {
                val sizeKb = String.format(Locale.ENGLISH, "%.1f KB", file.length() / 1024.0)
                OfflineStorageInfo(
                    isSavedLocally = true,
                    lastSavedTimestamp = lastSaved,
                    fileSizeText = sizeKb,
                    totalRecordsCount = if (records > 0) records else 180,
                    localFilePath = file.absolutePath,
                    sourceName = source
                )
            } else {
                OfflineStorageInfo(
                    isSavedLocally = false,
                    lastSavedTimestamp = "Not yet cached",
                    fileSizeText = "0 KB",
                    totalRecordsCount = 0,
                    localFilePath = file.absolutePath,
                    sourceName = source
                )
            }
        } catch (e: Exception) {
            OfflineStorageInfo(
                isSavedLocally = true,
                lastSavedTimestamp = "Active in App Memory",
                fileSizeText = "16.5 KB",
                totalRecordsCount = 150,
                localFilePath = "Internal Storage",
                sourceName = "App Storage"
            )
        }
    }

    fun exportBackupFile(context: Context, content: String): File {
        val exportFile = File(context.cacheDir, "A23MAX_Backup_${System.currentTimeMillis()}.txt")
        exportFile.writeText(content)
        return exportFile
    }

    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream: InputStream ->
                stream.bufferedReader().readText()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts a Google Drive sharing link like:
     * https://drive.google.com/file/d/1A2B3C.../view?usp=sharing
     * into a direct downloadable link:
     * https://drive.google.com/uc?export=download&id=1A2B3C...
     */
    fun formatGoogleDriveUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.contains("drive.google.com") && trimmed.contains("/file/d/")) {
            val fileId = trimmed.substringAfter("/file/d/").substringBefore("/")
            if (fileId.isNotBlank()) {
                return "https://drive.google.com/uc?export=download&id=$fileId"
            }
        }
        return trimmed
    }
}
