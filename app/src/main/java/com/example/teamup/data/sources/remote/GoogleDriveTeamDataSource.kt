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
                "isPrivate" to updatedTeam.isPrivate
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
                    val team = TeamModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        avatarResId = doc.getLong("avatarResId")?.toInt() ?: R.drawable.captain_icon,
                        imageUrl = doc.getString("imageUrl"),
                        driveFileId = doc.getString("driveFileId"),
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                        maxMembers = doc.getLong("maxMembers")?.toInt() ?: 5,
                        isPrivate = doc.getBoolean("isPrivate") ?: true
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
}