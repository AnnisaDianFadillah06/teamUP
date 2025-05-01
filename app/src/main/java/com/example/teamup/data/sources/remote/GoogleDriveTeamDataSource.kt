package com.example.teamup.data.sources.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.teamup.R
import com.example.teamup.data.model.TeamModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleDriveTeamDataSource(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val teamsCollection = firestore.collection("teams")
    private val driveService = GoogleDriveHelper(context)

    private val TAG = "GoogleDriveTeamDataSource"

    suspend fun initialize() {
        try {
            driveService.initialize()
        } catch (e: Exception) {
            Log.d(TAG, "Error initializing Google Drive service: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun addTeam(team: TeamModel, imageUri: Uri? = null): String? {
        return try {
            Log.d(TAG, "addTeam called with imageUri: $imageUri")

            var imageUrl: String? = null
            var fileId: String? = null
            var updatedTeam = team

            // Ensure image is uploaded before proceeding to save the team
            if (imageUri != null) {
                try {
                    if (!driveService.isInitialized()) {
                        Log.d(TAG, "Initializing Drive service")
                        driveService.initialize()
                    }

                    val tempDocId = teamsCollection.document().id
                    Log.d(TAG, "Uploading image with tempDocId: $tempDocId")

                    fileId = driveService.uploadTeamProfileImage(tempDocId, imageUri)
                    Log.d(TAG, "File uploaded with ID: $fileId")

                    if (fileId != null) {
                        imageUrl = driveService.getFileUrl(fileId)
                        Log.d(TAG, "File URL: $imageUrl")

                        updatedTeam = team.copy(
                            imageUrl = imageUrl,
                            driveFileId = fileId
                        )
                    } else {
                        Log.d(TAG, "File ID is null after upload")
                    }
                } catch (e: CancellationException) {
                    // Let cancellation exceptions propagate
                    Log.d(TAG, "Upload cancelled: ${e.message}")
                    throw e
                } catch (e: Exception) {
                    Log.d(TAG, "Error uploading image: ${e.message}")
                    e.printStackTrace()
                    // Continue without image rather than failing the entire team creation
                }
            } else {
                Log.d(TAG, "No image URI provided")
            }

            val teamMap = hashMapOf(
                "name" to updatedTeam.name,
                "description" to updatedTeam.description,
                "category" to updatedTeam.category,
                "avatarResId" to updatedTeam.avatarResId,
                "imageUrl" to updatedTeam.imageUrl,
                "driveFileId" to updatedTeam.driveFileId,
                "createdAt" to updatedTeam.createdAt,
                "maxMembers" to updatedTeam.maxMembers,
                "isPrivate" to updatedTeam.isPrivate,
                "members" to updatedTeam.members,
                "memberCount" to updatedTeam.members.size,
                "captainId" to "" // Menambahkan captainId kosong sesuai skema Firestore
            )

            Log.d(TAG, "Saving team to Firestore with imageUrl: ${updatedTeam.imageUrl}, driveFileId: ${updatedTeam.driveFileId}")

            val documentRef = teamsCollection.add(teamMap).await()
            Log.d(TAG, "Team added with ID: ${documentRef.id}")
            documentRef.id
        } catch (e: CancellationException) {
            // Properly handle cancellation
            Log.d(TAG, "Team creation cancelled: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Error adding team: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllTeams(): List<TeamModel> {
        return try {
            Log.d(TAG, "Fetching teams from Firebase...")
            val snapshot = teamsCollection.get().await()
            Log.d(TAG, "Got ${snapshot.documents.size} team documents from Firebase")

            val teams = snapshot.documents.mapNotNull { doc ->
                try {
                    val members = doc.get("members") as? List<String> ?: emptyList()
                    val memberCount = doc.getLong("memberCount")?.toInt() ?: members.size

                    val team = TeamModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        captainId = doc.getString("captainId") ?: "",
                        category = doc.getString("category") ?: "",
                        avatarResId = doc.getLong("avatarResId")?.toInt() ?: R.drawable.captain_icon,
                        imageUrl = doc.getString("imageUrl"),
                        driveFileId = doc.getString("driveFileId"),
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                        members = members,
                        maxMembers = doc.getLong("maxMembers")?.toInt() ?: 5,
                        isPrivate = doc.getBoolean("isPrivate") ?: true,
                        memberCount = memberCount,
                        isFull = memberCount >= (doc.getLong("maxMembers")?.toInt() ?: 5)
                    )
                    Log.d(TAG, "Successfully mapped team: ${team.name}, ID: ${team.id}, Category: ${team.category}")
                    team
                } catch (e: Exception) {
                    Log.d(TAG, "Error mapping document ${doc.id}: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }
            Log.d(TAG, "Total teams mapped: ${teams.size}")
            teams
        } catch (e: Exception) {
            Log.d(TAG, "Error getting teams: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTeamById(teamId: String): TeamModel? {
        return try {
            val docSnapshot = teamsCollection.document(teamId).get().await()

            if (docSnapshot.exists()) {
                val members = docSnapshot.get("members") as? List<String> ?: emptyList()
                val memberCount = docSnapshot.getLong("memberCount")?.toInt() ?: members.size

                TeamModel(
                    id = docSnapshot.id,
                    name = docSnapshot.getString("name") ?: "",
                    description = docSnapshot.getString("description") ?: "",
                    category = docSnapshot.getString("category") ?: "",
                    avatarResId = docSnapshot.getLong("avatarResId")?.toInt() ?: R.drawable.captain_icon,
                    imageUrl = docSnapshot.getString("imageUrl"),
                    driveFileId = docSnapshot.getString("driveFileId"),
                    createdAt = docSnapshot.getTimestamp("createdAt") ?: Timestamp.now(),
                    members = members,
                    maxMembers = docSnapshot.getLong("maxMembers")?.toInt() ?: 5,
                    isPrivate = docSnapshot.getBoolean("isPrivate") ?: true,
                    memberCount = memberCount,
                    isFull = memberCount >= (docSnapshot.getLong("maxMembers")?.toInt() ?: 5)
                )
            } else {
                Log.d(TAG, "Team with ID $teamId not found")
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error getting team by ID: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun joinTeam(teamId: String, userId: String): Boolean {
        return try {
            val teamDoc = teamsCollection.document(teamId)
            val teamSnapshot = teamDoc.get().await()

            if (!teamSnapshot.exists()) {
                Log.d(TAG, "Team not found for joining: $teamId")
                return false
            }

            val members = teamSnapshot.get("members") as? List<String> ?: emptyList()

            if (members.contains(userId)) {
                Log.d(TAG, "User already a member of team $teamId")
                return true // User is already a member
            }

            val maxMembers = teamSnapshot.getLong("maxMembers")?.toInt() ?: 5

            if (members.size >= maxMembers) {
                Log.d(TAG, "Team is full, cannot join: $teamId")
                return false
            }

            val updatedMembers = members + userId

            // Update the team document
            teamDoc.update(
                mapOf(
                    "members" to updatedMembers,
                    "memberCount" to updatedMembers.size
                )
            ).await()

            Log.d(TAG, "User $userId successfully joined team $teamId")
            true
        } catch (e: Exception) {
            Log.d(TAG, "Error joining team: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun leaveTeam(teamId: String, userId: String): Boolean {
        return try {
            val teamDoc = teamsCollection.document(teamId)
            val teamSnapshot = teamDoc.get().await()

            if (!teamSnapshot.exists()) {
                Log.d(TAG, "Team not found for leaving: $teamId")
                return false
            }

            val members = teamSnapshot.get("members") as? List<String> ?: emptyList()

            if (!members.contains(userId)) {
                Log.d(TAG, "User is not a member of team $teamId")
                return true // User is not a member anyway
            }

            val updatedMembers = members.filter { it != userId }

            // Update the team document
            teamDoc.update(
                mapOf(
                    "members" to updatedMembers,
                    "memberCount" to updatedMembers.size
                )
            ).await()

            Log.d(TAG, "User $userId successfully left team $teamId")
            true
        } catch (e: Exception) {
            Log.d(TAG, "Error leaving team: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTeam(teamId: String): Boolean {
        return try {
            val teamDoc = teamsCollection.document(teamId).get().await()
            val driveFileId = teamDoc.getString("driveFileId")

            if (!driveFileId.isNullOrEmpty()) {
                try {
                    driveService.deleteFile(driveFileId)
                } catch (e: Exception) {
                    Log.d(TAG, "Error deleting file from Drive: ${e.message}")
                }
            }

            teamsCollection.document(teamId).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Error deleting team: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun getTeamsByUserId(userId: String): List<TeamModel> {
        return try {
            Log.d(TAG, "Fetching teams for user $userId...")

            val memberQuery = teamsCollection
                .whereArrayContains("members", userId)
                .get()
                .await()

            val captainQuery = teamsCollection
                .whereEqualTo("captainId", userId)
                .get()
                .await()

            val allDocs = (memberQuery.documents + captainQuery.documents)
                .distinctBy { it.id } // Hindari duplikat jika user adalah member sekaligus captain

            Log.d(TAG, "Got ${allDocs.size} total teams for user $userId")

            allDocs.mapNotNull { doc ->
                try {
                    val members = doc.get("members") as? List<String> ?: emptyList()
                    val memberCount = doc.getLong("memberCount")?.toInt() ?: members.size

                    TeamModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        captainId = doc.getString("captainId") ?: "",
                        avatarResId = doc.getLong("avatarResId")?.toInt() ?: R.drawable.captain_icon,
                        imageUrl = doc.getString("imageUrl"),
                        driveFileId = doc.getString("driveFileId"),
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                        members = members,
                        maxMembers = doc.getLong("maxMembers")?.toInt() ?: 5,
                        isPrivate = doc.getBoolean("isPrivate") ?: true,
                        memberCount = memberCount,
                        isJoined = true,
                        isFull = memberCount >= (doc.getLong("maxMembers")?.toInt() ?: 5)
                    )
                } catch (e: Exception) {
                    Log.d(TAG, "Error mapping document ${doc.id}: ${e.message}")
                    null
                }
            }

        } catch (e: Exception) {
            Log.d(TAG, "Error getting teams for user: ${e.message}")
            emptyList()
        }
    }
}