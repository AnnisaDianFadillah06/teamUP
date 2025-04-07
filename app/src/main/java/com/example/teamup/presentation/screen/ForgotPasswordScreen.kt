package com.example.teamup.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamup.data.viewmodels.AuthUiState
import com.example.teamup.data.viewmodels.AuthViewModel
import com.example.teamup.route.Routes
import kotlinx.coroutines.launch

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
                    navController.navigate(Routes.Login.routes) {
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
