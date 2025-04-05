package com.example.teamup.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.presentation.screen.DashboardScreen
import com.example.teamup.presentation.screen.LoginScreen
import com.example.teamup.presentation.screen.RegisterScreen
import com.example.teamup.route.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartSail(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    competitionViewModel: CompetitionViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold { paddingValues ->
        val padding = paddingValues
        NavHost(navController = navController, startDestination = Routes.Login.routes) {
            composable(Routes.Login.routes) {
                LoginScreen(navController = navController)
            }
            composable(Routes.Register.routes) {
                RegisterScreen(navController = navController)
            }
            composable(Routes.Dashboard.routes) {
                DashboardScreen(competitionViewModel = competitionViewModel)
            }
        }
    }
}

@Preview
@Composable
fun StartSailPreview() {
    val fakeViewModel = viewModel<CompetitionViewModel>(
        factory = CompetitionViewModelFactory(
            Injection.provideCompetitionRepository()
        )
    )
    StartSail(competitionViewModel = fakeViewModel)
}