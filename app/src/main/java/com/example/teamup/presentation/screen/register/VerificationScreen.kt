//verificationscreen.kt
package com.example.teamup.presentation.screen.register

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamup.presentation.components.PrimaryButton
import com.example.teamup.route.Routes
import com.example.teamup.data.viewmodels.AuthViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay

@Composable
fun VerificationScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(
        navController.getBackStackEntry(Routes.Register.routes)
    )
) {
    val activity = LocalContext.current as ComponentActivity

    // Log untuk debugging
    LaunchedEffect(Unit) {
        Log.d("VerificationScreen", "Screen dimuat, cek registrationData: ${viewModel.registrationData}")
    }

    var selectedMethod by remember { mutableStateOf("email") }
    var message by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    // Ambil data registrasi dari ViewModel
    val data = viewModel.registrationData ?: run {
        Log.e("VerificationScreen", "Data registrasi tidak ditemukan")
        // Tampilkan pesan atau kembali ke RegisterScreen
        LaunchedEffect(Unit) {
            delay(100)
            navController.popBackStack()
        }
        return
    }

    // Callback untuk Phone Auth
    val phoneCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = id
            message = "OTP telah dikirim ke ${data.phone}"
            isProcessing = false
            Log.d("VerificationScreen", "OTP terkirim ke ${data.phone}")
        }

        override fun onVerificationCompleted(cred: PhoneAuthCredential) {
            Log.d("VerificationScreen", "Verifikasi otomatis berhasil")
            // Jika ingin otomatis sign-in, bisa diimplementasikan di sini
        }

        override fun onVerificationFailed(e: FirebaseException) {
            message = "Verifikasi gagal: ${e.message}"
            isProcessing = false
            Log.e("VerificationScreen", "Verifikasi gagal", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verifikasi Akun", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = { selectedMethod = "email" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMethod == "email") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Email")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { selectedMethod = "phone" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMethod == "phone") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Phone")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = if (isProcessing) "Memproses..." else if (selectedMethod == "email") "Kirim Email Verifikasi" else "Kirim OTP"
        ) {
            if (isProcessing) return@PrimaryButton
            message = ""
            isProcessing = true
            try {
                if (selectedMethod == "email") {
                    Log.d("VerificationScreen", "Mencoba kirim email verifikasi")
                    viewModel.registerWithEmail(data) { ok, err ->
                        isProcessing = false
                        if (ok) {
                            Log.d("VerificationScreen", "Email verifikasi berhasil")
                            // Perubahan: Navigasi ke halaman instruksi verifikasi email
                            navController.navigate(Routes.CekEmail.createRoute(data.email)) {
                                popUpTo(Routes.Verification.routes) { inclusive = false }
                            }

                        } else {
                            Log.e("VerificationScreen", "Gagal kirim email: $err")
                            message = err ?: "Gagal kirim email. Periksa koneksi Firebase."
                        }
                    }
                } else {
                    Log.d("VerificationScreen", "Mencoba kirim OTP ke: ${data.phone}")
                    viewModel.sendPhoneOtp(data.phone, activity, phoneCallbacks)
                    // phoneCallbacks akan mengatur isProcessing = false
                }
            } catch (e: Exception) {
                isProcessing = false
                Log.e("VerificationScreen", "Error saat verifikasi", e)
                message = "Error: ${e.message}"
            }
        }

        if (isProcessing) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        if (selectedMethod == "phone") {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = otpInput,
                onValueChange = { otpInput = it },
                label = { Text("Masukkan OTP") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = if (isProcessing) "Memverifikasi..." else "Verifikasi OTP"
            ) {
                if (isProcessing || otpInput.isEmpty()) return@PrimaryButton
                message = ""
                isProcessing = true
                Log.d("VerificationScreen", "Mencoba verifikasi OTP")
                viewModel.verifyPhoneOtp(verificationId, otpInput) { ok, err ->
                    if (ok) {
                        viewModel.saveProfile(data) { success, errorMsg ->
                            isProcessing = false
                            if (success) {
                                Log.d("VerificationScreen", "Verifikasi OTP berhasil")
                                navController.navigate(Routes.RegisterSuccess.routes) {
                                    popUpTo(Routes.Verification.routes) { inclusive = true }
                                }
                            } else {
                                Log.e("VerificationScreen", "Gagal simpan profil: $errorMsg")
                                message = errorMsg ?: "Gagal simpan profil"
                            }
                        }
                    } else {
                        isProcessing = false
                        Log.e("VerificationScreen", "OTP salah: $err")
                        message = err ?: "OTP salah"
                    }
                }
            }
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
    }
}