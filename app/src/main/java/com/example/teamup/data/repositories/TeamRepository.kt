package com.example.teamup.data.repositories

import android.net.Uri
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.sources.remote.GoogleDriveTeamDataSource

class TeamRepository private constructor(
    private val remoteDataSource: GoogleDriveTeamDataSource
) {

    private var initialized = false

    suspend fun initialize() {
        if (!initialized) {
            remoteDataSource.initialize()
            initialized = true
        }
    }

    fun isInitialized(): Boolean = initialized

    suspend fun addTeam(team: TeamModel, imageUri: Uri? = null): String? {
        if (!initialized) {
            initialize()
        }
        return remoteDataSource.addTeam(team, imageUri)
    }

    suspend fun getAllTeams(): List<TeamModel> {
        if (!initialized) {
            initialize()
        }
        return remoteDataSource.getAllTeams()
    }


    companion object {
        @Volatile
        private var instance: TeamRepository? = null

        fun getInstance(remoteDataSource: GoogleDriveTeamDataSource): TeamRepository {
            return instance ?: synchronized(this) {
                instance ?: TeamRepository(remoteDataSource).also { instance = it }
            }
        }
    }
}