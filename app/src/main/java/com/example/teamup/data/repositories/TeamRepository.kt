package com.example.teamup.data.repositories

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.model.UserProfile
import com.example.teamup.data.model.UserProfileData
import com.example.teamup.data.sources.remote.GoogleDriveTeamDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeamRepository private constructor(
    private val remoteDataSource: GoogleDriveTeamDataSource
) {
    private val TAG = "TeamRepository"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var initialized = false

    companion object {
        @Volatile
        private var instance: TeamRepository? = null

        fun getInstance(remoteDataSource: GoogleDriveTeamDataSource): TeamRepository {
            return instance ?: synchronized(this) {
                instance ?: TeamRepository(remoteDataSource).also { instance = it }
            }
        }
    }

    suspend fun initialize() {
        if (!initialized) {
            remoteDataSource.initialize()
            initialized = true
        }
    }

    fun isInitialized(): Boolean = initialized

    suspend fun getAllTeams(): List<TeamModel> {
        if (!initialized) initialize()
        return remoteDataSource.getAllTeams()
    }

    suspend fun getTeamById(teamId: String): TeamModel? {
        if (!initialized) initialize()
        return remoteDataSource.getTeamById(teamId)
    }

    suspend fun addTeam(team: TeamModel, imageUri: Uri? = null): String? {
        if (!initialized) initialize()
        return remoteDataSource.addTeam(team, imageUri)
    }

    suspend fun joinTeam(teamId: String, userId: String = auth.currentUser?.uid ?: ""): Boolean {
        if (!initialized) initialize()
        if (userId.isEmpty()) return false
        return remoteDataSource.joinTeam(teamId, userId)
    }

    suspend fun leaveTeam(teamId: String, userId: String = auth.currentUser?.uid ?: ""): Boolean {
        if (!initialized) initialize()
        if (userId.isEmpty()) return false
        return remoteDataSource.leaveTeam(teamId, userId)
    }

    suspend fun deleteTeam(teamId: String): Boolean {
        if (!initialized) initialize()
        return remoteDataSource.deleteTeam(teamId)
    }

    suspend fun getTeamsByUserId(userId: String = auth.currentUser?.uid ?: ""): List<TeamModel> {
        if (!initialized) initialize()
        if (userId.isEmpty()) return emptyList()
        return remoteDataSource.getTeamsByUserId(userId)
    }

    suspend fun updateTeamPhoto(teamId: String, imageUri: Uri): Boolean {
        if (!initialized) initialize()
        try {
            // First get the team to make sure it exists
            val team = getTeamById(teamId) ?: return false

            // Since driveService is private in GoogleDriveTeamDataSource, we'll use the addTeam method
            // which has logic for uploading images, and then update the existing team document

            // Create a temporary team with the same data
            val tempTeam = team.copy()

            // Use the data source to handle the image upload, but don't create a new team
            val tempId = remoteDataSource.addTeam(tempTeam, imageUri)
            if (tempId != null) {
                val tempTeam = remoteDataSource.getTeamById(tempId)
                if (tempTeam != null) {
                    val fileId = tempTeam.driveFileId
                    val imageUrl = tempTeam.imageUrl

                    // Delete the temporary team
                    remoteDataSource.deleteTeam(tempId)

                    // Delete old image if it exists
                    if (!team.driveFileId.isNullOrEmpty() && team.driveFileId != fileId) {
                        // We can't directly access remoteDataSource.driveService,
                        // so we'll let the deleteTeam method handle this
                        // This creates a temporary team with the old file ID
                        val tempTeamWithOldFile = team.copy(id = "temp_for_delete_" + System.currentTimeMillis())
                        val tempDocId = firestore.collection("teams").add(mapOf(
                            "name" to tempTeamWithOldFile.name,
                            "description" to tempTeamWithOldFile.description,
                            "driveFileId" to team.driveFileId
                        )).await().id

                        // Delete the temporary team, which will delete the old file too
                        remoteDataSource.deleteTeam(tempDocId)
                    }

                    // Update team in Firestore
                    firestore.collection("teams").document(teamId).update(
                        mapOf(
                            "imageUrl" to imageUrl,
                            "driveFileId" to fileId
                        )
                    ).await()

                    return true
                }
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error updating team photo: ${e.message}")
            return false
        }
    }

    suspend fun getUserProfile(userId: String): UserProfileData? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()

            if (document.exists()) {
                // Konversi document ke UserProfileData
                val data = document.data
                UserProfileData(
                    userId = data?.get("userId") as? String ?: userId,
                    fullName = data?.get("fullName") as? String ?: "",
                    username = data?.get("username") as? String ?: "",
                    email = data?.get("email") as? String ?: "",
                    phone = data?.get("phone") as? String ?: "",
                    university = data?.get("university") as? String ?: "",
                    major = data?.get("major") as? String ?: "",
                    skills = (data?.get("skills") as? List<String>) ?: emptyList(),
                    profilePictureUrl = data?.get("profilePictureUrl") as? String ?: "",
                    profileCompleted = data?.get("profileCompleted") as? Boolean ?: false
                )
            } else {
                Log.w("TeamRepository", "User document not found for ID: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e("TeamRepository", "Error fetching user profile for $userId: ${e.message}", e)
            null
        }
    }
}