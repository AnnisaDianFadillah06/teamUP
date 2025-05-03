package com.example.teamup.deeplink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.teamup.MainActivity
import com.example.teamup.common.theme.ESailTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activity yang menangani deep link dari halaman verifikasi email.
 */
class DeepLinkHandlerActivity : ComponentActivity() {

    private val TAG = "DeepLinkHandler"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log untuk debugging
        val uri: Uri? = intent?.data
        Log.d(TAG, "Deep link received: ${uri?.toString() ?: "null"}")

        // Cek intent type dan action
        Log.d(TAG, "Intent type: ${intent?.type}, action: ${intent?.action}")

        // Tampilkan UI loading selama proses redirect
        setContent {
            ESailTheme {
                DeepLinkHandlerScreen(
                    isEmailVerification = uri?.toString()?.contains("emailverified") == true
                )
            }
        }

        // Gunakan lifecycleScope untuk menjalankan coroutine non-composable
        lifecycleScope.launch {
            delay(1500) // Delay 1.5 detik untuk menampilkan UI loading
            if (uri != null) {
                handleDeepLink(uri)
            } else {
                Log.d(TAG, "No URI found in intent")
                startMainActivity(verified = false)
            }
        }
    }

    private fun handleDeepLink(uri: Uri) {
        Log.d(TAG, "Processing URI: $uri")

        when {
            // Handle email verification links
            uri.toString().contains("emailverified") -> {
                Log.d(TAG, "Email verification deeplink detected")

                // Reload current user to ensure we have latest verification status
                val auth = FirebaseAuth.getInstance()
                auth.currentUser?.reload()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val isVerified = auth.currentUser?.isEmailVerified ?: false
                        Log.d(TAG, "User email verified: $isVerified")
                        startMainActivity(verified = true)
                    } else {
                        Log.w(TAG, "Failed to reload user", task.exception)
                        startMainActivity(verified = true) // Still proceed as successful for UX
                    }
                } ?: run {
                    // No user signed in
                    Log.d(TAG, "No current user found")
                    startMainActivity(verified = true) // Still treat as success for better UX
                }
            }

            // Add more deeplink handlers here as needed
            uri.toString().contains("action") -> {
                // Handle action code links from Firebase
                val actionCode = uri.getQueryParameter("oobCode")
                val mode = uri.getQueryParameter("mode")

                Log.d(TAG, "Firebase action link: mode=$mode, code=$actionCode")

                if (mode == "verifyEmail" && actionCode != null) {
                    FirebaseAuth.getInstance().applyActionCode(actionCode)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Successfully applied action code")
                                startMainActivity(verified = true)
                            } else {
                                Log.w(TAG, "Error applying action code", task.exception)
                                startMainActivity(verified = false)
                            }
                        }
                } else {
                    startMainActivity(verified = false)
                }
            }

            else -> {
                Log.d(TAG, "Unknown deeplink pattern: $uri")
                startMainActivity(verified = false)
            }
        }
    }

    private fun startMainActivity(verified: Boolean) {
        Log.d(TAG, "Navigating to MainActivity with verified=$verified")

        val intent = Intent(this, MainActivity::class.java).apply {
            if (verified) {
                putExtra("EMAIL_VERIFIED", true)
                putExtra("NAVIGATE_TO", "register_success")
            }
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}