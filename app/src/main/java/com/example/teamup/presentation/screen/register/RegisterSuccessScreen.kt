package com.example.teamup.presentation.screen.register

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.White
import com.example.teamup.route.Routes

@Composable
fun RegisterSuccessScreen(navController: NavController) {
    // Auto-navigate to Complete Profile after 3 seconds
    LaunchedEffect(Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            navController.navigate(Routes.CompleteProfile.routes) {
                popUpTo(Routes.RegisterSuccess.routes) { inclusive = true }
            }
        }, 3000)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // Success icon instead of image asset
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Registration Success",
                tint = DodgerBlue,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Registration Successful!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = DodgerBlue
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your account has been created successfully. Let's complete your profile now.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate(Routes.CompleteProfile.routes) {
                        popUpTo(Routes.RegisterSuccess.routes) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue)
            ) {
                Text(
                    text = "Complete Profile",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = White
                    )
                )
            }
        }
    }
}
