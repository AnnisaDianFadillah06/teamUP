package com.example.teamup.presentation.screen

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.teamup.common.utils.BackPressHandler
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.JoinTeamViewModel
import com.example.teamup.data.viewmodels.SharedMemberViewModel
import com.example.teamup.di.Injection
import com.example.teamup.di.ViewModelJoinFactory
import com.example.teamup.presentation.components.BottomNavigationBar
import com.example.teamup.presentation.screen.competition.CompetitionDetailScreen // ✅ Use Dynamic version
import com.example.teamup.presentation.screen.competition.CompetitionScreen
import com.example.teamup.presentation.screen.profile.ProfileScreen
import com.example.teamup.presentation.screen.profile.ProfileSettingsScreen
import com.example.teamup.route.Routes

@Composable
fun DashboardScreen(navController: NavHostController = rememberNavController(), competitionViewModel: CompetitionViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val sharedMemberViewModel: SharedMemberViewModel = viewModel()

    BackPressHandler(navController)

    Scaffold(bottomBar = {
        if (currentRoute != "draft_invitation/{selectedIds}" &&
            currentRoute != Routes.Invite.routes &&
            currentRoute != Routes.InviteSelect.routes &&
            currentRoute != Routes.Detail.routes &&
            currentRoute != Routes.ChatGroup.routes &&
            currentRoute != Routes.FormAddTeam.routes &&
            currentRoute != Routes.Cart.routes &&
            currentRoute != Routes.Search.routes &&
            currentRoute != "competition_detail/{competitionId}") {
            BottomNavigationBar(navController)
        }
    }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.HomeV5.routes
        ) {
            composable(Routes.HomeV5.routes) {
                HomeScreenV5(
                    navController = navController,
                    paddingValues = paddingValues,
                    competitionViewModel = competitionViewModel,
                    onHomeClick = {
                        navController.navigate(Routes.Home.routes) {
                            popUpTo(Routes.Home.routes) { inclusive = true }
                        }
                    }
                )
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

            // ✅ Fixed CompetitionDetail composable
            composable(
                route = Routes.CompetitionDetail.routes,
                arguments = listOf(
                    navArgument("competitionId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val competitionId = backStackEntry.arguments?.getString("competitionId") ?: ""

                // ✅ Use DynamicCompetitionDetailScreen with correct parameters
                CompetitionDetailScreen(
                    navController = navController,
                    competitionId = competitionId,
                    competitionViewModel = competitionViewModel,
                )
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

            composable(Routes.TeamManagement.routes) {
                TeamManagementScreen(navController = navController)
            }
            composable(Routes.FormAddTeam.routes) {
                FormCreateTeamScreen(navController = navController)
            }
            composable(Routes.Invite.routes) {
                InviteMemberScreen(navController = navController)
            }
            composable(Routes.InviteSelect.routes) {
                InviteSelectMemberScreen(
                    navController = navController,
                    sharedViewModel = sharedMemberViewModel
                )
            }

            composable(Routes.DraftSelectMember.routes) {
                DraftInviteSelectMemberScreen(
                    navController = navController,
                    sharedViewModel = sharedMemberViewModel
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

            // ✅ Remove duplicate Profile composable (it's already defined above)
            composable(Routes.ProfileSettings.routes) {
                ProfileSettingsScreen(navController)
            }
        }
    }
}