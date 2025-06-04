package com.example.teamup.presentation.screen

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
import com.example.teamup.data.viewmodels.SharedMemberViewModel
import com.example.teamup.di.Injection
import com.example.teamup.presentation.components.BottomNavigationBar
import com.example.teamup.presentation.screen.profile.ProfileScreen
import com.example.teamup.route.Routes


@Composable
fun DashboardScreen(navController: NavHostController = rememberNavController(),  competitionViewModel: CompetitionViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val sharedMemberViewModel: SharedMemberViewModel = viewModel()

    // Tambahkan BackPressHandler
    BackPressHandler(navController)

    Scaffold(bottomBar = {
        if (currentRoute != "draft_invitation/{selectedIds}" && currentRoute != Routes.Invite.routes && currentRoute != Routes.InviteSelect.routes && currentRoute != Routes.Detail.routes && currentRoute != Routes.ChatGroup.routes && currentRoute != Routes.FormAddTeam.routes && currentRoute != Routes.Cart.routes && currentRoute != Routes.Search.routes) {
            BottomNavigationBar(navController)
        }
    }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Profile.routes
        ) {
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

// Update composable untuk draft screen - hapus parameter selectedIds
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
        }
    }
}