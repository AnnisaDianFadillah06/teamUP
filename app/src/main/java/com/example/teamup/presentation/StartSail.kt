package com.example.teamup

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.teamup.presentation.screen.*
import com.example.teamup.route.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartSail(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Login.routes,
            modifier = modifier
        ) {
            composable(Routes.Login.routes) {
                LoginScreen(navController = navController)
            }
            composable(Routes.Register.routes) {
                RegisterScreen(navController = navController)
            }
            composable(Routes.Dashboard.routes) {
                DashboardScreen()
            }
            composable(Routes.TeamList.routes) {
                TeamListScreen(navController = navController)
            }
            composable(Routes.AddTeam.routes) {
                AddTeamScreen(navController = navController)
            }
            composable(Routes.Verification.routes) {
                VerificationScreen(navController = navController)
            }
            composable(Routes.RegisterSuccess.routes) {
                RegisterSuccessScreen(navController = navController)
            }
            composable(Routes.ForgotPassword.routes) {
                ForgotPasswordScreen(navController = navController)
            }
            composable(Routes.ResetPassword.routes) {
                ResetPasswordScreen(navController = navController)
            }
            composable(Routes.Profile.routes) {
                ProfileScreen(navController = navController)
            }
            // Tambahkan destination lain sesuai kebutuhan
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartSailPreview() {
    StartSail()
}
