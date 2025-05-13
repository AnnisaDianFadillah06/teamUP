package com.example.teamup.presentation.screen.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.utils.BiometricAuthUtil
import com.example.teamup.common.utils.SessionManager
import com.example.teamup.route.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FingerprintLoginScreen(navController: NavController) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = context.getSharedPreferences("teamup_prefs", Context.MODE_PRIVATE)

    // Check biometric availability and credentials
    val isBiometricAvailable = remember { BiometricAuthUtil.isBiometricAvailable(context) }
    val hasSavedCredentials = remember { BiometricAuthUtil.hasSavedCredentials(context) }

    // State for activation status
    var isActivated by remember {
        mutableStateOf(sharedPrefs.getBoolean("fingerprint_activated", false))
    }

    // Authentication states
    var isAuthenticating by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var authSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Automatically show biometric prompt when component mounts (for quick login)
    LaunchedEffect(Unit) {
        // Check if coming from a direct fingerprint quick login attempt
        val isQuickLogin = sharedPrefs.getBoolean("quick_login_attempt", false)

        if (isQuickLogin && fragmentActivity != null && hasSavedCredentials) {
            // Reset the flag
            sharedPrefs.edit().putBoolean("quick_login_attempt", false).apply()

            // Show authentication dialog after a short delay
            delay(300)
            isAuthenticating = true

            // Try to authenticate
            BiometricAuthUtil.performQuickLogin(
                activity = fragmentActivity,
                navController = navController,
                onSuccess = {
                    // Mark user as logged in
                    SessionManager.setLoggedIn(context, true)

                    authSuccess = true
                    showResult = true
                    coroutineScope.launch {
                        delay(1000)
                        // Navigate to dashboard with clear backstack
                        navController.navigate(Routes.Dashboard.routes) {
                            popUpTo(Routes.LoginV5.routes) {
                                inclusive = true
                            }
                        }
                    }
                    isAuthenticating = false
                },
                onError = { message ->
                    authSuccess = false
                    errorMessage = message
                    showResult = true
                    isAuthenticating = false
                },
                onFailed = {
                    authSuccess = false
                    errorMessage = "Authentication failed"
                    showResult = true
                    isAuthenticating = false
                }
            )
        }
    }

    // Modal dialog to handle authentication
    if (isAuthenticating && fragmentActivity != null) {
        LaunchedEffect(isAuthenticating) {
            BiometricAuthUtil.performQuickLogin(
                activity = fragmentActivity,
                navController = navController,
                onSuccess = {
                    // Save activation status if activating for first time
                    if (!isActivated) {
                        sharedPrefs.edit().putBoolean("fingerprint_activated", true).apply()
                        isActivated = true
                    }

                    // Mark user as logged in in SessionManager
                    SessionManager.setLoggedIn(context, true)

                    authSuccess = true
                    showResult = true
                    coroutineScope.launch {
                        delay(1000)
                        // Navigate to dashboard if not already activating
                        if (isActivated) {
                            navController.navigate(Routes.Dashboard.routes) {
                                popUpTo(Routes.LoginV5.routes) {
                                    inclusive = true
                                }
                            }
                        } else {
                            Toast.makeText(context, "Fingerprint login activated successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                    isAuthenticating = false
                },
                onError = { message ->
                    authSuccess = false
                    errorMessage = message
                    showResult = true
                    isAuthenticating = false
                },
                onFailed = {
                    authSuccess = false
                    errorMessage = "Authentication failed"
                    showResult = true
                    isAuthenticating = false
                }
            )
        }
    }

    // Handle results display
    if (showResult) {
        LaunchedEffect(showResult) {
            delay(2000)
            showResult = false
        }
    }

    // Main UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Biometric icon with circle background
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(DodgerBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.fingerprint),
                    contentDescription = "Fingerprint Icon",
                    tint = DodgerBlue,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = if (isActivated) "Fingerprint Login" else "Aktivasi Fingerprint",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "Login lebih cepat dengan fitur biometrik. Yuk, aktifkan sekarang!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Activate button
            Button(
                onClick = {
                    if (fragmentActivity != null) {
                        if (!isBiometricAvailable) {
                            Toast.makeText(
                                context,
                                "Biometric authentication is not available on this device",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        if (!hasSavedCredentials) {
                            Toast.makeText(
                                context,
                                "Please log in with your email first and check 'Remember me'",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        isAuthenticating = true
                    } else {
                        Toast.makeText(
                            context,
                            "This feature requires a FragmentActivity context",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DodgerBlue
                )
            ) {
                Text(
                    text = if (isActivated) "Verify Fingerprint" else "Aktifkan",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel button
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Batalkan")
            }
        }

        // Feedback overlay
        if (showResult) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (authSuccess) {
                            Icon(
                                painter = painterResource(id = R.drawable.fingerprint),
                                contentDescription = "Success",
                                tint = DodgerBlue,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Authentication Successful",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.fingerprint),
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Authentication Failed",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}