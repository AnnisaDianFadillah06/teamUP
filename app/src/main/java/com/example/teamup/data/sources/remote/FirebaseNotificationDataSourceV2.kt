package com.example.teamup.data.sources.remote

import android.util.Log
import com.example.teamup.data.model.NotificationModelV2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseNotificationDataSourceV2 private constructor() {
    private val TAG = "FirebaseNotificationDataSourceV2"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var instance: FirebaseNotificationDataSourceV2? = null

        fun getInstance(): FirebaseNotificationDataSourceV2 {
            return instance ?: synchronized(this) {
                instance ?: FirebaseNotificationDataSourceV2().also { instance = it }
            }
        }
    }

    suspend fun getAllNotifications(): List<NotificationModelV2> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return emptyList()

            val result = firestore.collection("notifications")
                .whereEqualTo("recipientId", currentUserId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                doc.toObject(NotificationModelV2::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUnreadNotifications(): List<NotificationModelV2> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return emptyList()

            val result = firestore.collection("notifications")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("isRead", false)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                doc.toObject(NotificationModelV2::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread notifications: ${e.message}")
            emptyList()
        }
    }

    fun observeNotifications(): Flow<List<NotificationModelV2>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("notifications")
            .whereEqualTo("recipientId", currentUserId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing notifications: ${error.message}")
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(NotificationModelV2::class.java)
                } ?: emptyList()

                trySend(notifications)
            }

        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read: ${e.message}")
        }
    }

    suspend fun acceptJoinRequest(notificationId: String): Boolean {
        return try {
            // Get notification details
            val notificationDoc = firestore.collection("notifications").document(notificationId).get().await()
            val teamId = notificationDoc.getString("teamId") ?: return false
            val senderId = notificationDoc.getString("senderId") ?: return false

            // Add user to team
            firestore.collection("teams").document(teamId)
                .update("memberIds", FieldValue.arrayUnion(senderId))
                .await()

            // Mark notification as read
            markAsRead(notificationId)

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting join request: ${e.message}")
            false
        }
    }

    suspend fun rejectJoinRequest(notificationId: String): Boolean {
        return try {
            // Just mark as read for now
            markAsRead(notificationId)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting join request: ${e.message}")
            false
        }
    }

    suspend fun getUnreadNotificationsCount(): Int {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return 0

            val result = firestore.collection("notifications")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            result.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread count: ${e.message}")
            0
        }
    }

    suspend fun markAllAsRead() {
        try {
            val currentUserId = auth.currentUser?.uid ?: return

            val batch = firestore.batch()
            val unreadNotifications = firestore.collection("notifications")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            unreadNotifications.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }

            batch.commit().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all as read: ${e.message}")
        }
    }
}