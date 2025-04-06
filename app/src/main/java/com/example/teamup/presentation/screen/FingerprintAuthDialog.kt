package com.example.teamup.presentation.screen

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

@Composable
fun FingerprintAuthDialog(
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit,
    onAuthError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fragmentActivity = context as FragmentActivity

    LaunchedEffect(true) {
        showBiometricPrompt(
            context = context,
            activity = fragmentActivity,
            onSuccess = onAuthSuccess,
            onFailed = onAuthFailed,
            onError = onAuthError,
            onDismiss = onDismiss
        )
    }
}

private fun showBiometricPrompt(
    context: Context,
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFailed: () -> Unit,
    onError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Check if biometric authentication is available
    val biometricManager = BiometricManager.from(context)
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            // Biometric features are available
            authenticateWithBiometrics(activity, onSuccess, onFailed, onError)
        }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            onError("Device doesn't have fingerprint hardware")
            onDismiss()
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            onError("Biometric hardware is currently unavailable")
            onDismiss()
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            onError("No fingerprints enrolled. Please register at least one fingerprint in your device settings")
            onDismiss()
        }
        else -> {
            onError("Biometric authentication is not available")
            onDismiss()
        }
    }
}

private fun authenticateWithBiometrics(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFailed: () -> Unit,
    onError: (String) -> Unit
) {
    val executor: Executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Login dengan Fingerprint")
        .setSubtitle("Login lebih cepat dengan fitur biometrik")
        .setNegativeButtonText("Batalkan")
        .setConfirmationRequired(false)
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()

    biometricPrompt.authenticate(promptInfo)
}