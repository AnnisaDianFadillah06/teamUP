package com.example.teamup.data.repositories

import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.sources.remote.FirebaseCompetitionDataSource

class CompetitionRepository(private val dataSource: FirebaseCompetitionDataSource) {
    suspend fun addCompetition(competition: CompetitionModel): String? {
        return dataSource.addCompetition(competition)
    }

    suspend fun getAllCompetitions(): List<CompetitionModel> {
        return dataSource.getAllCompetitions()
    }

    companion object {
        @Volatile
        private var instance: CompetitionRepository? = null

        fun getInstance(dataSource: FirebaseCompetitionDataSource): CompetitionRepository =
            instance ?: synchronized(this) {
                instance ?: CompetitionRepository(dataSource).also { instance = it }
            }
    }
}