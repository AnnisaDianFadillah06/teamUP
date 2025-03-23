package com.example.teamup.data.repositories

import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.model.TeamModel
import com.example.teamup.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CompetitionRepository {
    fun getCategories(): Flow<List<CompetitionModel>> = flow {
        // In a real app, this would come from an API or database
        val categories = listOf(
            CompetitionModel(
                id = "kmipn",
                name = "KMIPN",
                iconResId = R.drawable.google,
                teamCount = 78
            ),
            CompetitionModel(
                id = "pkm",
                name = "PKM",
                iconResId = R.drawable.dev_icon,
                teamCount = 708
            ),
            CompetitionModel(
                id = "kri",
                name = "KRI",
                iconResId = R.drawable.earth_icon,
                teamCount = 5
            ),
            CompetitionModel(
                id = "gemastik",
                name = "Gemastik",
                iconResId = R.drawable.facebook,
                teamCount = 200
            )
        )
        emit(categories)
    }

    fun getPopularTeams(): Flow<List<TeamModel>> = flow {
        // In a real app, this would come from an API or database
        val teams = listOf(
            TeamModel(
                id = "team1",
                name = "Al-Fath",
                category = "KMIPN",
                avatarResId = R.drawable.captain_icon
            ),
            TeamModel(
                id = "team2",
                name = "Al-Fath",
                category = "KMIPN",
                avatarResId = R.drawable.captain_icon
            ),
            TeamModel(
                id = "team3",
                name = "Al-Fath",
                category = "KMIPN",
                avatarResId = R.drawable.captain_icon
            ),
            TeamModel(
                id = "team4",
                name = "Al-Fath",
                category = "KMIPN",
                avatarResId = R.drawable.captain_icon
            ),
            TeamModel(
                id = "team5",
                name = "Al-Fath",
                category = "KMIPN",
                avatarResId = R.drawable.captain_icon
            )
        )
        emit(teams)
    }
}