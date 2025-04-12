package com.example.teamup.route

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.teamup.presentation.screen.ForgotPasswordScreen
import com.example.teamup.presentation.screen.LoginScreenV5
import com.example.teamup.presentation.screen.ProfileScreen
import com.example.teamup.presentation.screen.RegisterScreen
import com.example.teamup.presentation.screen.RegisterSuccessScreen
import com.example.teamup.presentation.screen.ResetPasswordScreen
import com.example.teamup.presentation.screen.VerificationScreen

@Composable
fun NavGraph(startDestination: String = Routes.Register.routes) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Register.routes) { RegisterScreen(navController = navController) }
        composable(Routes.LoginV5.routes) { LoginScreenV5(navController = navController) }
        composable(Routes.ForgotPassword.routes) { ForgotPasswordScreen(navController = navController) }
        composable(Routes.ResetPassword.routes) { ResetPasswordScreen(navController = navController) }

        composable(Routes.Verification.routes) { VerificationScreen(navController = navController) }
        composable(Routes.RegisterSuccess.routes) { RegisterSuccessScreen(navController = navController) }
        composable(Routes.Profile.routes) { ProfileScreen(navController = navController) }

        // Tambahkan route lain jika diperlukan
    }
}

