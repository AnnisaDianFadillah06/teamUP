package com.example.teamup.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.data.viewmodels.AuthUiState
import com.example.teamup.data.viewmodels.AuthViewModel
import com.example.teamup.presentation.components.PrimaryButton
import com.example.teamup.route.Routes

@Composable
fun ResetPasswordScreen(navController: NavController) {
    val viewModel: AuthViewModel = viewModel()
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm New Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            if (newPassword == confirmPassword && newPassword.isNotEmpty()) {
                viewModel.updatePassword(newPassword) { success, err ->
                    if (success) {
                        navController.navigate(Routes.Login.routes) {
                            popUpTo(Routes.ResetPassword.routes) { inclusive = true }
                        }
                    } else {
                        message = "Gagal update: $err"
                    }
                }
            } else {
                message = "Password dan konfirmasi tidak sama atau kosong"
            }
        }) {
            Text("Reset Password")
        }
        message?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
