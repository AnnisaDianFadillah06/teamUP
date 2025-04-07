package com.example.teamup.presentation.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.utils.BiometricAuthUtil
import com.example.teamup.common.utils.SessionManager
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreenV5(navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val sharedPrefs = context.getSharedPreferences("teamup_prefs", Context.MODE_PRIVATE)

    // Check if biometric login is available using the utility class
    val isBiometricAvailable = remember { BiometricAuthUtil.isBiometricAvailable(context) }
    val hasSavedCredentials = remember { BiometricAuthUtil.hasSavedCredentials(context) }

    // Loading state
    var isLoading by remember { mutableStateOf(false) }

    // State untuk tab email/phone
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Email", "Phone Number")

    // State untuk remember me checkbox
    var rememberMe by remember { mutableStateOf(false) }

    // State untuk password visibility
    var passwordVisible by remember { mutableStateOf(false) }

    // State untuk menyimpan email/phone dan password
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Fungsi untuk proses login dengan Firebase
    fun performLogin() {
        // Validasi input sebelum login
        if ((selectedTab == 0 && emailOrPhone.isEmpty()) ||
            (selectedTab == 1 && emailOrPhone.isEmpty()) ||
            password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        // Login dengan Firebase Authentication
        if (selectedTab == 0) {
            // Login with email
            auth.signInWithEmailAndPassword(emailOrPhone, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        // Login berhasil
                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()

                        // Simpan kredensial jika "Remember me" dicentang
                        if (rememberMe) {
                            val editor = sharedPrefs.edit()
                            editor.putString("saved_email", emailOrPhone)
                            editor.putString("saved_password", password)
                            editor.apply()
                        }

                        // Set user as logged in in SessionManager
                        SessionManager.setLoggedIn(context, true)

                        // Navigasi ke dashboard
                        navController.navigate(Routes.Dashboard.routes) {
                            popUpTo(Routes.LoginV5.routes) {
                                inclusive = true
                            }
                        }
                    } else {
                        // Login gagal
                        Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Login with phone (you would need to implement phone auth with Firebase)
            Toast.makeText(context, "Phone authentication not implemented yet", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    // Function to handle fingerprint login - updated to use BiometricAuthUtil
    fun handleFingerprintLogin() {
        if (!isBiometricAvailable) {
            Toast.makeText(context, "Biometric authentication is not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        // Hapus pengecekan hasSavedCredentials untuk memungkinkan quick login langsung

        // Use the enhanced BiometricAuthUtil for quick login
        fragmentActivity?.let {
            BiometricAuthUtil.performQuickLogin(
                activity = it,
                navController = navController,
                onSuccess = {
                    // Set user as logged in in SessionManager
                    SessionManager.setLoggedIn(context, true)
                },
                onError = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            )
        } ?: run {
            Toast.makeText(context, "This feature requires a FragmentActivity context", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
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
                ),
                enabled = !isLoading
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
                        painter = painterResource(id = R.drawable.google),
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
                        painter = painterResource(id = R.drawable.facebook),
                        contentDescription = "Login with Facebook",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Fingerprint Button - Now using our enhanced utility function
                IconButton(
                    onClick = { handleFingerprintLogin() },
                    modifier = Modifier
                        .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.fingerprint),
                        contentDescription = "Login with Fingerprint",
                        tint = if (isBiometricAvailable && hasSavedCredentials) Color.Unspecified else Color.Gray,
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

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DodgerBlue)
            }
        }
    }
}