package com.example.teamup.presentation.screen

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.route.Routes

@Composable
fun LoginScreenV5(navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity

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

    // State untuk dialog fingerprint
    var showFingerprintDialog by remember { mutableStateOf(false) }

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

    // Fungsi untuk membuka dialog fingerprint
    fun openFingerprintDialog() {
        if (fragmentActivity == null) {
            Toast.makeText(context, "This feature requires a FragmentActivity context", Toast.LENGTH_SHORT).show()
            return
        }

        showFingerprintDialog = true
    }

    // Observasi lifecycle untuk memastikan dialog tidak muncul saat aplikasi di background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                showFingerprintDialog = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Dialog Fingerprint Authentication
    if (showFingerprintDialog && fragmentActivity != null) {
        FingerprintAuthDialog(
            onAuthSuccess = {
                showFingerprintDialog = false
                Toast.makeText(context, "Authentication successful", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.Dashboard.routes) {
                    popUpTo(Routes.Login.routes) {
                        inclusive = true
                    }
                }
            },
            onAuthFailed = {
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
            },
            onAuthError = { errorMessage ->
                Toast.makeText(context, "Authentication error: $errorMessage", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                showFingerprintDialog = false
            }
        )
    }

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
                onClick = { openFingerprintDialog() },  // Panggil fungsi untuk membuka dialog fingerprint
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