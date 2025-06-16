// data/repositories/user/EducationRepository.kt
package com.example.teamup.data.repositories.user

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.user.Education
import com.example.teamup.data.sources.remote.user.EducationRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EducationRepository {
    private val remoteDataSource = EducationRemoteDataSource()

    companion object {
        private const val TAG = "EducationRepository"
    }

    /**
     * Get all educations for a user as Flow
     */
    fun getUserEducations(userId: String): Flow<List<Education>> = flow {
        try {
            val educations = remoteDataSource.getEducations(userId)
            emit(educations)
            Log.d(TAG, "Retrieved ${educations.size} educations for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user educations", e)
            emit(emptyList())
        }
    }

    /**
     * Get specific education by ID
     */
    suspend fun getEducation(userId: String, educationId: String): Education? {
        return try {
            val education = remoteDataSource.getEducationById(userId, educationId)
            Log.d(TAG, "Retrieved education: $educationId")
            education
        } catch (e: Exception) {
            Log.e(TAG, "Error getting education by ID", e)
            null
        }
    }

    /**
     * Add new education with media upload
     */
    suspend fun addEducation(userId: String, education: Education, mediaUris: List<Uri>): Boolean {
        return try {
            // First save the education
            val saveResult = remoteDataSource.saveEducation(userId, education)

            if (saveResult.isSuccess) {
                val educationId = saveResult.getOrThrow()

                // Upload media if any
                val mediaUrls = if (mediaUris.isNotEmpty()) {
                    remoteDataSource.uploadEducationMedia(userId, educationId, mediaUris)
                } else {
                    emptyList()
                }

                // Update education with media URLs if any were uploaded
                if (mediaUrls.isNotEmpty()) {
                    val updatedEducation = education.copy(
                        id = educationId,
                        mediaUrls = mediaUrls,
                        updatedAt = System.currentTimeMillis()
                    )
                    remoteDataSource.updateEducation(userId, updatedEducation)
                }

                Log.d(TAG, "Education added successfully: $educationId")
                true
            } else {
                Log.e(TAG, "Failed to save education", saveResult.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding education", e)
            false
        }
    }

    /**
     * Update existing education with media upload
     */
    suspend fun updateEducation(userId: String, education: Education, mediaUris: List<Uri>): Boolean {
        return try {
            // Upload new media if any
            val newMediaUrls = if (mediaUris.isNotEmpty()) {
                remoteDataSource.uploadEducationMedia(userId, education.id, mediaUris)
            } else {
                emptyList()
            }

            // Combine existing and new media URLs
            val allMediaUrls = education.mediaUrls + newMediaUrls

            // Update education with all media URLs
            val updatedEducation = education.copy(
                mediaUrls = allMediaUrls,
                updatedAt = System.currentTimeMillis()
            )

            val updateResult = remoteDataSource.updateEducation(userId, updatedEducation)

            if (updateResult.isSuccess) {
                Log.d(TAG, "Education updated successfully: ${education.id}")
                true
            } else {
                Log.e(TAG, "Failed to update education", updateResult.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating education", e)
            false
        }
    }

    /**
     * Delete education
     */
    suspend fun deleteEducation(userId: String, educationId: String): Boolean {
        return try {
            val deleteResult = remoteDataSource.deleteEducation(userId, educationId)

            if (deleteResult.isSuccess) {
                Log.d(TAG, "Education deleted successfully: $educationId")
                true
            } else {
                Log.e(TAG, "Failed to delete education", deleteResult.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting education", e)
            false
        }
    }

    /**
     * Delete specific media from education
     */
    suspend fun deleteEducationMedia(mediaUrl: String): Boolean {
        return try {
            val deleteResult = remoteDataSource.deleteEducationMedia(mediaUrl)

            if (deleteResult.isSuccess) {
                Log.d(TAG, "Education media deleted successfully")
                true
            } else {
                Log.e(TAG, "Failed to delete education media", deleteResult.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting education media", e)
            false
        }
    }
}