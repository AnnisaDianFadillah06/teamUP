package com.example.teamup.data.repositories

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.sources.remote.GoogleDriveTeamDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeamRepositoryImpl private constructor(
    private val remoteDataSource: GoogleDriveTeamDataSource
) : TeamRepository { // IMPLEMENT interface

    private val TAG = "TeamRepositoryImpl"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // FIX: Tambah reference ke collection teams
    private val teamsCollection = firestore.collection("teams")

    private var initialized = false

    companion object {
        @Volatile
        private var instance: TeamRepositoryImpl? = null

        fun getInstance(remoteDataSource: GoogleDriveTeamDataSource): TeamRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: TeamRepositoryImpl(remoteDataSource).also { instance = it }
            }
        }
    }

    override suspend fun initialize() {
        if (!initialized) {
            remoteDataSource.initialize()
            initialized = true
        }
    }

    override fun isInitialized(): Boolean = initialized

    override suspend fun getAllTeams(): List<TeamModel> {
        if (!initialized) initialize()
        return remoteDataSource.getAllTeams()
    }

    override suspend fun getTeamById(teamId: String): TeamModel? {
        if (!initialized) initialize()
        return remoteDataSource.getTeamById(teamId)
    }

    override suspend fun addTeam(team: TeamModel, imageUri: Uri?): String? {
        if (!initialized) initialize()
        return remoteDataSource.addTeam(team, imageUri)
    }

    override suspend fun joinTeam(teamId: String, userId: String): Boolean {
        if (!initialized) initialize()
        if (userId.isEmpty()) return false
        return remoteDataSource.joinTeam(teamId, userId)
    }

    override suspend fun leaveTeam(teamId: String, userId: String): Boolean {
        if (!initialized) initialize()
        if (userId.isEmpty()) return false
        return remoteDataSource.leaveTeam(teamId, userId)
    }

    override suspend fun deleteTeam(teamId: String): Boolean {
        if (!initialized) initialize()
        return remoteDataSource.deleteTeam(teamId)
    }

    override suspend fun getTeamsByUserId(userId: String): List<TeamModel> {
        if (!initialized) initialize()
        if (userId.isEmpty()) return emptyList()
        return remoteDataSource.getTeamsByUserId(userId)
    }

    override suspend fun updateTeamPhoto(teamId: String, imageUri: Uri): Boolean {
        if (!initialized) initialize()
        try {
            val team = getTeamById(teamId) ?: return false
            val tempTeam = team.copy()
            val tempId = remoteDataSource.addTeam(tempTeam, imageUri)

            if (tempId != null) {
                val tempTeam = remoteDataSource.getTeamById(tempId)
                if (tempTeam != null) {
                    val fileId = tempTeam.driveFileId
                    val imageUrl = tempTeam.imageUrl

                    remoteDataSource.deleteTeam(tempId)

                    if (!team.driveFileId.isNullOrEmpty() && team.driveFileId != fileId) {
                        val tempTeamWithOldFile = team.copy(id = "temp_for_delete_" + System.currentTimeMillis())
                        val tempDocId = firestore.collection("teams").add(mapOf(
                            "name" to tempTeamWithOldFile.name,
                            "description" to tempTeamWithOldFile.description,
                            "driveFileId" to team.driveFileId
                        )).await().id
                        remoteDataSource.deleteTeam(tempDocId)
                    }

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

    override suspend fun getUserProfile(userId: String): UserProfileData? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()

            if (document.exists()) {
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
                Log.w(TAG, "User document not found for ID: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile for $userId: ${e.message}", e)
            null
        }
    }

    // ===== MEMBER MANAGEMENT METHODS (BARU) =====

    override suspend fun addMemberToTeam(teamId: String, userId: String): Result<Unit> {
        return try {
            if (!initialized) initialize()

            firestore.runTransaction { transaction ->
                val teamRef = teamsCollection.document(teamId)
                val teamSnapshot = transaction.get(teamRef)

                // Ambil data saat ini
                val currentMembers = teamSnapshot.get("members") as? List<String> ?: emptyList()
                val currentCount = teamSnapshot.getLong("memberCount")?.toInt() ?: 0
                val maxMembers = teamSnapshot.getLong("maxMembers")?.toInt() ?: 10

                // Validasi
                if (currentCount >= maxMembers) {
                    throw Exception("Tim sudah penuh")
                }

                if (userId in currentMembers) {
                    throw Exception("User sudah menjadi member")
                }

                // Update atomic
                transaction.update(teamRef, mapOf(
                    "members" to (currentMembers + userId),
                    "memberCount" to (currentCount + 1)
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding member to team: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun removeMemberFromTeam(teamId: String, userId: String): Result<Unit> {
        return try {
            if (!initialized) initialize()

            firestore.runTransaction { transaction ->
                val teamRef = teamsCollection.document(teamId)
                val teamSnapshot = transaction.get(teamRef)

                val currentMembers = teamSnapshot.get("members") as? List<String> ?: emptyList()
                val currentCount = teamSnapshot.getLong("memberCount")?.toInt() ?: 0

                if (userId !in currentMembers) {
                    throw Exception("User bukan member tim ini")
                }

                transaction.update(teamRef, mapOf(
                    "members" to (currentMembers - userId),
                    "memberCount" to maxOf(0, currentCount - 1) // Prevent negative
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing member from team: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getTeamMembers(teamId: String): Result<List<String>> {
        return try {
            if (!initialized) initialize()

            val doc = teamsCollection.document(teamId).get().await()
            val members = doc.get("members") as? List<String> ?: emptyList()
            Result.success(members)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting team members: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun isUserMemberOfTeam(teamId: String, userId: String): Result<Boolean> {
        return try {
            if (!initialized) initialize()

            val doc = teamsCollection.document(teamId).get().await()
            val members = doc.get("members") as? List<String> ?: emptyList()
            Result.success(userId in members)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user membership: ${e.message}")
            Result.failure(e)
        }
    }
}