package com.example.teamup.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.teamup.common.utils.BackPressHandler
import com.example.teamup.common.utils.SessionManager
import com.example.teamup.presentation.screen.AddTeamScreen
import com.example.teamup.presentation.screen.DashboardScreen
import com.example.teamup.presentation.screen.FingerprintLoginScreen
import com.example.teamup.presentation.screen.LoginScreen
import com.example.teamup.presentation.screen.LoginScreenV5
import com.example.teamup.presentation.screen.RegisterScreen
import com.example.teamup.presentation.screen.TeamListScreen
import com.example.teamup.route.Routes

@Composable
fun StartSail(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    var checkingAuth by remember { mutableStateOf(true) }

    // Tentukan start destination berdasarkan status login
    val startDestination = remember {
        mutableStateOf(
            if (SessionManager.isLoggedIn(context)) Routes.Dashboard.routes else Routes.LoginV5.routes
        )
    }

    // Check user login status
    LaunchedEffect(key1 = true) {
        checkingAuth = false
    }

    // Implementasi BackPressHandler
    BackPressHandler(navController)

    Scaffold { paddingValues ->
        val padding = paddingValues

        if (!checkingAuth) {
            NavHost(
                navController = navController,
                startDestination = startDestination.value
            ) {
                // Authentication graph
                composable(Routes.Login.routes) {
                    LoginScreen(navController = navController)
                }
                composable(Routes.Register.routes) {
                    RegisterScreen(navController = navController)
                }
                composable(route = Routes.FingerprintLogin.routes) {
                    FingerprintLoginScreen(navController = navController)
                }
                composable(route = Routes.LoginV5.routes) {
                    LoginScreenV5(navController = navController)
                }

                // Dashboard Screen
                composable(Routes.Dashboard.routes) {
                    DashboardScreen()
                }

                // Team management routes
                composable(Routes.TeamList.routes) {
                    TeamListScreen(navController = navController)
                }
                composable(Routes.AddTeam.routes) {
                    AddTeamScreen(navController = navController)
                }
            }
        } else {
            // Show loading screen or splash screen while checking auth
            Box(modifier = Modifier.fillMaxSize()) {
                // You can show your splash screen or loading indicator here
            }
        }
    }
}

@Preview
@Composable
fun StartSailPreview() {
    StartSail()
}