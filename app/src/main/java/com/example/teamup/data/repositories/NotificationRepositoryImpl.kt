package com.example.teamup.data.repositories

import android.util.Log
import com.example.teamup.data.model.NotificationModel
import com.example.teamup.data.sources.remote.FirebaseNotificationDataSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class NotificationRepositoryImpl private constructor(
    private val firebaseNotificationDataSource: FirebaseNotificationDataSource
) : NotificationRepository {

    private val TAG = "NotificationRepositoryImpl"
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationsCollection = firestore.collection("notifications")

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

    // METHOD BARU: Create notification
    override suspend fun createNotification(notification: NotificationModel): Result<String> {
        return firebaseNotificationDataSource.createNotification(notification)
    }

    companion object {
        @Volatile
        private var instance: NotificationRepositoryImpl? = null

        fun getInstance(
            firebaseNotificationDataSource: FirebaseNotificationDataSource
        ): NotificationRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: NotificationRepositoryImpl(firebaseNotificationDataSource).also {
                    instance = it
                }
            }
        }
    }
}