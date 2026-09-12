package com.example.model

/**
 * Result status for record parsing and validation.
 */
enum class RecordValidationStatus {
    VALID,
    HOLIDAY,
    DUPLICATE,
    INVALID
}

/**
 * Conflict resolution strategy when importing duplicate/existing dates.
 */
enum class DuplicateConflictResolution {
    SKIP_SAFE,      // Default safe: do not overwrite existing records
    OVERWRITE       // Explicitly update existing records
}

/**
 * Parsed record ready for validation and batch processing.
 */
data class ParsedEntryRecord(
    val rawLine: String,
    val marketName: String,
    val date: String,             // Standardized YYYY-MM-DD
    val openPana: String?,        // e.g. "123" or "***"
    val jodi: String?,            // e.g. "45" or "**"
    val closePana: String?,       // e.g. "678" or "***"
    val isHoliday: Boolean,
    val status: RecordValidationStatus,
    val validationError: String? = null,
    val isMarketNew: Boolean = false
) {
    fun toCanonical(source: String = "DATA_ENTRY", updatedBy: String = "admin"): CanonicalMarketRecord {
        return CanonicalMarketRecord(
            marketName = marketName,
            date = date,
            openPana = if (isHoliday) "***" else openPana,
            jodi = if (isHoliday) "**" else jodi,
            closePana = if (isHoliday) "***" else closePana,
            isHoliday = isHoliday,
            source = source,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            updatedBy = updatedBy
        )
    }

    val displayResult: String
        get() = if (isHoliday) "*** - ** - ***" else "${openPana ?: "***"} - ${jodi ?: "**"} - ${closePana ?: "***"}"
}

/**
 * Preview summary before committing bulk save to Firestore & Local repository.
 */
data class BulkValidationPreview(
    val totalCount: Int = 0,
    val validCount: Int = 0,
    val invalidCount: Int = 0,
    val duplicateCount: Int = 0,
    val holidayCount: Int = 0,
    val newMarketsDetected: List<String> = emptyList(),
    val parsedRecords: List<ParsedEntryRecord> = emptyList(),
    val hasMultipleMarkets: Boolean = false
)

/**
 * Final execution report after writing bulk records to Firestore.
 */
data class BulkSaveReport(
    val totalProcessed: Int,
    val successfullySaved: Int,
    val failedCount: Int,
    val skippedDuplicates: Int,
    val holidayCount: Int,
    val createdMarkets: List<String>,
    val failures: List<BulkFailureDetail> = emptyList(),
    val isCompleteSuccess: Boolean = failedCount == 0,
    val timestamp: String = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.ENGLISH).format(java.util.Date())
)

data class BulkFailureDetail(
    val marketName: String,
    val date: String,
    val reason: String
)

/**
 * Global live process status for long-running Firebase/Data operations.
 */
enum class OperationStatus {
    IDLE,
    LOADING,
    VALIDATING,
    WRITING,
    READING_VERIFICATION,
    SUCCESS,
    PARTIAL_SUCCESS,
    FAILED,
    PERMISSION_DENIED,
    OFFLINE,
    UNAVAILABLE
}

data class OperationProcessState(
    val title: String = "",
    val message: String = "",
    val status: OperationStatus = OperationStatus.IDLE,
    val currentProgress: Int = 0,
    val totalProgress: Int = 0,
    val isIndeterminate: Boolean = true,
    val errorMessage: String? = null,
    val isCancellable: Boolean = false,
    val details: List<String> = emptyList(),
    val isVisible: Boolean = status != OperationStatus.IDLE
)
