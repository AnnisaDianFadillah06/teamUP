package com.example.teamup.presentation

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
        val padding = paddingValues
        NavHost(
            navController = navController,
            startDestination = Routes.LoginV5.routes // Ubah startDestination ke FingerprintLogin
        ) {
            composable(Routes.Login.routes) {
                LoginScreen(navController = navController)
            }
            composable(Routes.Register.routes) {
                RegisterScreen(navController = navController)
            }
//            composable(route = Routes.FingerprintLogin.routes) {
//                FingerprintLoginScreen(navController = navController)
//            }
            composable(route = Routes.LoginV5.routes) {
                LoginScreenV5(navController = navController)
            }
            composable(Routes.Dashboard.routes) {
                DashboardScreen()
            }
            composable(Routes.Competition.routes) {
                CompetitionScreen(navController = navController)
            }
            composable(Routes.AddCompetition.routes) {
                AddCompetitionScreen(navController = navController)
            }
            composable(Routes.TeamList.routes) {
                TeamListScreen(navController = navController)
            }
            composable(Routes.AddTeam.routes) {
                AddTeamScreen(navController = navController)
            }
        }
    }
}

@Preview
@Composable
fun StartSailPreview() {
    StartSail()
}