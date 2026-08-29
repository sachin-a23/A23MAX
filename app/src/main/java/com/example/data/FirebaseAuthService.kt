package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class FirebaseUserData(
    val uid: String,
    val email: String,
    val displayName: String,
    val isEmailVerified: Boolean,
    val isAnonymous: Boolean = false
)

class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    fun getCurrentUserData(): FirebaseUserData? {
        val user = auth.currentUser ?: return null
        return FirebaseUserData(
            uid = user.uid,
            email = user.email ?: "",
            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "A23 User",
            isEmailVerified = user.isEmailVerified,
            isAnonymous = user.isAnonymous
        )
    }

    /**
     * Flow of FirebaseUser updates whenever auth state changes.
     */
    val authStateFlow: Flow<FirebaseUserData?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val userData = user?.let {
                FirebaseUserData(
                    uid = it.uid,
                    email = it.email ?: "",
                    displayName = it.displayName ?: it.email?.substringBefore("@") ?: "A23 User",
                    isEmailVerified = it.isEmailVerified,
                    isAnonymous = it.isAnonymous
                )
            }
            trySend(userData)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Sign In with Email and Password
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUserData> =
        suspendCancellableCoroutine { continuation ->
            val trimmedEmail = email.trim()
            if (trimmedEmail.isBlank() || pass.isBlank()) {
                continuation.resume(Result.failure(IllegalArgumentException("Email and password cannot be empty.")))
                return@suspendCancellableCoroutine
            }

            auth.signInWithEmailAndPassword(trimmedEmail, pass)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        val userData = FirebaseUserData(
                            uid = user.uid,
                            email = user.email ?: trimmedEmail,
                            displayName = user.displayName ?: trimmedEmail.substringBefore("@"),
                            isEmailVerified = user.isEmailVerified
                        )
                        continuation.resume(Result.success(userData))
                    } else {
                        continuation.resume(Result.failure(Exception("Sign in failed: User is null")))
                    }
                }
                .addOnFailureListener { exception ->
                    val readableMessage = parseAuthErrorMessage(exception)
                    continuation.resume(Result.failure(Exception(readableMessage, exception)))
                }
        }

    /**
     * Register new user with Email, Password and Display Name
     */
    suspend fun registerWithEmail(
        email: String,
        pass: String,
        displayName: String
    ): Result<FirebaseUserData> = suspendCancellableCoroutine { continuation ->
        val trimmedEmail = email.trim()
        val trimmedName = displayName.trim()

        if (trimmedEmail.isBlank() || pass.isBlank()) {
            continuation.resume(Result.failure(IllegalArgumentException("Email and password cannot be empty.")))
            return@suspendCancellableCoroutine
        }
        if (pass.length < 6) {
            continuation.resume(Result.failure(IllegalArgumentException("Password must be at least 6 characters.")))
            return@suspendCancellableCoroutine
        }

        auth.createUserWithEmailAndPassword(trimmedEmail, pass)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    // Update user's profile with display name if provided
                    if (trimmedName.isNotBlank()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(trimmedName)
                            .build()
                        user.updateProfile(profileUpdates)
                    }

                    val userData = FirebaseUserData(
                        uid = user.uid,
                        email = user.email ?: trimmedEmail,
                        displayName = trimmedName.ifBlank { trimmedEmail.substringBefore("@") },
                        isEmailVerified = user.isEmailVerified
                    )
                    continuation.resume(Result.success(userData))
                } else {
                    continuation.resume(Result.failure(Exception("Registration failed: User is null")))
                }
            }
            .addOnFailureListener { exception ->
                val readableMessage = parseAuthErrorMessage(exception)
                continuation.resume(Result.failure(Exception(readableMessage, exception)))
            }
    }

    /**
     * Send Password Reset Email
     */
    suspend fun sendPasswordReset(email: String): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val trimmedEmail = email.trim()
            if (trimmedEmail.isBlank()) {
                continuation.resume(Result.failure(IllegalArgumentException("Please enter your registered email address.")))
                return@suspendCancellableCoroutine
            }

            auth.sendPasswordResetEmail(trimmedEmail)
                .addOnSuccessListener {
                    continuation.resume(Result.success("Password reset link sent to $trimmedEmail. Please check your inbox and spam folder."))
                }
                .addOnFailureListener { exception ->
                    val readableMessage = parseAuthErrorMessage(exception)
                    continuation.resume(Result.failure(Exception(readableMessage, exception)))
                }
        }

    /**
     * Sign out current user
     */
    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun parseAuthErrorMessage(exception: Exception): String {
        val msg = exception.localizedMessage ?: exception.message ?: ""
        return when {
            msg.contains("password", ignoreCase = true) && msg.contains("invalid", ignoreCase = true) ->
                "Invalid password. Please check and try again."
            msg.contains("user-not-found", ignoreCase = true) || msg.contains("no user record", ignoreCase = true) ->
                "No registered account found with this email. Please register first."
            msg.contains("email-already-in-use", ignoreCase = true) || msg.contains("already in use", ignoreCase = true) ->
                "This email is already registered. Please sign in or use Forgot Password."
            msg.contains("invalid-email", ignoreCase = true) || msg.contains("badly formatted", ignoreCase = true) ->
                "Please enter a valid email address format (e.g. user@gmail.com)."
            msg.contains("weak-password", ignoreCase = true) ->
                "Password is too weak. Please use at least 6 characters."
            msg.contains("network", ignoreCase = true) ->
                "Network connection error. Please check your internet connection."
            msg.contains("too-many-requests", ignoreCase = true) ->
                "Too many attempts. Access temporarily disabled for security. Please try again later."
            else -> msg.ifBlank { "Authentication request failed. Please check credentials and try again." }
        }
    }
}
