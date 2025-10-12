package com.example.teamup.data.repositories

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.sources.remote.GoogleDriveTeamDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeamRepositoryImpl private constructor(
    private val remoteDataSource: GoogleDriveTeamDataSource
) : TeamRepository {

    private val TAG = "TeamRepositoryImpl"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
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

    // ===== MEMBER MANAGEMENT METHODS (ENHANCED WITH LOGGING) =====

    override suspend fun addMemberToTeam(teamId: String, userId: String): Result<Unit> {
        return try {
            if (!initialized) initialize()

            Log.d(TAG, "🔵 START addMemberToTeam: teamId=$teamId, userId=$userId")

            // ✅ STEP 1: Cek dokumen team ada
            val teamDoc = teamsCollection.document(teamId).get().await()
            if (!teamDoc.exists()) {
                Log.e(TAG, "❌ Team document not found: $teamId")
                return Result.failure(Exception("Tim tidak ditemukan"))
            }

            Log.d(TAG, "✅ Team document exists")

            // ✅ STEP 2: Log data current
            val currentMembers = teamDoc.get("members") as? List<String>
            val currentCount = teamDoc.getLong("memberCount")?.toInt()
            val maxMembers = teamDoc.getLong("maxMembers")?.toInt()

            Log.d(TAG, "📊 Current data:")
            Log.d(TAG, "   - members: $currentMembers")
            Log.d(TAG, "   - memberCount: $currentCount")
            Log.d(TAG, "   - maxMembers: $maxMembers")

            // ✅ STEP 3: Validasi
            if (currentMembers == null) {
                Log.w(TAG, "⚠️ members field is null, initializing as empty list")
            }

            if (currentCount == null) {
                Log.w(TAG, "⚠️ memberCount field is null, initializing as 0")
            }

            val members = currentMembers ?: emptyList()
            val count = currentCount ?: 0
            val max = maxMembers ?: 10

            if (count >= max) {
                Log.e(TAG, "❌ Team is full: $count/$max")
                return Result.failure(Exception("Tim sudah penuh"))
            }

            if (userId in members) {
                Log.e(TAG, "❌ User already a member: $userId")
                return Result.failure(Exception("User sudah menjadi member"))
            }

            // ✅ STEP 4: Update menggunakan FieldValue.arrayUnion (lebih aman)
            Log.d(TAG, "🔄 Updating team document...")

            teamsCollection.document(teamId).update(
                mapOf(
                    "members" to FieldValue.arrayUnion(userId),
                    "memberCount" to FieldValue.increment(1)
                )
            ).await()

            Log.d(TAG, "✅ Member added successfully!")

            // ✅ STEP 5: Verify update
            val updatedDoc = teamsCollection.document(teamId).get().await()
            val updatedMembers = updatedDoc.get("members") as? List<String>
            val updatedCount = updatedDoc.getLong("memberCount")?.toInt()

            Log.d(TAG, "📊 Updated data:")
            Log.d(TAG, "   - members: $updatedMembers")
            Log.d(TAG, "   - memberCount: $updatedCount")

            if (userId in (updatedMembers ?: emptyList())) {
                Log.d(TAG, "✅ VERIFIED: User is now in members list")
            } else {
                Log.e(TAG, "❌ VERIFICATION FAILED: User not in members list after update!")
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR addMemberToTeam: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun removeMemberFromTeam(teamId: String, userId: String): Result<Unit> {
        return try {
            if (!initialized) initialize()

            Log.d(TAG, "🔵 START removeMemberFromTeam: teamId=$teamId, userId=$userId")

            val teamDoc = teamsCollection.document(teamId).get().await()
            if (!teamDoc.exists()) {
                Log.e(TAG, "❌ Team document not found: $teamId")
                return Result.failure(Exception("Tim tidak ditemukan"))
            }

            val currentMembers = teamDoc.get("members") as? List<String> ?: emptyList()

            if (userId !in currentMembers) {
                Log.e(TAG, "❌ User not a member: $userId")
                return Result.failure(Exception("User bukan member tim ini"))
            }

            teamsCollection.document(teamId).update(
                mapOf(
                    "members" to FieldValue.arrayRemove(userId),
                    "memberCount" to FieldValue.increment(-1)
                )
            ).await()

            Log.d(TAG, "✅ Member removed successfully!")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR removeMemberFromTeam: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getTeamMembers(teamId: String): Result<List<String>> {
        return try {
            if (!initialized) initialize()

            val doc = teamsCollection.document(teamId).get().await()
            val members = doc.get("members") as? List<String> ?: emptyList()

            Log.d(TAG, "📊 getTeamMembers: teamId=$teamId, count=${members.size}")
            Result.success(members)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR getTeamMembers: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun isUserMemberOfTeam(teamId: String, userId: String): Result<Boolean> {
        return try {
            if (!initialized) initialize()

            val doc = teamsCollection.document(teamId).get().await()
            val members = doc.get("members") as? List<String> ?: emptyList()
            val isMember = userId in members

            Log.d(TAG, "🔍 isUserMemberOfTeam: teamId=$teamId, userId=$userId, result=$isMember")
            Result.success(isMember)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR isUserMemberOfTeam: ${e.message}", e)
            Result.failure(e)
        }
    }
}