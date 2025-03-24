package com.example.teamup.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.presentation.components.FingerprintButton
import com.example.teamup.route.Routes
import com.example.teamup.common.utils.BiometricHelper

/**
 * Screen login dengan opsi biometrik fingerprint
 */
@Composable
fun FingerprintLoginScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context) }
    val isBiometricAvailable = remember { biometricHelper.isBiometricAvailable() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Text(
            text = "Quick Login",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = DodgerBlue,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sign in quickly using your fingerprint",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SoftGray2,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isBiometricAvailable) {
            FingerprintButton(
                navController = navController,
                biometricHelper = biometricHelper
            )
        } else {
            Text(
                text = "Biometric authentication is not available on this device.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SoftGray2,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Divider with "OR" text
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp),
                color = SoftGray
            )
            Text(
                text = "  OR  ",
                style = MaterialTheme.typography.bodySmall.copy(color = SoftGray2)
            )
            Divider(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp),
                color = SoftGray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Link to standard login
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sign in with email and password",
                style = MaterialTheme.typography.bodyMedium.copy(color = SoftGray2)
            )
            Spacer(modifier = Modifier.width(5.dp))
            TextButton(onClick = {
                navController.navigate(Routes.Login.routes)
            }) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DodgerBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}