package com.example.teamup.presentation.screen.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamup.data.viewmodels.AuthViewModel
import com.example.teamup.presentation.components.PrimaryButton
import com.example.teamup.route.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CekEmailScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(
        navController.getBackStackEntry(Routes.Register.routes)
    ),
    email: String
) {
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Halaman ini akan melakukan polling status verifikasi email
    // Ini adalah alternatif karena Firebase tidak menyediakan listener untuk status verifikasi
    LaunchedEffect(Unit) {
        // Mulai polling status verifikasi email setiap 5 detik
        while (true) {
            viewModel.reloadCurrentUser { isVerified ->
                if (isVerified) {
                    navController.navigate(Routes.RegisterSuccess.routes) {
                        popUpTo(Routes.CekEmail.routes) { inclusive = true }
                    }
                }
            }
            delay(5000) // Cek setiap 5 detik
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Verifikasi Email",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Kami telah mengirim email verifikasi ke:",
            textAlign = TextAlign.Center
        )

        Text(
            email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Silakan periksa email Anda dan klik tautan verifikasi yang kami kirimkan. " +
                    "Setelah verifikasi, kembali ke aplikasi ini.",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = if (isChecking) "Memeriksa..." else "Saya Sudah Verifikasi Email"
        ) {
            if (isChecking) return@PrimaryButton

            isChecking = true
            errorMessage = ""

            scope.launch {
                viewModel.reloadCurrentUser { isVerified ->
                    isChecking = false
                    if (isVerified) {
                        navController.navigate(Routes.RegisterSuccess.routes) {
                            popUpTo(Routes.CekEmail.routes) { inclusive = true }
                        }
                    } else {
                        errorMessage = "Email belum diverifikasi. Silakan periksa email Anda dan klik tautan verifikasi."
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = "Kirim Ulang Email Verifikasi"
        ) {
            scope.launch {
                viewModel.resendEmailVerification { success, error ->
                    if (success) {
                        errorMessage = "Email verifikasi telah dikirim ulang."
                    } else {
                        errorMessage = error ?: "Gagal mengirim ulang email verifikasi."
                    }
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}