package com.example.teamup.common.utils

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth

object BiometricAuthUtil {

    /**
     * Check if biometric authentication is available on the device
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Check if user has saved credentials for quick login
     */
    fun hasSavedCredentials(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences("teamup_prefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("saved_email", null)
        val savedPassword = sharedPrefs.getString("saved_password", null)
        return savedEmail != null && savedPassword != null
    }

    /**
     * Perform quick login with biometric authentication
     */
    fun performQuickLogin(
        activity: FragmentActivity,
        navController: NavController,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
        onFailed: () -> Unit = {}
    ) {
        if (!isBiometricAvailable(activity)) {
            onError("Biometric authentication is not available on this device")
            return
        }

        // Hilangkan pemeriksaan saved credentials untuk memungkinkan quick login langsung
        // Setup prompt
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Quick Login")
            .setSubtitle("Login to TeamUp using your fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // Cek apakah ada saved credentials
                val sharedPrefs = activity.getSharedPreferences("teamup_prefs", Context.MODE_PRIVATE)
                val savedEmail = sharedPrefs.getString("saved_email", null)
                val savedPassword = sharedPrefs.getString("saved_password", null)

                if (savedEmail != null && savedPassword != null) {
                    // Login dengan Firebase jika ada saved credentials
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(savedEmail, savedPassword)
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Login successful", Toast.LENGTH_SHORT).show()

                            // Mark user as logged in
                            SessionManager.setLoggedIn(activity, true)

                            // Navigate to Dashboard & clear back stack
                            navController.navigate(Routes.Dashboard.routes) {
                                popUpTo(Routes.LoginV5.routes) { inclusive = true }
                            }

                            onSuccess()
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            onError("Firebase login failed: ${it.message}")
                        }
                } else {
                    // Jika tidak ada saved credentials, anggap berhasil otentikasi tapi perlu set credentials
                    // Mark user as "authenticated with biometric only"
                    val editor = sharedPrefs.edit()
                    editor.putBoolean("biometric_authenticated", true)
                    editor.apply()

                    Toast.makeText(activity, "Biometric authentication successful", Toast.LENGTH_SHORT).show()

                    // Navigasi ke dashboard tanpa perlu kredensial
                    // Kita bisa menambahkan logika tambahan jika dibutuhkan
                    navController.navigate(Routes.Dashboard.routes) {
                        popUpTo(Routes.LoginV5.routes) { inclusive = true }
                    }

                    onSuccess()
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                // Only call error handler for actual errors, not for cancellation
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                } else {
                    // Just dismiss quietly on cancel
                    onError("Authentication cancelled")
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        // Create and show the biometric prompt
        try {
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError("Failed to launch biometric authentication: ${e.message}")
        }
    }

    /**
     * Quick login launcher for direct use from other screens
     */
    fun launchQuickLogin(context: Context, navController: NavController) {
        if (context !is FragmentActivity) {
            Toast.makeText(context, "This feature requires a FragmentActivity context", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isBiometricAvailable(context)) {
            Toast.makeText(context, "Biometric authentication is not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        // Hapus pengecekan hasSavedCredentials untuk memungkinkan quick login langsung

        // Flag to indicate intent to do quick login
        val sharedPrefs = context.getSharedPreferences("teamup_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("quick_login_attempt", true).apply()

        // Navigate to fingerprint login screen
        navController.navigate(Routes.FingerprintLogin.routes)
    }
}