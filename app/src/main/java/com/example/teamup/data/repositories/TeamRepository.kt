package com.example.teamup.data.repositories

import android.net.Uri
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.sources.remote.GoogleDriveTeamDataSource

interface TeamRepository {
    suspend fun initialize()
    fun isInitialized(): Boolean

    // Team CRUD
    suspend fun getAllTeams(): List<TeamModel>
    suspend fun getTeamById(teamId: String): TeamModel?
    suspend fun addTeam(team: TeamModel, imageUri: Uri? = null): String?
    suspend fun deleteTeam(teamId: String): Boolean
    suspend fun updateTeamPhoto(teamId: String, imageUri: Uri): Boolean

    // Team Membership
    suspend fun joinTeam(teamId: String, userId: String): Boolean
    suspend fun leaveTeam(teamId: String, userId: String): Boolean
    suspend fun getTeamsByUserId(userId: String): List<TeamModel>

    // Member Management (BARU - untuk join request flow)
    suspend fun addMemberToTeam(teamId: String, userId: String): Result<Unit>
    suspend fun removeMemberFromTeam(teamId: String, userId: String): Result<Unit>
    suspend fun getTeamMembers(teamId: String): Result<List<String>>
    suspend fun isUserMemberOfTeam(teamId: String, userId: String): Result<Boolean>

    // User Profile
    suspend fun getUserProfile(userId: String): UserProfileData?


}