// data/repositories/ActivityRepository.kt
package com.example.teamup.data.repositories.user

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.user.Activity
import com.example.teamup.data.sources.remote.user.ActivityRemoteDataSource

class ActivityRepository {
    private val remoteDataSource = ActivityRemoteDataSource()

    companion object {
        private const val TAG = "ActivityRepository"
    }

    /**
     * Create a new activity
     */
    suspend fun createActivity(userId: String, activity: Activity, mediaUris: List<Uri>? = null): Result<String> {
        return try {
            // Upload media files first if provided
            val mediaUrls = if (!mediaUris.isNullOrEmpty()) {
                val activityId = activity.id.ifEmpty { java.util.UUID.randomUUID().toString() }
                remoteDataSource.uploadActivityMedia(userId, activityId, mediaUris)
            } else {
                emptyList()
            }

            val activityWithMedia = activity.copy(
                userId = userId,
                mediaUrls = activity.mediaUrls + mediaUrls
            )

            remoteDataSource.saveActivity(userId, activityWithMedia)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating activity for user: $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Update an existing activity
     */
    suspend fun updateActivity(userId: String, activity: Activity, newMediaUris: List<Uri>? = null): Result<Unit> {
        return try {
            if (activity.id.isEmpty()) {
                return Result.failure(IllegalArgumentException("Activity ID cannot be empty"))
            }

            // Upload new media files if provided
            val newMediaUrls = if (!newMediaUris.isNullOrEmpty()) {
                remoteDataSource.uploadActivityMedia(userId, activity.id, newMediaUris)
            } else {
                emptyList()
            }

            // Combine existing and new media URLs
            val updatedActivity = activity.copy(
                mediaUrls = activity.mediaUrls + newMediaUrls,
                updatedAt = System.currentTimeMillis()
            )

            remoteDataSource.updateActivity(userId, updatedActivity)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating activity: ${activity.id}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete an activity
     */
    suspend fun deleteActivity(userId: String, activityId: String): Result<Unit> {
        return remoteDataSource.deleteActivity(userId, activityId)
    }

    /**
     * Get all activities for a user
     */
    suspend fun getUserActivities(userId: String, limit: Int = 20): Result<List<Activity>> {
        return try {
            val activities = remoteDataSource.getActivities(userId)
                .take(limit) // Apply limit if needed

            Log.d(TAG, "Retrieved ${activities.size} activities for user: $userId")
            Result.success(activities)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user activities: $userId", e)
            Result.failure(e)
        }
    }


    /**
     * Get a specific activity by ID (throws exception on error - for ViewModel compatibility)
     */
    suspend fun getActivity(userId: String, activityId: String): Activity {
        return try {
            val activity = remoteDataSource.getActivityById(userId, activityId)
            if (activity != null) {
                Log.d(TAG, "Retrieved activity: $activityId")
                activity
            } else {
                Log.w(TAG, "Activity not found: $activityId")
                throw NoSuchElementException("Activity with ID $activityId not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activity: $activityId", e)
            throw e
        }
    }

    /**
     * Get activities by visibility
     */
    suspend fun getActivitiesByVisibility(userId: String, visibility: String): Result<List<Activity>> {
        return try {
            val activities = remoteDataSource.getActivitiesByVisibility(userId, visibility)
            Log.d(TAG, "Retrieved ${activities.size} activities with visibility $visibility")
            Result.success(activities)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activities by visibility: $visibility", e)
            Result.failure(e)
        }
    }

    /**
     * Update activity visibility
     */
    suspend fun updateActivityVisibility(userId: String, activityId: String, visibility: String): Result<Unit> {
        return remoteDataSource.updateActivityVisibility(userId, activityId, visibility)
    }

    /**
     * Get public activities (for feed)
     */
    suspend fun getPublicActivities(limit: Int = 20): Result<List<Activity>> {
        return try {
            val activities = remoteDataSource.getPublicActivities(limit)
            Log.d(TAG, "Retrieved ${activities.size} public activities")
            Result.success(activities)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting public activities", e)
            Result.failure(e)
        }
    }

    /**
     * Search activities by content
     */
    suspend fun searchActivities(userId: String, query: String): Result<List<Activity>> {
        return try {
            // Get all user activities and filter by content
            val allActivities = remoteDataSource.getActivities(userId)

            val matchingActivities = allActivities.filter { activity ->
                activity.content.contains(query, ignoreCase = true)
            }

            Log.d(TAG, "Found ${matchingActivities.size} activities matching query: $query")
            Result.success(matchingActivities)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching activities with query: $query", e)
            Result.failure(e)
        }
    }

    /**
     * Upload single activity image (for backward compatibility)
     */
    suspend fun uploadActivityImage(userId: String, activityId: String, imageUri: Uri): String? {
        return remoteDataSource.uploadActivityImage(userId, activityId, imageUri)
    }

    /**
     * Upload multiple media files
     */
    suspend fun uploadActivityMedia(userId: String, activityId: String, mediaUris: List<Uri>): List<String> {
        return remoteDataSource.uploadActivityMedia(userId, activityId, mediaUris)
    }

    /**
     * Delete activity image
     */
    suspend fun deleteActivityImage(imageUrl: String): Result<Unit> {
        return remoteDataSource.deleteActivityImage(imageUrl)
    }
}