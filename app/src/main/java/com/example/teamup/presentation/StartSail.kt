package com.example.teamup.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.teamup.common.utils.BackPressHandler
import com.example.teamup.common.utils.SessionManager
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.presentation.screen.AddTeamScreen
import com.example.teamup.presentation.screen.DashboardScreen
import com.example.teamup.presentation.screen.FingerprintLoginScreen
import com.example.teamup.presentation.screen.ForgotPasswordScreen
import com.example.teamup.presentation.screen.LoginScreenV5
import com.example.teamup.presentation.screen.register.RegisterScreen
import com.example.teamup.presentation.screen.register.RegisterSuccessScreen
import com.example.teamup.presentation.screen.ResetPasswordScreen
import com.example.teamup.presentation.screen.SplashScreen
import com.example.teamup.presentation.screen.TeamListScreen
import com.example.teamup.presentation.screen.profile.CompleteProfileScreen
import com.example.teamup.presentation.screen.register.CekEmailScreen
import com.example.teamup.presentation.screen.register.VerificationScreen
import com.example.teamup.data.viewmodels.user.ProfileViewModel
import com.example.teamup.route.Routes

@Composable
fun StartSail(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    competitionViewModel: CompetitionViewModel
) {
    val profileViewModel: ProfileViewModel = viewModel()

    BackPressHandler(navController)

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.SplashScreen.routes,
            modifier = Modifier.padding(paddingValues) // ✅ Gunakan paddingValues di sini
        ) {
            composable(Routes.SplashScreen.routes) {
                val context = LocalContext.current
                SplashScreen {
                    // Navigasi ke screen berikutnya setelah animasi selesai
                    val destination = if (SessionManager.isLoggedIn(context)) {
                        Routes.Dashboard.routes
                    } else {
                        Routes.LoginV5.routes
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.SplashScreen.routes) { inclusive = true }
                    }
                }
            }
            composable(Routes.Register.routes) {
                RegisterScreen(navController = navController)
            }
            composable(Routes.FingerprintLogin.routes) {
                FingerprintLoginScreen(navController = navController)
            }
            composable(Routes.LoginV5.routes) {
                LoginScreenV5(navController = navController)
            }

            // Dashboard & Teams
            composable(Routes.Dashboard.routes) {
                DashboardScreen(competitionViewModel = competitionViewModel)
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


            composable(Routes.CompleteProfile.routes) {
                CompleteProfileScreen(navController, profileViewModel)
            }


            composable(
                route = Routes.CekEmail.routes,
                arguments = listOf(
                    navArgument("email") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                CekEmailScreen(navController, email = email)
            }

        }
    }
}

@Preview
@Composable
fun StartSailPreview() {
    val fakeViewModel = viewModel<CompetitionViewModel>(
        factory = CompetitionViewModelFactory(
            Injection.provideCompetitionRepository(), Injection.provideCabangLombaRepository()
        )
    )
    StartSail(competitionViewModel = fakeViewModel)
}