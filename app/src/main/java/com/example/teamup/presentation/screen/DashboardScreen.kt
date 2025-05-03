package com.example.teamup.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.teamup.R
import com.example.teamup.common.utils.BackPressHandler
import com.example.teamup.data.model.ProfileModel
import com.example.teamup.data.repositories.NotificationRepository
import com.example.teamup.data.sources.remote.FirebaseNotificationDataSource
import com.example.teamup.data.viewmodels.JoinTeamViewModel
import com.example.teamup.di.ViewModelJoinFactory
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.NotificationViewModel
import com.example.teamup.di.Injection
import com.example.teamup.presentation.components.BottomNavigationBar
import com.example.teamup.presentation.screen.profile.ProfileScreen
import com.example.teamup.route.Routes
import com.example.teamup.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController = rememberNavController()) {

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
            composable(Routes.Wishlist.routes) {
                WishlistScreen(navController = navController, paddingValues = paddingValues)
            }
            composable(Routes.Cart.routes) {
                CartScreen(navController = navController)
            }
            composable(Routes.MyCourse.routes) {
                MyCoursesScreen(navController = navController, paddingValues = paddingValues)
            }
            composable(
                route = "draft_invitation/{selectedIds}",
                arguments = listOf(navArgument("selectedIds") { type = NavType.StringType })
            ) { backStackEntry ->
                val selectedIds = backStackEntry.arguments
                    ?.getString("selectedIds")
                    ?.split(",") ?: emptyList()

                // Ambil semua data member dari tempat yang bisa diakses, misalnya di sini kamu bisa inject dummy list atau viewModel jika masih dalam scope
                val allMembers = listOf(
                    ProfileModel("1", "Annisa Dian", "annisadian@gmail.com", R.drawable.captain_icon, "Universitas Indonesia", "Informatika", listOf("UI/UX", "Mobile")),
                    ProfileModel("2", "Annisa Dian", "annisa.dian@gmail.com", R.drawable.captain_icon, "Universitas Indonesia", "Elektro", listOf("Mobile", "Backend")),
                    ProfileModel("3", "Annisa Dian", "dian.annisa@gmail.com", R.drawable.captain_icon, "Universitas Gadjah Mada", "Informatika", listOf("Frontend", "UI/UX")),
                    ProfileModel("4", "Annisa Dian", "annisa.d@gmail.com", R.drawable.captain_icon, "Institut Teknologi Bandung", "Mesin", listOf("Backend", "Database")),
                    ProfileModel("5", "Annisa Dian", "ad.annisa@gmail.com", R.drawable.captain_icon, "Universitas Brawijaya", "Elektro", listOf("Mobile", "Database")),
                    ProfileModel("6", "Annisa Dian", "annisa.dian01@gmail.com", R.drawable.captain_icon, "Universitas Indonesia", "Informatika", listOf("UI/UX", "Frontend")),
                )

                val selectedMembers = remember(selectedIds) {
                    allMembers.filter { it.id in selectedIds }
                }

                DraftInviteSelectMemberScreen(
                    navController = navController,
                    selectedMembers = selectedMembers
                )
            }
            composable(Routes.JoinTeam.routes) {
                val viewModelFactory = ViewModelJoinFactory.getInstance()
                val joinTeamViewModel: JoinTeamViewModel = viewModel(factory = viewModelFactory)

                JoinTeamScreen(
                    navController = navController,
                    viewModel = joinTeamViewModel,
                )
            }
            composable(Routes.Notifications.routes) {
                val notificationViewModel = Injection.provideNotificationViewModel()

                ListNotificationScreen(
                    navController = navController,
                    viewModel = notificationViewModel
                )
            }
            composable(Routes.TeamListCategory.routes) {
                TeamListScreen(navController = navController)
            }
            composable(
                route = Routes.TeamDetailGrup.routes,
                arguments = listOf(
                    navArgument("teamId") { type = NavType.StringType },
                    navArgument("isJoined") { type = NavType.BoolType },
                    navArgument("isFull") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                val isJoined = backStackEntry.arguments?.getBoolean("isJoined") ?: false
                val isFull = backStackEntry.arguments?.getBoolean("isFull") ?: false

                DetailTeamScreen(
                    navController = navController,
                    teamId = teamId,
                    isJoined = isJoined,
                    isFull = isFull
                )
            }
            composable(
                Routes.ChatGroup.routes,
                arguments = listOf(
                    navArgument("teamId") { type = NavType.StringType },
                    navArgument("teamName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
                ChatGroupScreen(
                    navController = navController,
                    teamId = teamId,
                    teamName = teamName
                )
            }
        }
    }
}