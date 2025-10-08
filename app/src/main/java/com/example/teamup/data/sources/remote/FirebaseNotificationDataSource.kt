package com.example.teamup.data.sources.remote

import android.content.Context
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
    private val notificationsCollection = firestore.collection("notifications")
    private val auth = FirebaseAuth.getInstance()

    // Get all notifications untuk current user
    suspend fun getAllNotifications(): List<NotificationModel> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(NotificationModel::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
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
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(NotificationModel::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
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
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(NotificationModel::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    // ✅ TAMBAH METHOD INI (yang dipanggil dari ViewModel)
    suspend fun createNotification(notification: NotificationModel): Result<String> {
        return try {
            val docRef = notificationsCollection.document()
            val notificationWithId = notification.copy(
                id = docRef.id,
                createdAt = Timestamp.now()
            )
            docRef.set(notificationWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mark as read
    suspend fun markAsRead(notificationId: String) {
        try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            // Log error
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
        } catch (e: Exception) {
            // Log error
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
            0
        }
    }

    // Accept join request (dummy implementation - nanti dipindah ke JoinRequestViewModel)
    suspend fun acceptJoinRequest(notificationId: String) {
        // Method ini sekarang deprecated, logic udah di JoinRequestViewModel
        markAsRead(notificationId)
    }

    // Reject join request (dummy implementation - nanti dipindah ke JoinRequestViewModel)
    suspend fun rejectJoinRequest(notificationId: String) {
        // Method ini sekarang deprecated, logic udah di JoinRequestViewModel
        markAsRead(notificationId)
    }

    // Delete notification
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}