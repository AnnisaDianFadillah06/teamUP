// data/sources/remote/user/ExperienceRemoteDataSource.kt
package com.example.teamup.data.sources.remote.user

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.user.Experience
import com.example.teamup.data.model.user.toExperience
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ExperienceRemoteDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "ExperienceRemoteSource"
        private const val USERS_COLLECTION = "users"
        private const val EXPERIENCES_COLLECTION = "experiences"
        private const val STORAGE_PATH = "experience_media"
    }

    suspend fun getExperiences(userId: String): List<Experience> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.toExperience()?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting experiences: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getExperienceById(userId: String, experienceId: String): Experience? {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .document(experienceId)
                .get()
                .await()

            doc.data?.toExperience()?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting experience by ID: ${e.message}", e)
            null
        }
    }

    suspend fun saveExperience(userId: String, experience: Experience): Result<String> {
        return try {
            val experienceId = experience.id.ifEmpty { UUID.randomUUID().toString() }

            val experienceToSave = experience.copy(
                id = experienceId,
                userId = userId,
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .document(experienceId)
                .set(experienceToSave.toMap())
                .await()

            Result.success(experienceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving experience: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateExperience(userId: String, experience: Experience): Result<Unit> {
        return try {
            val experienceToUpdate = experience.copy(
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .document(experience.id)
                .set(experienceToUpdate.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating experience: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteExperience(userId: String, experienceId: String): Result<Unit> {
        return try {
            // Get experience data first to delete associated media
            val experience = getExperienceById(userId, experienceId)

            // Delete associated media files
            experience?.mediaUrls?.forEach { mediaUrl ->
                try {
                    storage.getReferenceFromUrl(mediaUrl).delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete media file: $mediaUrl", e)
                }
            }

            // Delete experience document
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_COLLECTION)
                .document(experienceId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting experience: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadExperienceMedia(userId: String, experienceId: String, mediaUris: List<Uri>): List<String> {
        val uploadedUrls = mutableListOf<String>()

        mediaUris.forEach { uri ->
            try {
                val mediaRef = storage.reference
                    .child("$STORAGE_PATH/$userId/$experienceId/${UUID.randomUUID()}")

                mediaRef.putFile(uri).await()
                val downloadUrl = mediaRef.downloadUrl.await().toString()
                uploadedUrls.add(downloadUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload media file", e)
            }
        }

        Log.d(TAG, "Uploaded ${uploadedUrls.size} media files for experience: $experienceId")
        return uploadedUrls
    }

    suspend fun deleteExperienceMedia(mediaUrl: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(mediaUrl).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting media: ${e.message}", e)
            Result.failure(e)
        }
    }
}