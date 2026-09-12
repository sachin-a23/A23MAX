package com.example.data

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class FirebaseUserData(
    val uid: String,
    val email: String,
    val phoneNumber: String = "",
    val displayName: String,
    val isEmailVerified: Boolean,
    val isPhoneVerified: Boolean = false,
    val isAnonymous: Boolean = false
)

interface PhoneVerificationListener {
    fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)
    fun onAutoVerificationSuccess(credential: PhoneAuthCredential)
    fun onVerificationFailed(errorMessage: String, exception: Exception)
}

/**
 * Production Firebase Authentication Service.
 * Implements real Phone Authentication (SMS OTP via Firebase PhoneAuthProvider),
 * real Email + Password authentication, password recovery, and secure session management.
 * Strictly no mock or fake responses.
 */
class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "FirebaseAuthService"
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    fun getCurrentUserData(): FirebaseUserData? {
        val user = auth.currentUser ?: return null
        return FirebaseUserData(
            uid = user.uid,
            email = user.email ?: "",
            phoneNumber = user.phoneNumber ?: "",
            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "VIP Trader",
            isEmailVerified = user.isEmailVerified,
            isPhoneVerified = !user.phoneNumber.isNullOrBlank(),
            isAnonymous = user.isAnonymous
        )
    }

    /**
     * Ensures the app has an active authenticated Firebase user session.
     * Uses anonymous sign-in if no email/phone session is currently logged in.
     */
    suspend fun ensureAuthenticated(): Result<FirebaseUserData> = suspendCancellableCoroutine { continuation ->
        val current = getCurrentUserData()
        if (current != null) {
            continuation.resume(Result.success(current))
            return@suspendCancellableCoroutine
        }

        auth.signInAnonymously()
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    val data = FirebaseUserData(
                        uid = user.uid,
                        email = "",
                        phoneNumber = "",
                        displayName = "Guest User",
                        isEmailVerified = false,
                        isPhoneVerified = false,
                        isAnonymous = true
                    )
                    continuation.resume(Result.success(data))
                } else {
                    continuation.resume(Result.failure(Exception("Anonymous login succeeded but user is null.")))
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Anonymous sign-in failed: ${e.message}")
                continuation.resume(Result.failure(e))
            }
    }

    /**
     * Observable flow of Firebase Auth state.
     */
    val authStateFlow: Flow<FirebaseUserData?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val userData = user?.let {
                FirebaseUserData(
                    uid = it.uid,
                    email = it.email ?: "",
                    phoneNumber = it.phoneNumber ?: "",
                    displayName = it.displayName ?: it.email?.substringBefore("@") ?: "VIP Trader",
                    isEmailVerified = it.isEmailVerified,
                    isPhoneVerified = !it.phoneNumber.isNullOrBlank(),
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
     * Send Real SMS OTP to the provided phone number using Firebase PhoneAuthProvider.
     */
    fun sendPhoneVerificationOtp(
        activity: Activity,
        phoneNumber: String,
        resendToken: PhoneAuthProvider.ForceResendingToken? = null,
        listener: PhoneVerificationListener
    ) {
        val formattedNumber = formatPhoneNumber(phoneNumber)
        if (formattedNumber.isBlank()) {
            listener.onVerificationFailed("Invalid phone number format. Please include country code (e.g. +91 9876543210).", IllegalArgumentException("Invalid phone number"))
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Phone auto-verification completed by device SMS")
                listener.onAutoVerificationSuccess(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                val readableError = parseAuthErrorMessage(e)
                Log.e(TAG, "Phone verification failed: $readableError", e)
                listener.onVerificationFailed(readableError, e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "SMS OTP sent successfully by Firebase to: $formattedNumber")
                listener.onCodeSent(verificationId, token)
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    /**
     * Sign in or link using verified PhoneAuthCredential.
     */
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUserData> =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        val userData = FirebaseUserData(
                            uid = user.uid,
                            email = user.email ?: "",
                            phoneNumber = user.phoneNumber ?: "",
                            displayName = user.displayName ?: "VIP Trader",
                            isEmailVerified = user.isEmailVerified,
                            isPhoneVerified = true
                        )
                        continuation.resume(Result.success(userData))
                    } else {
                        continuation.resume(Result.failure(Exception("Phone sign-in failed: User is null")))
                    }
                }
                .addOnFailureListener { exception ->
                    val readable = parseAuthErrorMessage(exception)
                    continuation.resume(Result.failure(Exception(readable, exception)))
                }
        }

    /**
     * Real Email + Password Sign In.
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
                            phoneNumber = user.phoneNumber ?: "",
                            displayName = user.displayName ?: trimmedEmail.substringBefore("@"),
                            isEmailVerified = user.isEmailVerified,
                            isPhoneVerified = !user.phoneNumber.isNullOrBlank()
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
     * Real Email + Password Registration with Display Name.
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
                    if (trimmedName.isNotBlank()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(trimmedName)
                            .build()
                        user.updateProfile(profileUpdates)
                    }

                    val userData = FirebaseUserData(
                        uid = user.uid,
                        email = user.email ?: trimmedEmail,
                        phoneNumber = user.phoneNumber ?: "",
                        displayName = trimmedName.ifBlank { trimmedEmail.substringBefore("@") },
                        isEmailVerified = user.isEmailVerified,
                        isPhoneVerified = !user.phoneNumber.isNullOrBlank()
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
     * Real Password Reset link sent to registered email via Firebase.
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
                    continuation.resume(Result.success("Password reset instructions sent to $trimmedEmail. Please check your inbox and spam folder."))
                }
                .addOnFailureListener { exception ->
                    val readableMessage = parseAuthErrorMessage(exception)
                    continuation.resume(Result.failure(Exception(readableMessage, exception)))
                }
        }

    /**
     * Update password for the currently logged-in user.
     */
    suspend fun updatePassword(newPassword: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val user = auth.currentUser
            if (user == null) {
                continuation.resume(Result.failure(Exception("No active Firebase session. Please sign in again.")))
                return@suspendCancellableCoroutine
            }
            if (newPassword.length < 6) {
                continuation.resume(Result.failure(IllegalArgumentException("New password must be at least 6 characters.")))
                return@suspendCancellableCoroutine
            }

            user.updatePassword(newPassword)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    val readableMessage = parseAuthErrorMessage(exception)
                    continuation.resume(Result.failure(Exception(readableMessage, exception)))
                }
        }

    /**
     * Sign out current user from Firebase.
     */
    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out: ${e.message}")
        }
    }

    fun formatPhoneNumber(rawPhone: String): String {
        val clean = rawPhone.replace(" ", "").replace("-", "").trim()
        return when {
            clean.startsWith("+") -> clean
            clean.length == 10 && clean.all { it.isDigit() } -> "+91$clean"
            clean.length == 12 && clean.startsWith("91") && clean.all { it.isDigit() } -> "+$clean"
            clean.length == 11 && clean.startsWith("0") -> "+91${clean.substring(1)}"
            else -> clean
        }
    }

    fun parseAuthErrorMessage(exception: Exception): String {
        val msg = exception.localizedMessage ?: exception.message ?: ""
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                when {
                    msg.contains("code", ignoreCase = true) || msg.contains("otp", ignoreCase = true) ->
                        "Invalid SMS OTP verification code. Please check and enter the 6-digit code received."
                    msg.contains("password", ignoreCase = true) ->
                        "Incorrect password. Please verify and try again, or tap Forgot Password."
                    msg.contains("phone", ignoreCase = true) || msg.contains("format", ignoreCase = true) ->
                        "Invalid mobile number format. Please enter a valid 10-digit number."
                    else -> "Invalid credentials provided. Please check and try again."
                }
            }
            is FirebaseAuthInvalidUserException ->
                "No registered account found with this email/phone. Please create a new account."
            is FirebaseAuthUserCollisionException ->
                "An account already exists with this email or phone number. Please sign in or reset password."
            is FirebaseAuthWeakPasswordException ->
                "Password is too weak. Please use at least 6 alphanumeric characters."
            is FirebaseTooManyRequestsException ->
                "Too many attempts. Requests temporarily blocked for security. Please wait a minute and retry."
            else -> {
                when {
                    msg.contains("session-expired", ignoreCase = true) || msg.contains("expired", ignoreCase = true) ->
                        "The SMS OTP has expired. Please tap 'Resend OTP' to request a new code."
                    msg.contains("quota-exceeded", ignoreCase = true) ->
                        "SMS verification quota reached. Please try again later or use Email login."
                    msg.contains("network", ignoreCase = true) ->
                        "Network error. Please check your internet connection and try again."
                    msg.contains("app-not-authorized", ignoreCase = true) || msg.contains("play integrity", ignoreCase = true) || msg.contains("reCAPTCHA", ignoreCase = true) ->
                        "App verification required. Please ensure Google Play Services is up to date."
                    else -> msg.ifBlank { "Authentication request failed. Please check credentials and try again." }
                }
            }
        }
    }
}
