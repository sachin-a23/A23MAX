package com.example.model

/**
 * Canonical data structure for a single market record in A23MAX.
 * Matches the primary Firestore schema while supporting backward compatibility with legacy formats.
 */
data class CanonicalMarketRecord(
    val marketName: String,
    val date: String,             // Format: YYYY-MM-DD (e.g. 2026-09-07) or dd-MM-yyyy
    val openPana: String? = null, // e.g. "123" or "***" if holiday
    val jodi: String? = null,     // e.g. "45" or "**" if holiday
    val closePana: String? = null,// e.g. "678" or "***" if holiday
    val isHoliday: Boolean = false,
    val source: String = "FIREBASE_CANONICAL",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "admin"
) {
    val marketKey: String
        get() = marketName.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_")

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "marketName" to marketName,
            "date" to date,
            "openPana" to (openPana ?: if (isHoliday) "***" else "159"),
            "jodi" to (jodi ?: if (isHoliday) "**" else "56"),
            "closePana" to (closePana ?: if (isHoliday) "***" else "647"),
            "isHoliday" to isHoliday,
            "source" to source,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "updatedBy" to updatedBy,
            "result" to if (isHoliday) "*** - ** - ***" else "${openPana ?: "159"} - ${jodi ?: "56"} - ${closePana ?: "647"}"
        )
    }
}

/**
 * Represents the active data source for Formula Lab and History datasets.
 */
enum class ActiveDataSource(val displayLabel: String) {
    FIREBASE_LIVE("Firebase Live"),
    FIREBASE_CACHE("Firebase Cache"),
    LOCAL_CACHE("Local Device Cache"),
    GITHUB_FALLBACK("GitHub Fallback")
}

/**
 * Overall synchronization status across Firestore, GitHub Mirror, and Local Cache.
 */
data class DataSyncState(
    val firebaseStatus: String = "CONNECTED", // "CONNECTED", "CONNECTING", "OFFLINE", "ERROR"
    val isFirebaseLive: Boolean = true,
    val totalFirebaseRecords: Int = 0,
    val totalMarketsLoaded: Int = 0,
    val githubSyncStatus: String = "SYNCED", // "SYNCED", "OUT OF DATE", "FALLBACK", "ERROR"
    val localPendingWritesCount: Int = 0,
    val lastFirebaseSyncTime: String = "Just now",
    val lastGithubSyncTime: String = "Pending",
    val activeDataSource: ActiveDataSource = ActiveDataSource.FIREBASE_LIVE,
    val statusDetailMessage: String? = null
)
