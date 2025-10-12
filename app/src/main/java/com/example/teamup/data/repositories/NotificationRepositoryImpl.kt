package com.example.teamup.data.repositories

import android.util.Log
import com.example.teamup.data.model.NotificationModel
import com.example.teamup.data.sources.remote.FirebaseNotificationDataSource
import com.google.firebase.Timestamp  // ✅ TAMBAH
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query  // ✅ TAMBAH
import kotlinx.coroutines.channels.awaitClose  // ✅ TAMBAH
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow  // ✅ TAMBAH
import kotlinx.coroutines.tasks.await

class NotificationRepositoryImpl private constructor(
    private val firebaseNotificationDataSource: FirebaseNotificationDataSource
) : NotificationRepository {

    private val TAG = "NotificationRepositoryImpl"
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationsCollection = firestore.collection("notifications")
    private val auth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var instance: NotificationRepositoryImpl? = null

        fun getInstance(dataSource: FirebaseNotificationDataSource): NotificationRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: NotificationRepositoryImpl(dataSource).also { instance = it }
            }
        }
    }

    // ===== EXISTING METHODS (delegate ke dataSource) =====

    override suspend fun getAllNotifications(): List<NotificationModel> {
        return firebaseNotificationDataSource.getAllNotifications()
    }

    override suspend fun getUnreadNotifications(): List<NotificationModel> {
        return firebaseNotificationDataSource.getUnreadNotifications()
    }

    override fun observeNotifications(): Flow<List<NotificationModel>> {
        return firebaseNotificationDataSource.observeNotifications()
    }

    override suspend fun markAsRead(notificationId: String) {
        firebaseNotificationDataSource.markAsRead(notificationId)
    }

    override suspend fun acceptJoinRequest(notificationId: String) {
        firebaseNotificationDataSource.acceptJoinRequest(notificationId)
    }

    override suspend fun rejectJoinRequest(notificationId: String) {
        firebaseNotificationDataSource.rejectJoinRequest(notificationId)
    }

    override suspend fun getUnreadNotificationsCount(): Int {
        return firebaseNotificationDataSource.getUnreadNotificationsCount()
    }

    override suspend fun markAllAsRead() {
        firebaseNotificationDataSource.markAllAsRead()
    }

    // ===== NEW METHODS untuk Invitation & Join Request =====

    override suspend fun createNotification(notification: NotificationModel): Result<String> {
        return try {
            val docRef = notificationsCollection.document()
            val notificationWithId = notification.copy(
                id = docRef.id,
                createdAt = Timestamp.now()  // ✅ SEKARANG SUDAH ADA IMPORT
            )

            docRef.set(notificationWithId).await()

            Log.d(TAG, "✅ Notification created: ${docRef.id} for user: ${notification.userId}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating notification: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getUserNotifications(userId: String): Flow<List<NotificationModel>> {
        return callbackFlow {  // ✅ SEKARANG SUDAH ADA IMPORT
            val listener = notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)  // ✅ Query imported
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to notifications: ${error.message}")
                        close(error)  // ✅ close() available in callbackFlow
                        return@addSnapshotListener
                    }

                    val notifications = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(NotificationModel::class.java)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing notification ${doc.id}: ${e.message}")
                            null
                        }
                    } ?: emptyList()

                    Log.d(TAG, "📩 Loaded ${notifications.size} notifications for user: $userId")
                    trySend(notifications)  // ✅ trySend() available in callbackFlow
                }
            awaitClose {  // ✅ awaitClose imported
                Log.d(TAG, "Closing notification listener for user: $userId")
                listener.remove()
            }
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
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