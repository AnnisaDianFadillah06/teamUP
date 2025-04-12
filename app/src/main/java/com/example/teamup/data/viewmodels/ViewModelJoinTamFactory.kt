package com.example.teamup.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.teamup.data.repositories.CompetitionRepositoryDummy
import com.example.teamup.data.repositories.TeamRepository
import com.example.teamup.data.sources.remote.FirebaseTeamDataSource
import com.example.teamup.data.viewmodels.JoinTeamViewModel

class ViewModelJoinFactory(
    private val teamRepository: TeamRepository,
    private val competitionRepository: CompetitionRepositoryDummy
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JoinTeamViewModel::class.java)) {
            return JoinTeamViewModel(teamRepository, competitionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    companion object {
        @Volatile
        private var instance: ViewModelJoinFactory? = null

        fun getInstance(): ViewModelJoinFactory {
            return instance ?: synchronized(this) {
                val firebaseTeamDataSource = FirebaseTeamDataSource()
                val teamRepository = TeamRepository.getInstance(firebaseTeamDataSource)
                val competitionRepository = CompetitionRepositoryDummy()

                instance ?: ViewModelJoinFactory(
                    teamRepository,
                    competitionRepository
                ).also { instance = it }
            }
        }
    }
}