package com.example.teamup.data.sources.remote.user

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.user.Activity
import com.example.teamup.data.model.user.toActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ActivityRemoteDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "ActivityRemoteSource"
        private const val USERS_COLLECTION = "users"
        private const val ACTIVITIES_COLLECTION = "activities" // Changed from "posts" to match repository
        private const val STORAGE_PATH = "activity_media" // Changed to match repository
    }

    suspend fun getActivities(userId: String): List<Activity> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.toActivity()?.copy(id = doc.id) // Changed from postId to id
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activities: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getActivityById(userId: String, activityId: String): Activity? {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .document(activityId)
                .get()
                .await()

            doc.data?.toActivity()?.copy(id = doc.id) // Changed from postId to id
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activity by ID: ${e.message}", e)
            null
        }
    }

    suspend fun saveActivity(userId: String, activity: Activity): Result<String> {
        return try {
            val activityId = activity.id.ifEmpty { UUID.randomUUID().toString() } // Changed from postId to id

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .document(activityId)
                .set(activity.toMap())
                .await()

            Result.success(activityId)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving activity: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateActivity(userId: String, activity: Activity): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .document(activity.id) // Changed from postId to id
                .set(activity.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating activity: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteActivity(userId: String, activityId: String): Result<Unit> {
        return try {
            // Get activity data first to delete associated media
            val activity = getActivityById(userId, activityId)

            // Delete associated media files
            activity?.let { act ->
                // Delete single image if exists (for backward compatibility)
                act.imageUrl?.let { imageUrl ->
                    try {
                        storage.getReferenceFromUrl(imageUrl).delete().await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete image file: $imageUrl", e)
                    }
                }

                // Delete all media files
                act.mediaUrls.forEach { mediaUrl ->
                    try {
                        storage.getReferenceFromUrl(mediaUrl).delete().await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete media file: $mediaUrl", e)
                    }
                }
            }

            // Delete activity document
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .document(activityId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting activity: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadActivityImage(userId: String, activityId: String, imageUri: Uri): String? {
        return try {
            val imageRef = storage.reference
                .child("$STORAGE_PATH/$userId/$activityId.jpg")

            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload activity image", e)
            null
        }
    }

    suspend fun uploadActivityMedia(userId: String, activityId: String, mediaUris: List<Uri>): List<String> {
        val uploadedUrls = mutableListOf<String>()
        mediaUris.forEach { uri ->
            try {
                val mediaRef = storage.reference
                    .child("$STORAGE_PATH/$userId/$activityId/${UUID.randomUUID()}")
                mediaRef.putFile(uri).await()
                val downloadUrl = mediaRef.downloadUrl.await().toString()
                uploadedUrls.add(downloadUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload media file", e)
            }
        }
        return uploadedUrls
    }

    suspend fun deleteActivityImage(imageUrl: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(imageUrl).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting activity image: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Get public activities from all users (for feed)
    suspend fun getPublicActivities(limit: Int = 20): List<Activity> {
        return try {
            val snapshot = firestore.collectionGroup(ACTIVITIES_COLLECTION)
                .whereEqualTo("visibility", "public")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.toActivity()?.copy(id = doc.id) // Changed from postId to id
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting public activities: ${e.message}", e)
            emptyList()
        }
    }

    // Additional methods to match repository functionality
    suspend fun getActivitiesByVisibility(userId: String, visibility: String): List<Activity> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .whereEqualTo("visibility", visibility)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.toActivity()?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activities by visibility: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun updateActivityVisibility(userId: String, activityId: String, visibility: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "visibility" to visibility,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .document(activityId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating activity visibility: ${e.message}", e)
            Result.failure(e)
        }
    }
}