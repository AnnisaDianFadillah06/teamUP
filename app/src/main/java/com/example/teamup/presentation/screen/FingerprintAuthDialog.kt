package com.example.teamup.presentation.screen

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@Composable
fun FingerprintAuthDialog(
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit,
    onAuthError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val scope = rememberCoroutineScope()

    // State for UI
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check if biometric is available
    val isBiometricAvailable = isBiometricAvailable(context)

    // Effect to start biometric auth when dialog is shown
    LaunchedEffect(key1 = Unit) {
        if (isBiometricAvailable) {
            delay(300) // Short delay to ensure the dialog is shown first
            startBiometricAuthentication(
                activity = activity,
                onSuccess = {
                    isAuthenticating = false
                    onAuthSuccess()
                },
                onError = { code, message ->
                    errorMessage = message.toString()
                    isAuthenticating = false
                    onAuthError(message.toString())
                },
                onFailed = {
                    isAuthenticating = false
                    onAuthFailed()
                }
            )
            isAuthenticating = true
        } else {
            errorMessage = "Biometric authentication is not available on this device or not set up"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Fingerprint Authentication",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Fingerprint Icon
                Icon(
                    painter = painterResource(id = R.drawable.fingerprint),
                    contentDescription = "Fingerprint",
                    tint = DodgerBlue,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Message
                Text(
                    text = if (errorMessage != null)
                        errorMessage!!
                    else if (isAuthenticating)
                        "Please verify your identity using fingerprint"
                    else
                        "Touch the fingerprint sensor",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (errorMessage != null) Color.Red else Color.Unspecified,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Cancel")
                }

                // Try Again Button (shown only if there's an error)
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            scope.launch {
                                delay(300)
                                startBiometricAuthentication(
                                    activity = activity,
                                    onSuccess = {
                                        isAuthenticating = false
                                        onAuthSuccess()
                                    },
                                    onError = { code, message ->
                                        errorMessage = message.toString()
                                        isAuthenticating = false
                                        onAuthError(message.toString())
                                    },
                                    onFailed = {
                                        isAuthenticating = false
                                        onAuthFailed()
                                    }
                                )
                                isAuthenticating = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DodgerBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

// Fungsi untuk memeriksa apakah biometrik tersedia
fun isBiometricAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
}

// Fungsi untuk memulai autentikasi biometrik
fun startBiometricAuthentication(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (Int, CharSequence) -> Unit,
    onFailed: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError(errorCode, errString)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onFailed()
        }
    }

    val biometricPrompt = BiometricPrompt(activity, executor, callback)

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Authentication")
        .setSubtitle("Log in using your biometric credential")
        .setNegativeButtonText("Cancel")
        .build()

    try {
        biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
        onError(-1, "Failed to initialize biometric authentication: ${e.message}")
    }
}