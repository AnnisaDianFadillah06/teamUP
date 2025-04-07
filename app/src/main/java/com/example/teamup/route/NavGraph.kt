package com.example.teamup.route

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.teamup.presentation.screen.*

@Composable
fun NavGraph(startDestination: String = Routes.Register.routes) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Register.routes) { RegisterScreen(navController = navController) }
        composable(Routes.Login.routes) { LoginScreen(navController = navController) }
        composable(Routes.ForgotPassword.routes) { ForgotPasswordScreen(navController = navController) }
        composable(Routes.ResetPassword.routes) { ResetPasswordScreen(navController = navController) }

        composable(Routes.Verification.routes) { VerificationScreen(navController = navController) }
        composable(Routes.RegisterSuccess.routes) { RegisterSuccessScreen(navController = navController) }
        composable(Routes.Profile.routes) { ProfileScreen(navController = navController) }

        // Tambahkan route lain jika diperlukan
    }
}

