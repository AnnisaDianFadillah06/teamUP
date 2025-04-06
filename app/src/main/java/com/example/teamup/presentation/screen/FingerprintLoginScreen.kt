package com.example.teamup.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context



@Composable
fun FingerprintLoginScreen(navController: NavController) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    var showFingerprint by remember { mutableStateOf(false) }

    // Check if user is already logged in (from SharedPreferences)
    // This is needed to know if we can use fingerprint login
    val sharedPrefs = context.getSharedPreferences("teamup_prefs", Context.MODE_PRIVATE)
    val savedEmail = sharedPrefs.getString("saved_email", null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.fingerprint),
            contentDescription = "Fingerprint Icon",
            tint = DodgerBlue,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Aktivasi Fingerprint",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Login lebih cepat dengan fitur biometrik. Yuk, aktifkan sekarang!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { showFingerprint = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = DodgerBlue
            )
        ) {
            Text("Aktifkan")
        }

        Spacer(modifier = Modifier.height(16.dp))

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

    // Show fingerprint dialog when button is clicked
    if (showFingerprint && fragmentActivity != null && savedEmail != null) {
        FingerprintAuthDialog(
            onAuthSuccess = {
                // Fingerprint auth successful, login user with savedEmail
                loginWithSavedCredentials(context, navController, savedEmail)
            },
            onAuthFailed = {
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
            },
            onAuthError = { errorMessage ->
                Toast.makeText(context, "Authentication error: $errorMessage", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                showFingerprint = false
            }
        )
    } else if (showFingerprint && savedEmail == null) {
        // No saved credentials, can't use fingerprint
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Please login with email first before using fingerprint", Toast.LENGTH_LONG).show()
            showFingerprint = false
        }
    }
}

// Function to login with saved credentials
private fun loginWithSavedCredentials(
    context: android.content.Context,
    navController: NavController,
    email: String
) {
    val sharedPrefs = context.getSharedPreferences("teamup_prefs", android.content.Context.MODE_PRIVATE)
    val password = sharedPrefs.getString("saved_password", null)

    if (password != null) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Login successful
                Toast.makeText(context, "Login successful with fingerprint", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.Dashboard.routes) {
                    popUpTo(Routes.Login.routes) {
                        inclusive = true
                    }
                }
            }
            .addOnFailureListener {
                // Login failed
                Toast.makeText(context, "Failed to login: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    } else {
        Toast.makeText(context, "No saved credentials found", Toast.LENGTH_SHORT).show()
    }
}