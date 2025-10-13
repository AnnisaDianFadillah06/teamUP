package com.example.teamup.data.sources.remote

import android.util.Log
import com.example.teamup.data.model.NotificationModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseNotificationDataSource(
    private val firestore: FirebaseFirestore
) {
    private val TAG = "FirebaseNotifDS"
    private val notificationsCollection = firestore.collection("notifications")
    private val auth = FirebaseAuth.getInstance()

    // Get all notifications untuk current user
    suspend fun getAllNotifications(): List<NotificationModel> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(NotificationModel::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing notification: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all notifications: ${e.message}")
            emptyList()
        }
    }

    // Get unread notifications only
    suspend fun getUnreadNotifications(): List<NotificationModel> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(NotificationModel::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing unread notification: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread notifications: ${e.message}")
            emptyList()
        }
    }

    // Real-time observer untuk notifications
    fun observeNotifications(): Flow<List<NotificationModel>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing notifications: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(NotificationModel::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing notification in observer: ${e.message}")
                        null
                    }
                } ?: emptyList()

                trySend(notifications)
            }

        awaitClose {
            Log.d(TAG, "Closing notification observer for user: $userId")
            listener.remove()
        }
    }

    // ✅ CREATE NOTIFICATION (digunakan oleh Invitation & Join Request)
    suspend fun createNotification(notification: NotificationModel): Result<String> {
        return try {
            val docRef = notificationsCollection.document()
            val notificationWithId = notification.copy(
                id = docRef.id,
                createdAt = Timestamp.now()
            )
            docRef.set(notificationWithId).await()
            Log.d(TAG, "✅ Notification created: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating notification: ${e.message}")
            Result.failure(e)
        }
    }

    // Mark as read
    suspend fun markAsRead(notificationId: String) {
        try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
            Log.d(TAG, "Notification marked as read: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking as read: ${e.message}")
        }
    }

    // Mark all as read
    suspend fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Log.d(TAG, "All notifications marked as read for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all as read: ${e.message}")
        }
    }

    // Get unread count
    suspend fun getUnreadNotificationsCount(): Int {
        val userId = auth.currentUser?.uid ?: return 0
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread count: ${e.message}")
            0
        }
    }

    // Accept join request (deprecated - logic moved to JoinRequestViewModel)
    suspend fun acceptJoinRequest(notificationId: String) {
        // Just mark as read, actual logic is in JoinRequestViewModel
        markAsRead(notificationId)
    }

    // Reject join request (deprecated - logic moved to JoinRequestViewModel)
    suspend fun rejectJoinRequest(notificationId: String) {
        // Just mark as read, actual logic is in JoinRequestViewModel
        markAsRead(notificationId)
    }

    // Delete notification
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            Log.d(TAG, "Notification deleted: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification: ${e.message}")
            Result.failure(e)
        }
    }
}