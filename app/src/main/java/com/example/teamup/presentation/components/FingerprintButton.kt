//package com.example.teamup.presentation.components
//
//import android.widget.Toast
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Fingerprint
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.fragment.app.FragmentActivity
//import androidx.navigation.NavController
//import com.example.teamup.common.theme.DodgerBlue
//import com.example.teamup.common.theme.White
//import com.example.teamup.route.Routes
//import com.example.teamup.common.utils.BiometricHelper
//
///**
// * Komponen tombol untuk login menggunakan biometrik (fingerprint)
// */
//@Composable
//fun FingerprintButton(
//    navController: NavController,
//    modifier: Modifier = Modifier,
//    biometricHelper: BiometricHelper? = null
//) {
//    val context = LocalContext.current
//    val helper = biometricHelper ?: BiometricHelper(context)
//
//    Button(
//        onClick = {
//            val activity = context as? FragmentActivity
//            activity?.let {
//                helper.showBiometricPrompt(
//                    activity = it,
//                    onSuccess = {
//                        // Handle successful authentication
//                        navController.navigate(Routes.Dashboard.routes) {
//                            popUpTo(Routes.Login.routes) {
//                                inclusive = true
//                            }
//                        }
//                    },
//                    onError = { errorMsg ->
//                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
//                    }
//                )
//            }
//        },
//        modifier = modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue)
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                Icons.Default.Fingerprint,
//                contentDescription = "Fingerprint",
//                modifier = Modifier.size(24.dp)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(
//                text = "Sign in with Fingerprint",
//                style = MaterialTheme.typography.bodyMedium.copy(
//                    color = White,
//                    fontWeight = FontWeight.Medium
//                )
//            )
//        }
//    }
//}