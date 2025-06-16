// data/repositories/user/ExperienceRepository.kt
package com.example.teamup.data.repositories.user

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.user.Experience
import com.example.teamup.data.model.user.toExperience
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ExperienceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "ExperienceRepository"
        private const val USERS_COLLECTION = "users"
        private const val EXPERIENCES_COLLECTION = "experiences"
        private const val STORAGE_PATH = "experience_media"
    }

    /**
     * Get all experiences for a user as Flow
     */
    suspend fun getUserExperiences(userId: String): Flow<List<Experience>> = flow {
        try {
            val snapshot = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val experiences = snapshot.documents.mapNotNull { doc ->
                doc.data?.toExperience()?.copy(id = doc.id)
            }

            Log.d(TAG, "Retrieved ${experiences.size} experiences for user: $userId")
            emit(experiences)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user experiences", e)
            emit(emptyList())
        }
    }

    /**
     * Get specific experience by ID
     */
    suspend fun getExperience(userId: String, experienceId: String): Experience? {
        return try {
            val snapshot = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .document(experienceId)
                .get()
                .await()

            snapshot.data?.toExperience()?.copy(id = snapshot.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting experience by ID", e)
            null
        }
    }

    /**
     * Add new experience
     */
    suspend fun addExperience(userId: String, experience: Experience, mediaUris: List<Uri>): Boolean {
        return try {
            // Validasi input
            if (userId.isBlank()) {
                Log.e(TAG, "User ID is empty. Cannot add document.")
                return false
            }

            val experienceId = UUID.randomUUID().toString()

            // Upload media files first
            val mediaUrls = uploadExperienceMedia(userId, experienceId, mediaUris)

            // Create experience with uploaded media URLs
            val experienceToSave = experience.copy(
                id = experienceId,
                userId = userId,
                mediaUrls = mediaUrls,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            Log.d(TAG, "Adding new experience with ID: $experienceId")
            Log.d(TAG, "User ID: $userId")

            db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .document(experienceId)
                .set(experienceToSave.toMap())
                .await()

            Log.d(TAG, "Experience added successfully: $experienceId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding experience", e)
            false
        }
    }

    /**
     * Update existing experience
     */
    suspend fun updateExperience(userId: String, experience: Experience, mediaUris: List<Uri>): Boolean {
        return try {
            if (experience.id.isBlank()) {
                Log.e(TAG, "Experience ID is empty. Cannot update document.")
                return false
            }

            // Tambahkan validasi userId juga
            if (userId.isBlank()) {
                Log.e(TAG, "User ID is empty. Cannot update document.")
                return false
            }

            Log.d(TAG, "Updating experience with ID: ${experience.id}")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Path: users/$userId/experiences/${experience.id}")

            // Upload new media files if any
            val newMediaUrls = if (mediaUris.isNotEmpty()) {
                uploadExperienceMedia(userId, experience.id, mediaUris)
            } else {
                emptyList()
            }

            // Combine existing media URLs with new ones
            val allMediaUrls = experience.mediaUrls + newMediaUrls

            val experienceToUpdate = experience.copy(
                userId = userId, // pastikan userId ter-set
                mediaUrls = allMediaUrls,
                updatedAt = System.currentTimeMillis()
            )

            // Tambahkan log untuk debug
            Log.d(TAG, "Experience data to update: ${experienceToUpdate.toMap()}")

            db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .document(experience.id)
                .set(experienceToUpdate.toMap())
                .await()

            Log.d(TAG, "Experience updated successfully: ${experience.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating experience", e)
            false
        }
    }
    /**
     * Delete experience
     */
    suspend fun deleteExperience(userId: String, experienceId: String): Boolean {
        return try {
            // Get experience first to delete associated media
            val experience = getExperience(userId, experienceId)

            // Delete associated media files
            experience?.mediaUrls?.forEach { mediaUrl ->
                try {
                    storage.getReferenceFromUrl(mediaUrl).delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete media file: $mediaUrl", e)
                }
            }

            // Delete experience document
            db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .document(experienceId)
                .delete()
                .await()

            Log.d(TAG, "Experience deleted successfully: $experienceId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting experience", e)
            false
        }
    }

    /**
     * Upload experience media files
     */
    private suspend fun uploadExperienceMedia(userId: String, experienceId: String, mediaUris: List<Uri>): List<String> {
        val mediaUrls = mutableListOf<String>()

        mediaUris.forEach { uri ->
            try {
                val mediaRef = storage.reference
                    .child("$STORAGE_PATH/$userId/$experienceId/${UUID.randomUUID()}")

                mediaRef.putFile(uri).await()
                val downloadUrl = mediaRef.downloadUrl.await().toString()
                mediaUrls.add(downloadUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload media file", e)
            }
        }

        Log.d(TAG, "Uploaded ${mediaUrls.size} media files for experience: $experienceId")
        return mediaUrls
    }

    /**
     * Delete experience media file
     */
    suspend fun deleteExperienceMedia(mediaUrl: String): Boolean {
        return try {
            storage.getReferenceFromUrl(mediaUrl).delete().await()
            Log.d(TAG, "Experience media deleted successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting experience media", e)
            false
        }
    }
}