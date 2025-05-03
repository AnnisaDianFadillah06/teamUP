package com.example.teamup.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamup.data.viewmodels.AuthViewModel
import com.example.teamup.route.Routes

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val viewModel: AuthViewModel = viewModel()
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Forgot Password", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            viewModel.sendPasswordResetEmail(email) { success, err ->
                message = if (success) "Email reset terkirim" else "Error: $err"
                if (success) {
                    navController.navigate(Routes.LoginV5.routes) {
                        popUpTo(Routes.ForgotPassword.routes) { inclusive = true }
                    }
                }
            }
        }) {
            Text("Send Reset Email")
        }
        message?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
