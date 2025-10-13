package com.example.teamup.data.sources.remote.user

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.user.Education
import com.example.teamup.data.model.user.toEducation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EducationRemoteDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "EducationRemoteSource"
        private const val USERS_COLLECTION = "users"
        private const val EDUCATIONS_COLLECTION = "educations"
        private const val STORAGE_PATH = "education_media"
    }

    /**
     * Get all educations for a user, ordered by start date (newest first)
     */
    suspend fun getEducations(userId: String): List<Education> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EDUCATIONS_COLLECTION)
                .orderBy("startDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val educations = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.toEducation()?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse education document ${doc.id}", e)
                    null
                }
            }

            Log.d(TAG, "Retrieved ${educations.size} educations for user: $userId")
            educations
        } catch (e: Exception) {
            Log.e(TAG, "Error getting educations: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get specific education by ID
     */
    suspend fun getEducationById(userId: String, educationId: String): Education? {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EDUCATIONS_COLLECTION)
                .document(educationId)
                .get()
                .await()

            if (doc.exists()) {
                doc.data?.toEducation()?.copy(id = doc.id)
            } else {
                Log.w(TAG, "Education document not found: $educationId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting education by ID: ${e.message}", e)
            null
        }
    }

    /**
     * Save new education (create)
     */
    suspend fun saveEducation(userId: String, education: Education): Result<String> {
        return try {
            val educationId = education.id.ifEmpty { UUID.randomUUID().toString() }
            val educationToSave = education.copy(
                id = educationId,
                userId = userId,
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EDUCATIONS_COLLECTION)
                .document(educationId)
                .set(educationToSave.toMap())
                .await()

            Log.d(TAG, "Education saved successfully: $educationId")
            Result.success(educationId)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving education: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update existing education
     */
    suspend fun updateEducation(userId: String, education: Education): Result<Unit> {
        return try {
            val educationToUpdate = education.copy(
                userId = userId,
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EDUCATIONS_COLLECTION)
                .document(education.id)
                .set(educationToUpdate.toMap())
                .await()

            Log.d(TAG, "Education updated successfully: ${education.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating education: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete education and its associated media
     */
    suspend fun deleteEducation(userId: String, educationId: String): Result<Unit> {
        return try {
            // Get education data first to delete associated media
            val education = getEducationById(userId, educationId)

            // Delete associated media files
            education?.mediaUrls?.forEach { mediaUrl ->
                try {
                    storage.getReferenceFromUrl(mediaUrl).delete().await()
                    Log.d(TAG, "Deleted media file: $mediaUrl")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete media file: $mediaUrl", e)
                    // Continue even if media deletion fails
                }
            }

            // Delete education document
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EDUCATIONS_COLLECTION)
                .document(educationId)
                .delete()
                .await()

            Log.d(TAG, "Education deleted successfully: $educationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting education: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Upload multiple media files for education
     */
    suspend fun uploadEducationMedia(userId: String, educationId: String, mediaUris: List<Uri>): List<String> {
        val uploadedUrls = mutableListOf<String>()

        for (uri in mediaUris) {
            try {
                val fileName = "${UUID.randomUUID()}"
                val mediaRef = storage.reference
                    .child("$STORAGE_PATH/$userId/$educationId/$fileName")

                // Upload file
                mediaRef.putFile(uri).await()

                // Get download URL
                val downloadUrl = mediaRef.downloadUrl.await().toString()
                uploadedUrls.add(downloadUrl)

                Log.d(TAG, "Uploaded media file: $fileName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload media file: $uri", e)
                // Continue with other files even if one fails
            }
        }

        Log.d(TAG, "Successfully uploaded ${uploadedUrls.size}/${mediaUris.size} media files")
        return uploadedUrls
    }

    /**
     * Delete specific media file
     */
    suspend fun deleteEducationMedia(mediaUrl: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(mediaUrl).delete().await()
            Log.d(TAG, "Education media deleted successfully: $mediaUrl")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting media: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Batch delete multiple media files
     */
    suspend fun deleteEducationMediaBatch(mediaUrls: List<String>): Result<Unit> {
        return try {
            var deletedCount = 0
            var failedCount = 0

            for (mediaUrl in mediaUrls) {
                try {
                    storage.getReferenceFromUrl(mediaUrl).delete().await()
                    deletedCount++
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete media: $mediaUrl", e)
                    failedCount++
                }
            }

            Log.d(TAG, "Batch delete completed: $deletedCount successful, $failedCount failed")

            if (failedCount > 0) {
                throw Exception("Failed to delete $failedCount media files")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error in batch delete media: ${e.message}", e)
            Result.failure(e)
        }
    }
}