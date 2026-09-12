package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class A23Application : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            val firebaseAppCheck = FirebaseAppCheck.getInstance()

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Initializing Firebase App Check with DebugAppCheckProviderFactory")
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                Log.i(TAG, "Initializing Firebase App Check with PlayIntegrityAppCheckProviderFactory")
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }

            // Monitor token refresh events
            firebaseAppCheck.addAppCheckListener { tokenResult ->
                Log.d(TAG, "App Check token refreshed successfully (Token length: ${tokenResult.token.length})")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize Firebase App Check", e)
        }
    }

    companion object {
        private const val TAG = "A23MAX_AppCheck"
    }
}
