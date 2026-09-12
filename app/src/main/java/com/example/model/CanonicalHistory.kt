package com.example.model

import java.security.MessageDigest
import java.util.Locale

/**
 * Data Quality Audit Report for a market's historical dataset.
 */
data class DataQualityReport(
    val marketName: String,
    val totalRawRecords: Int,
    val eligibleRecords: Int,
    val duplicateRecords: Int,
    val invalidRecords: Int,
    val holidayRecords: Int,
    val missingRecords: Int,
    val pendingRecords: Int,
    val earliestDate: String?,
    val latestDate: String?,
    val datasetFingerprint: String
)

/**
 * Validated, canonical entry for a single market draw.
 * Guaranteed to have valid format, normalized date, and strict data-type safety.
 */
data class CanonicalHistoryEntry(
    val canonicalId: String,
    val marketName: String,
    val date: String,            // dd-MM-yyyy format
    val timestampMillis: Long,
    val dayOfWeek: String,       // MON, TUE, WED, THU, FRI, SAT, SUN
    val openPana: String?,       // 3 digits e.g. "159", or null if holiday/missing
    val jodi: String?,           // 2 digits e.g. "56", or null if holiday/missing
    val closePana: String?,      // 3 digits e.g. "789", or null if holiday/missing
    val openAnk: Int?,           // sum of openPana % 10 (0..9)
    val closeAnk: Int?,          // sum of closePana % 10 (0..9)
    val jodiOpenAnk: Int?,       // first digit of jodi (0..9)
    val jodiCloseAnk: Int?,      // second digit of jodi (0..9)
    val isHoliday: Boolean,
    val isPending: Boolean,
    val isMissing: Boolean,
    val isInvalid: Boolean,
    val validationNote: String
) {
    val isEligibleForResearch: Boolean
        get() = !isHoliday && !isPending && !isMissing && !isInvalid && openPana != null && jodi != null
}

/**
 * Helper to compute SHA-256 fingerprint for dataset reproducibility.
 */
object DatasetFingerprinter {
    fun computeFingerprint(entries: List<CanonicalHistoryEntry>): String {
        if (entries.isEmpty()) return "empty_dataset"
        val sb = StringBuilder()
        for (e in entries) {
            sb.append("${e.marketName}|${e.date}|${e.openPana}|${e.jodi}|${e.closePana}|${e.isHoliday};")
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(sb.toString().toByteArray(Charsets.UTF_8))
        return bytes.take(8).joinToString("") { "%02x".format(Locale.ENGLISH, it) }
    }
}
