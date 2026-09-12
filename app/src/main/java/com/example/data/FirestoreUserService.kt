package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class FirestoreUserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val phoneVerified: Boolean = false,
    val emailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val pinEnabled: Boolean = false,
    val city: String = "",
    val role: String = "VIP Member"
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "displayName" to displayName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "phoneVerified" to phoneVerified,
            "emailVerified" to emailVerified,
            "createdAt" to createdAt,
            "lastLoginAt" to lastLoginAt,
            "pinEnabled" to pinEnabled,
            "city" to city,
            "role" to role
        )
    }
}

/**
 * Service for managing secure user profiles in Cloud Firestore under `users/{uid}`.
 * Strictly adheres to zero-sensitive-data storage (no PINs, no passwords, no OTPs).
 */
class FirestoreUserService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FirestoreUserService"
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Saves or merges the user's profile document into Firestore under `users/{uid}`.
     */
    suspend fun saveUserProfile(profile: FirestoreUserProfile): Result<Unit> {
        return try {
            if (profile.uid.isBlank()) {
                return Result.failure(IllegalArgumentException("UID cannot be empty for Firestore profile."))
            }

            firestore.collection(USERS_COLLECTION)
                .document(profile.uid)
                .set(profile.toMap(), SetOptions.merge())
                .await()

            Log.d(TAG, "User profile successfully saved to Firestore for UID: ${profile.uid}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Could not save profile to Firestore (Offline or Error): ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Retrieves the user profile document from Firestore.
     */
    suspend fun getUserProfile(uid: String): Result<FirestoreUserProfile?> {
        return try {
            if (uid.isBlank()) {
                return Result.failure(IllegalArgumentException("UID cannot be empty."))
            }

            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (snapshot.exists()) {
                val profile = FirestoreUserProfile(
                    uid = snapshot.getString("uid") ?: uid,
                    displayName = snapshot.getString("displayName") ?: "",
                    email = snapshot.getString("email") ?: "",
                    phoneNumber = snapshot.getString("phoneNumber") ?: "",
                    phoneVerified = snapshot.getBoolean("phoneVerified") ?: false,
                    emailVerified = snapshot.getBoolean("emailVerified") ?: false,
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    lastLoginAt = snapshot.getLong("lastLoginAt") ?: System.currentTimeMillis(),
                    pinEnabled = snapshot.getBoolean("pinEnabled") ?: false,
                    city = snapshot.getString("city") ?: "",
                    role = snapshot.getString("role") ?: "VIP Member"
                )
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching user profile from Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Updates the last login timestamp for the authenticated user.
     */
    suspend fun updateLastLogin(uid: String) {
        try {
            if (uid.isNotBlank()) {
                firestore.collection(USERS_COLLECTION)
                    .document(uid)
                    .update("lastLoginAt", System.currentTimeMillis())
                    .await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not update last login: ${e.message}")
        }
    }

    /**
     * Updates the pinEnabled flag for the user's Firestore profile.
     */
    suspend fun updatePinEnabledStatus(uid: String, pinEnabled: Boolean) {
        try {
            if (uid.isNotBlank()) {
                firestore.collection(USERS_COLLECTION)
                    .document(uid)
                    .update("pinEnabled", pinEnabled)
                    .await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not update pinEnabled: ${e.message}")
        }
    }
}
