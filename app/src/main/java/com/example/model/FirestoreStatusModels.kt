package com.example.model

/**
 * Real-time health and connection status of Firebase Firestore.
 */
enum class FirestoreHealthStatus {
    UNKNOWN,
    CHECKING,
    CONNECTED_LIVE,     // Real Firestore ping/read successful
    OFFLINE_CACHE,      // Network offline or reading from local cache
    PERMISSION_DENIED,  // Security rules blocked
    ERROR               // Other network or auth errors
}

/**
 * Status of an individual write operation.
 */
sealed class FirestoreWriteResult {
    data class Success(val marketName: String, val date: String, val isCreatedMarket: Boolean = false) : FirestoreWriteResult()
    data class Failure(val marketName: String, val date: String, val errorMessage: String, val isPermissionDenied: Boolean = false) : FirestoreWriteResult()
}
