package com.example.teamup.data.repositories

import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.sources.remote.FirebaseTeamDataSource

class TeamRepository(private val dataSource: FirebaseTeamDataSource) {

    suspend fun addTeam(team: TeamModel): String? {
        return dataSource.addTeam(team)
    }

    suspend fun getAllTeams(): List<TeamModel> {
        return dataSource.getAllTeams()
    }

    companion object {
        @Volatile
        private var instance: TeamRepository? = null

        fun getInstance(dataSource: FirebaseTeamDataSource): TeamRepository =
            instance ?: synchronized(this) {
                instance ?: TeamRepository(dataSource).also { instance = it }
            }
    }
}