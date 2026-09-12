package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom

sealed class PinVerifyResult {
    data object Success : PinVerifyResult()
    data class WrongPin(val remainingAttempts: Int) : PinVerifyResult()
    data class LockedOut(val cooldownSeconds: Long) : PinVerifyResult()
    data object NotSet : PinVerifyResult()
    data class Error(val message: String) : PinVerifyResult()
}

/**
 * Production Security Gate: Cryptographic 4-Digit Local App Lock Manager.
 *
 * Security Guarantees:
 * 1. The raw 4-digit PIN is NEVER stored in plain text (No SharedPreferences, No Firestore, No Logs).
 * 2. Uses SHA-256 cryptographic hashing with a 32-byte CSPRNG-generated salt.
 * 3. Salt and Hash are stored in app-private storage.
 * 4. Maximum 5 wrong attempts trigger a 30-second security cooldown to prevent brute force.
 * 5. Device-local security: The PIN protects the local session only and does NOT replace Firebase Auth.
 */
object AppPinSecurityManager {

    private const val PREFS_NAME = "a23_pin_security_prefs"
    private const val KEY_PIN_ENABLED = "key_pin_enabled"
    private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
    private const val KEY_LOCKOUT_TIMESTAMP = "key_lockout_timestamp"

    private const val HASH_FILE = "a23_pin_hash.bin"
    private const val SALT_FILE = "a23_pin_salt.bin"

    private const val MAX_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds cooldown

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if 4-digit App PIN protection is currently active and set.
     */
    fun isPinEnabled(context: Context): Boolean {
        val prefs = getPrefs(context)
        val enabled = prefs.getBoolean(KEY_PIN_ENABLED, false)
        if (!enabled) return false

        val hashFile = File(context.filesDir, HASH_FILE)
        val saltFile = File(context.filesDir, SALT_FILE)
        return hashFile.exists() && saltFile.exists() && hashFile.length() > 0 && saltFile.length() > 0
    }

    /**
     * Securely sets a new 4-digit PIN using cryptographic salted hashing.
     */
    fun setPin(context: Context, pin: String): Boolean {
        if (!isValidPinFormat(pin)) {
            return false
        }

        try {
            // Generate a 32-byte secure random salt
            val salt = ByteArray(32)
            SecureRandom().nextBytes(salt)

            // Hash PIN with salt
            val hash = hashPinWithSalt(pin, salt)

            // Write hash and salt to app-private directory
            val hashFile = File(context.filesDir, HASH_FILE)
            val saltFile = File(context.filesDir, SALT_FILE)

            hashFile.writeBytes(hash)
            saltFile.writeBytes(salt)

            // Enable PIN in preferences and reset attempts
            getPrefs(context).edit()
                .putBoolean(KEY_PIN_ENABLED, true)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_TIMESTAMP, 0L)
                .apply()

            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Completely disables App PIN protection and wipes stored hash and salt.
     */
    fun disablePin(context: Context) {
        try {
            val hashFile = File(context.filesDir, HASH_FILE)
            val saltFile = File(context.filesDir, SALT_FILE)

            if (hashFile.exists()) hashFile.delete()
            if (saltFile.exists()) saltFile.delete()

            getPrefs(context).edit()
                .putBoolean(KEY_PIN_ENABLED, false)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_TIMESTAMP, 0L)
                .apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Verifies the entered 4-digit PIN against the stored cryptographic hash.
     * Enforces rate limiting and 30s lockout after 5 consecutive failures.
     */
    fun verifyPin(context: Context, enteredPin: String): PinVerifyResult {
        if (!isPinEnabled(context)) {
            return PinVerifyResult.NotSet
        }

        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_TIMESTAMP, 0L)

        // Check if currently in cooldown
        if (now < lockoutUntil) {
            val remainingSeconds = (lockoutUntil - now + 999L) / 1000L
            return PinVerifyResult.LockedOut(remainingSeconds)
        }

        if (!isValidPinFormat(enteredPin)) {
            return PinVerifyResult.Error("PIN must be exactly 4 numeric digits.")
        }

        try {
            val hashFile = File(context.filesDir, HASH_FILE)
            val saltFile = File(context.filesDir, SALT_FILE)

            if (!hashFile.exists() || !saltFile.exists()) {
                return PinVerifyResult.NotSet
            }

            val storedHash = hashFile.readBytes()
            val storedSalt = saltFile.readBytes()

            val enteredHash = hashPinWithSalt(enteredPin, storedSalt)

            if (MessageDigest.isEqual(storedHash, enteredHash)) {
                // Correct PIN: Reset failed attempts counter
                prefs.edit()
                    .putInt(KEY_FAILED_ATTEMPTS, 0)
                    .putLong(KEY_LOCKOUT_TIMESTAMP, 0L)
                    .apply()
                return PinVerifyResult.Success
            } else {
                // Wrong PIN: Increment failed attempts
                val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
                val editor = prefs.edit()
                editor.putInt(KEY_FAILED_ATTEMPTS, failedAttempts)

                if (failedAttempts >= MAX_ATTEMPTS) {
                    val lockoutTime = now + LOCKOUT_DURATION_MS
                    editor.putLong(KEY_LOCKOUT_TIMESTAMP, lockoutTime)
                    editor.putInt(KEY_FAILED_ATTEMPTS, 0) // reset count for next cycle
                    editor.apply()
                    return PinVerifyResult.LockedOut(LOCKOUT_DURATION_MS / 1000L)
                } else {
                    editor.apply()
                    val remaining = MAX_ATTEMPTS - failedAttempts
                    return PinVerifyResult.WrongPin(remaining)
                }
            }
        } catch (e: Exception) {
            return PinVerifyResult.Error("Verification error: ${e.message}")
        }
    }

    /**
     * Checks if the device is currently in lockout state.
     */
    fun getCooldownSecondsRemaining(context: Context): Long {
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_TIMESTAMP, 0L)
        return if (now < lockoutUntil) {
            (lockoutUntil - now + 999L) / 1000L
        } else {
            0L
        }
    }

    fun getFailedAttempts(context: Context): Int {
        return getPrefs(context).getInt(KEY_FAILED_ATTEMPTS, 0)
    }

    fun isValidPinFormat(pin: String): Boolean {
        return pin.length == 4 && pin.all { it.isDigit() }
    }

    private fun hashPinWithSalt(pin: String, salt: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        md.update(pin.toByteArray(Charsets.UTF_8))
        return md.digest()
    }
}
