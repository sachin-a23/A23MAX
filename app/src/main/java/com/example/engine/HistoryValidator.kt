package com.example.engine

import com.example.model.CanonicalHistoryEntry
import com.example.model.DataQualityReport
import com.example.model.DatasetFingerprinter
import com.example.model.MarketHistoryEntry
import com.example.util.DateUtils
import java.util.Calendar
import java.util.Locale

object HistoryValidator {

    /**
     * Sanitizes, deduplicates, validates, and sorts historical market entries chronologically.
     */
    fun buildCanonicalDataset(
        marketName: String,
        rawEntries: List<MarketHistoryEntry>
    ): Pair<List<CanonicalHistoryEntry>, DataQualityReport> {
        val normalizedMarket = marketName.trim().uppercase()
        var duplicateCount = 0
        var invalidCount = 0
        var holidayCount = 0
        var missingCount = 0
        var pendingCount = 0

        val seenDates = mutableSetOf<String>()
        val validatedList = mutableListOf<CanonicalHistoryEntry>()

        for (raw in rawEntries) {
            val dateStr = sanitizeDate(raw.date)
            if (dateStr == null) {
                invalidCount++
                continue
            }

            // Deduplication: if exact date already recorded, skip duplicate and record in count
            if (seenDates.contains(dateStr)) {
                duplicateCount++
                continue
            }
            seenDates.add(dateStr)

            val cal = DateUtils.parseDateToCalendar(dateStr)
            val timeMillis = cal?.timeInMillis ?: 0L
            val dayOfWeek = raw.dayOfWeek.ifBlank { DateUtils.getDayOfWeek(dateStr) }

            val rawOpen = raw.resultPanaOpen?.trim() ?: ""
            val rawJodi = raw.resultJodi?.trim() ?: ""
            val rawClose = raw.resultPanaClose?.trim() ?: ""

            val isHolidayByStars = rawOpen.contains("*") || rawJodi.contains("*") || rawClose.contains("*")
            val isExplicitHoliday = raw.isHoliday || isHolidayByStars

            if (isExplicitHoliday) {
                holidayCount++
                validatedList.add(
                    CanonicalHistoryEntry(
                        canonicalId = "${normalizedMarket}_$dateStr",
                        marketName = normalizedMarket,
                        date = dateStr,
                        timestampMillis = timeMillis,
                        dayOfWeek = dayOfWeek,
                        openPana = null,
                        jodi = null,
                        closePana = null,
                        openAnk = null,
                        closeAnk = null,
                        jodiOpenAnk = null,
                        jodiCloseAnk = null,
                        isHoliday = true,
                        isPending = false,
                        isMissing = false,
                        isInvalid = false,
                        validationNote = "Official Holiday / Market Closed"
                    )
                )
                continue
            }

            // Detect Pending
            if (raw.isPending || rawOpen.equals("PENDING", ignoreCase = true) || rawJodi.equals("PENDING", ignoreCase = true)) {
                pendingCount++
                validatedList.add(
                    CanonicalHistoryEntry(
                        canonicalId = "${normalizedMarket}_$dateStr",
                        marketName = normalizedMarket,
                        date = dateStr,
                        timestampMillis = timeMillis,
                        dayOfWeek = dayOfWeek,
                        openPana = null,
                        jodi = null,
                        closePana = null,
                        openAnk = null,
                        closeAnk = null,
                        jodiOpenAnk = null,
                        jodiCloseAnk = null,
                        isHoliday = false,
                        isPending = true,
                        isMissing = false,
                        isInvalid = false,
                        validationNote = "Draw Pending"
                    )
                )
                continue
            }

            // Detect Missing Data
            if (rawOpen.isBlank() && rawJodi.isBlank() && rawClose.isBlank()) {
                missingCount++
                validatedList.add(
                    CanonicalHistoryEntry(
                        canonicalId = "${normalizedMarket}_$dateStr",
                        marketName = normalizedMarket,
                        date = dateStr,
                        timestampMillis = timeMillis,
                        dayOfWeek = dayOfWeek,
                        openPana = null,
                        jodi = null,
                        closePana = null,
                        openAnk = null,
                        closeAnk = null,
                        jodiOpenAnk = null,
                        jodiCloseAnk = null,
                        isHoliday = false,
                        isPending = false,
                        isMissing = true,
                        isInvalid = false,
                        validationNote = "Missing result entries"
                    )
                )
                continue
            }

            // Validate Open Pana (must be 3 digits)
            val cleanOpen = rawOpen.filter { it.isDigit() }
            val validOpen = if (cleanOpen.length == 3) cleanOpen else null

            // Validate Jodi (must be 2 digits)
            val cleanJodi = rawJodi.filter { it.isDigit() }
            val validJodi = if (cleanJodi.length == 2) cleanJodi else null

            // Validate Close Pana (must be 3 digits)
            val cleanClose = rawClose.filter { it.isDigit() }
            val validClose = if (cleanClose.length == 3) cleanClose else null

            val isDataInvalid = (validOpen == null && validJodi == null)

            if (isDataInvalid) {
                invalidCount++
                validatedList.add(
                    CanonicalHistoryEntry(
                        canonicalId = "${normalizedMarket}_$dateStr",
                        marketName = normalizedMarket,
                        date = dateStr,
                        timestampMillis = timeMillis,
                        dayOfWeek = dayOfWeek,
                        openPana = null,
                        jodi = null,
                        closePana = null,
                        openAnk = null,
                        closeAnk = null,
                        jodiOpenAnk = null,
                        jodiCloseAnk = null,
                        isHoliday = false,
                        isPending = false,
                        isMissing = false,
                        isInvalid = true,
                        validationNote = "Invalid number format (Open: '$rawOpen', Jodi: '$rawJodi')"
                    )
                )
                continue
            }

            val openAnk = validOpen?.sumOf { it.digitToIntOrNull() ?: 0 }?.rem(10)
            val closeAnk = validClose?.sumOf { it.digitToIntOrNull() ?: 0 }?.rem(10)
            val jodiOpen = validJodi?.getOrNull(0)?.digitToIntOrNull()
            val jodiClose = validJodi?.getOrNull(1)?.digitToIntOrNull()

            validatedList.add(
                CanonicalHistoryEntry(
                    canonicalId = "${normalizedMarket}_$dateStr",
                    marketName = normalizedMarket,
                    date = dateStr,
                    timestampMillis = timeMillis,
                    dayOfWeek = dayOfWeek,
                    openPana = validOpen,
                    jodi = validJodi,
                    closePana = validClose,
                    openAnk = openAnk,
                    closeAnk = closeAnk,
                    jodiOpenAnk = jodiOpen,
                    jodiCloseAnk = jodiClose,
                    isHoliday = false,
                    isPending = false,
                    isMissing = false,
                    isInvalid = false,
                    validationNote = "Valid Canonical Entry"
                )
            )
        }

        // Strictly sort chronologically ASCENDING
        val sortedAscending = validatedList.sortedWith(
            compareBy<CanonicalHistoryEntry> { it.timestampMillis }
                .thenBy { it.date }
        )

        val eligibleCount = sortedAscending.count { it.isEligibleForResearch }
        val earliest = sortedAscending.firstOrNull()?.date
        val latest = sortedAscending.lastOrNull()?.date
        val fingerprint = DatasetFingerprinter.computeFingerprint(sortedAscending)

        val report = DataQualityReport(
            marketName = normalizedMarket,
            totalRawRecords = rawEntries.size,
            eligibleRecords = eligibleCount,
            duplicateRecords = duplicateCount,
            invalidRecords = invalidCount,
            holidayRecords = holidayCount,
            missingRecords = missingCount,
            pendingRecords = pendingCount,
            earliestDate = earliest,
            latestDate = latest,
            datasetFingerprint = fingerprint
        )

        return Pair(sortedAscending, report)
    }

    private fun sanitizeDate(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null
        val normalized = trimmed.replace("/", "-").replace(".", "-")
        val parts = normalized.split("-")
        if (parts.size != 3) return null

        val p0 = parts[0].toIntOrNull() ?: return null
        val p1 = parts[1].toIntOrNull() ?: return null
        val p2 = parts[2].toIntOrNull() ?: return null

        // Standardize to dd-MM-yyyy
        return if (p0 > 31 && p2 <= 31) {
            // yyyy-MM-dd -> dd-MM-yyyy
            String.format(Locale.ENGLISH, "%02d-%02d-%04d", p2, p1, p0)
        } else {
            // dd-MM-yyyy
            String.format(Locale.ENGLISH, "%02d-%02d-%04d", p0, p1, if (p2 < 100) 2000 + p2 else p2)
        }
    }
}
