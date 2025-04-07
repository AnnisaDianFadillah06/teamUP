package com.example.teamup.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.teamup.presentation.components.PrimaryButton
import com.example.teamup.route.Routes

@Composable
fun RegisterSuccessScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrasi Berhasil!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(text = "Lanjut ke Login") {
            navController.navigate(Routes.Login.routes) {
                popUpTo(Routes.RegisterSuccess.routes) { inclusive = true }
            }
        }
    }
}
