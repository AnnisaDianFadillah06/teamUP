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
                category = "KMIPN - Cipta Inovasi",
                avatarResId = R.drawable.captain_icon,
                isJoined = true,
                isFull = false,
                description = "KMIPN - Cipta Inovasi",
                memberCount = 2,
                maxMembers = 5
            ),
            TeamModel(
                id = "team2",
                name = "Garuda",
                category = "KMIPN - Smart City",
                avatarResId = R.drawable.captain_icon,
                isJoined = false,
                isFull = false,
                description = "KMIPN - Smart City",
                memberCount = 3,
                maxMembers = 5
            ),
            TeamModel(
                id = "team3",
                name = "Brawijaya",
                category = "Gemastik - IoT",
                avatarResId = R.drawable.captain_icon,
                isJoined = false,
                isFull = true,
                description = "Gemastik - IoT",
                memberCount = 5,
                maxMembers = 5
            ),
        )
        emit(teams)
    }
}