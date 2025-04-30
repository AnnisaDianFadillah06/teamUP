package com.example.teamup.presentation.screen

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.teamup.data.viewmodels.JoinTeamViewModel
import com.example.teamup.di.ViewModelJoinFactory
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.presentation.components.BottomNavigationBar
import com.example.teamup.presentation.screen.profile.ProfileScreen
import com.example.teamup.route.Routes


@Composable
fun DashboardScreen(navController: NavHostController = rememberNavController(),  competitionViewModel: CompetitionViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                InviteSelectMemberScreen(navController = navController)
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
                    viewModel = joinTeamViewModel
                )
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