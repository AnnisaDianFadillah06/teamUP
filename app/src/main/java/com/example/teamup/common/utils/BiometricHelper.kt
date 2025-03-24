package com.example.teamup.common.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper class untuk mengimplementasikan autentikasi biometrik
 */
class BiometricHelper(private val context: Context) {

    /**
     * Memeriksa apakah perangkat mendukung autentikasi biometrik
     * @return Boolean - true jika biometrik tersedia dan dapat digunakan
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Menampilkan dialog autentikasi biometrik
     * @param activity FragmentActivity yang dibutuhkan untuk menampilkan prompt
     * @param onSuccess Fungsi callback yang dipanggil ketika autentikasi berhasil
     * @param onError Fungsi callback yang dipanggil ketika terjadi error
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login to TeamUp")
            .setSubtitle("Use your fingerprint to quickly sign in")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Fungsi untuk handling ketika icon fingerprint di klik
     * @param activity FragmentActivity context
     * @param onLoginSuccess Callback ketika login berhasil
     */
    fun handleFingerprintLogin(
        activity: FragmentActivity?,
        onLoginSuccess: () -> Unit
    ) {
        if (activity == null) return

        if (isBiometricAvailable()) {
            showBiometricPrompt(
                activity = activity,
                onSuccess = {
                    onLoginSuccess()
                },
                onError = { errorMsg ->
                    // Bisa tambahkan handling error di sini jika perlu
                }
            )
        }
    }
}