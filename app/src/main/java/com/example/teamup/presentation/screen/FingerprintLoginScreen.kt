package com.example.teamup.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.utils.BiometricHelper
import com.example.teamup.presentation.components.PasswordTextField
import com.example.teamup.presentation.components.PrimaryButton
import com.example.teamup.route.Routes
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun LoginScreenV5(navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // State untuk tab email/phone
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Email", "Phone Number")

    // State untuk remember me checkbox
    var rememberMe by remember { mutableStateOf(false) }

    // State untuk password visibility
    var passwordVisible by remember { mutableStateOf(false) }

    // State untuk menyimpan email/phone dan password
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Fungsi untuk proses login normal
    fun performLogin() {
        // Validasi input sebelum navigasi
        if ((selectedTab == 0 && emailOrPhone.isEmpty()) ||
            (selectedTab == 1 && emailOrPhone.isEmpty()) ||
            password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Anda bisa tambahkan logika autentikasi di sini

        // Navigasi ke dashboard setelah login sukses
        navController.navigate(Routes.Dashboard.routes) {
            popUpTo(Routes.Login.routes) {
                inclusive = true
            }
        }
    }

    // Fungsi untuk melakukan autentikasi biometrik
    fun showBiometricPrompt() {
        val fragmentActivity = context as? FragmentActivity
        if (fragmentActivity == null) {
            Toast.makeText(context, "Cannot initialize biometric authentication", Toast.LENGTH_SHORT).show()
            return
        }

        val biometricHelper = BiometricHelper(fragmentActivity)

        // Periksa apakah perangkat mendukung biometrik dan ada sidik jari terdaftar
        if (!biometricHelper.canAuthenticate()) {
            Toast.makeText(context, "Biometric authentication not available or no fingerprints enrolled", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat executor untuk BiometricPrompt
        val executor = ContextCompat.getMainExecutor(context)

        // Callback untuk hasil autentikasi biometrik
        val authCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Autentikasi berhasil, navigasi ke dashboard
                Toast.makeText(context, "Authentication successful", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.Dashboard.routes) {
                    popUpTo(Routes.Login.routes) {
                        inclusive = true
                    }
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Tampilkan pesan error
                Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Autentikasi gagal
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Inisialisasi BiometricPrompt
        val biometricPrompt = BiometricPrompt(fragmentActivity, executor, authCallback)

        // Konfigurasi prompt
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login with Fingerprint")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        // Tampilkan prompt biometrik
        biometricPrompt.authenticate(promptInfo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        // Back Button
//        IconButton(
//            onClick = { navController.popBackStack() },
//            modifier = Modifier.align(Alignment.Start)
//        ) {
//            Icon(
//                imageVector = Icons.Default.ArrowBack,
//                contentDescription = "Back"
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))

        // TeamUp Icon
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "TeamUp Logo",
                tint = DodgerBlue,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Welcome Text
        Text(
            text = "Welcome back to\nTeamUp",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Create an account or log in to explore about our app",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SoftGray2,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email/Phone Tab
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = DodgerBlue,
            indicator = { },
            divider = { }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    selectedContentColor = DodgerBlue,
                    unselectedContentColor = SoftGray2
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Email/Phone Input
        Text(
            text = if (selectedTab == 0) "Email" else "Phone Number",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = emailOrPhone,
            onValueChange = { emailOrPhone = it },
            placeholder = { Text(if (selectedTab == 0) "Email" else "Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = if (selectedTab == 0) KeyboardType.Email else KeyboardType.Phone
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        Text(
            text = "Password",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Remember Me and Forgot Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = DodgerBlue
                    )
                )
                Text(
                    text = "Remember me",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(onClick = { /* Handle forgot password */ }) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DodgerBlue,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = { performLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = DodgerBlue
            )
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Or login with
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Or login with",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SoftGray2
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Social Login Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Google Button
            IconButton(
                onClick = { /* Handle Google login */ },
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google), // Pastikan resource ini tersedia
                    contentDescription = "Login with Google",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Facebook Button
            IconButton(
                onClick = { /* Handle Facebook login */ },
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.facebook), // Pastikan resource ini tersedia
                    contentDescription = "Login with Facebook",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Fingerprint Button
            IconButton(
                onClick = { showBiometricPrompt() },  // Panggil fungsi biometric authentication
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.fingerprint), // Pastikan resource ini tersedia
                    contentDescription = "Login with Fingerprint",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Don't have an account
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(
                onClick = {
                    navController.navigate(Routes.Register.routes)
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DodgerBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}