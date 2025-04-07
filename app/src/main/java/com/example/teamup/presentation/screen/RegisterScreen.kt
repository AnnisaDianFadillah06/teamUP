package com.example.teamup.presentation.screen

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamup.data.viewmodels.AuthViewModel
import com.example.teamup.presentation.components.AuthSocial
import com.example.teamup.presentation.components.PasswordTextField
import com.example.teamup.presentation.components.PrimaryButton
import com.example.teamup.route.Routes
import com.example.teamup.ui.components.PrimaryTextField
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavController,
    // Ambil backStackEntry dari rute "register" untuk memastikan ViewModel instance yang sama
    viewModel: AuthViewModel = viewModel(
        navController.getBackStackEntry(Routes.Register.routes)
    )
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Buat Akun Baru", style = MaterialTheme.typography.headlineSmall, fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))
        PrimaryTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = "Full Name"
        )
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = "Username"
        )
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Email"
        )
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = "Phone Number"
        )
        Spacer(Modifier.height(8.dp))
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password"
        )
        Spacer(Modifier.height(8.dp))
        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Confirm Password"
        )
        Spacer(Modifier.height(16.dp))

        errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        PrimaryButton(text = if (isLoading) "Loading..." else "Register") {
            if (isLoading) return@PrimaryButton
            errorMsg = null

            when {
                fullName.isBlank() ->
                    errorMsg = "Full name tidak boleh kosong"
                username.isBlank() ->
                    errorMsg = "Username tidak boleh kosong"
                email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    errorMsg = "Email tidak valid"
                phone.isBlank() ->
                    errorMsg = "Phone number tidak boleh kosong"
                !viewModel.isPasswordStrong(password) ->
                    errorMsg = "Password minimal 8 karakter, ada angka, huruf besar, dan spesial"
                password != confirmPassword ->
                    errorMsg = "Password dan konfirmasi tidak sama"
            }

            if (errorMsg == null) {
                isLoading = true
                scope.launch {
                    try {
                        val emailTaken = viewModel.isEmailTaken(email)
                        val userTaken = viewModel.isUsernameTaken(username)
                        val phoneTaken = viewModel.isPhoneTaken(phone)

                        when {
                            emailTaken ->
                                errorMsg = "Email sudah terdaftar"
                            userTaken ->
                                errorMsg = "Username sudah digunakan"
                            phoneTaken ->
                                errorMsg = "Nomor telepon sudah terdaftar"
                            else -> {
                                viewModel.setRegistrationData(fullName, username, email, phone, password)
                                Log.d("RegisterScreen", "Navigasi ke VerificationScreen")
                                // Pastikan tidak menggunakan popUpTo agar data tidak hilang
                                navController.navigate(Routes.Verification.routes)
                            }
                        }
                    } catch (e: Exception) {
                        errorMsg = "Error: ${e.message ?: "Terjadi kesalahan"}"
                    } finally {
                        isLoading = false
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        AuthSocial()
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = {
            navController.navigate(Routes.Login.routes) {
                popUpTo(Routes.Register.routes) { inclusive = true }
            }
        }) {
            Text("Sign In")
        }
    }
}
