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

    suspend fun getTeamById(teamId: String): TeamModel? {
        if (!initialized) {
            initialize()
        }
        return remoteDataSource.getTeamById(teamId)
    }

    suspend fun joinTeam(teamId: String, userId: String): Boolean {
        if (!initialized) {
            initialize()
        }
        return remoteDataSource.joinTeam(teamId, userId)
    }

    suspend fun leaveTeam(teamId: String, userId: String): Boolean {
        if (!initialized) {
            initialize()
        }
        return remoteDataSource.leaveTeam(teamId, userId)
    }

    suspend fun deleteTeam(teamId: String): Boolean {
        if (!initialized) {
            initialize()
        }
        return remoteDataSource.deleteTeam(teamId)
    }

    suspend fun getTeamsByUserId(userId: String): List<TeamModel> {
        if (!initialized) {
            initialize()
        }
        return remoteDataSource.getTeamsByUserId(userId)
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