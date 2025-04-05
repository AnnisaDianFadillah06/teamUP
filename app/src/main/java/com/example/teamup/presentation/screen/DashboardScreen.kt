package com.example.teamup.presentation.screen

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.presentation.components.BottomNavigationBar
import com.example.teamup.route.Routes

@Composable
fun DashboardScreen(navController: NavHostController = rememberNavController(),  competitionViewModel: CompetitionViewModel) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(bottomBar = {
        if (currentRoute != Routes.Detail.routes && currentRoute != Routes.Cart.routes && currentRoute != Routes.Search.routes) {
            BottomNavigationBar(navController)
        }
    }) { paddingValues ->
        NavHost(navController = navController, startDestination = Routes.Home.routes) {
            composable(Routes.Home.routes) {
                HomeScreen(navController = navController, paddingValues = paddingValues)
            }
            composable(Routes.Search.routes) {
                SearchScreen(navController = navController)
            }
            composable(Routes.Profile.routes) {
                ProfileScreen(navController = navController)
            }
            composable(Routes.Competition.routes) {
                CompetitionScreen(navController = navController)
            }
            composable(Routes.Wishlist.routes) {
                WishlistScreen(navController = navController, paddingValues = paddingValues)
            }
            composable(Routes.Cart.routes) {
                CartScreen(navController = navController)
            }
            composable(Routes.MyCourse.routes) {
                MyCoursesScreen(navController = navController, paddingValues = paddingValues)
            }
            composable(Routes.Detail.routes) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")?.toInt() ?: 0
                DetailScreen(navController, courseId)
            }
//            composable(Routes.AddCompetition.routes) {
//                AddCompetitionScreen(
//                    navController = navController,
//                    viewModel = competitionViewModel
//                )
//            }
//            composable(Routes.AddCompetition.routes) {
//                AddCompetitionForm(
//                    viewModel = competitionViewModel,
//                    onSuccess = { navController.popBackStack() } // Navigasi balik setelah sukses
//                )
//            }
//            composable(Routes.CompetitionList.routes) {
//                CompetitionListScreen(navController)
//            }
//            composable(
//                Routes.Detail.routes,
//                arguments = listOf(navArgument("id") { type = NavType.IntType })
//            ) {
//                val id = it.arguments?.getInt("id") ?: 0
//                DetailScreen(navController = navController, id = id)
//            }
        }
    }
}