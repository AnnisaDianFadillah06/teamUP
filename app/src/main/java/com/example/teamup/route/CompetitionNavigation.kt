package com.example.teamup.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.teamup.data.viewmodels.CabangLombaViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.presentation.screen.competition.CompetitionEditStatusScreen

// Navigation routes
object CompetitionRoutes {
    const val COMPETITION_LIST = "competition_list"
    const val COMPETITION_ADD = "competition_add"
    const val COMPETITION_EDIT_STATUS = "competition_edit_status/{competitionId}"

    // Helper function to create route with parameters
    fun competitionEditStatus(competitionId: String) = "competition_edit_status/$competitionId"
}

// Navigation extension function
fun NavGraphBuilder.competitionGraph(
    navController: NavController,
    competitionViewModel: CompetitionViewModel,
    cabangLombaViewModel: CabangLombaViewModel
) {
//    // List screen
//    composable(CompetitionRoutes.COMPETITION_LIST) {
//        CompetitionListScreen(
//            viewModel = competitionViewModel,
//            cabangLombaViewModel = cabangLombaViewModel,
//            onAddClick = { navController.navigate(CompetitionRoutes.COMPETITION_ADD) },
//            onEditStatusClick = { competitionId ->
//                navController.navigate(CompetitionRoutes.competitionEditStatus(competitionId))
//            }
//        )
//    }

    // Edit status screen
    composable(
        route = CompetitionRoutes.COMPETITION_EDIT_STATUS,
        arguments = listOf(
            navArgument("competitionId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val competitionId = backStackEntry.arguments?.getString("competitionId") ?: ""
        CompetitionEditStatusScreen(
            competitionId = competitionId,
            viewModel = competitionViewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Add other routes as needed...
}