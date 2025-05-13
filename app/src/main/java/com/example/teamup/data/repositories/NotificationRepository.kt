package com.example.teamup.data.repositories

import com.example.teamup.data.model.NotificationModel
import com.example.teamup.data.sources.remote.FirebaseNotificationDataSource
import kotlinx.coroutines.flow.Flow

class NotificationRepository private constructor(
    private val firebaseNotificationDataSource: FirebaseNotificationDataSource
) {

    suspend fun getAllNotifications(): List<NotificationModel> {
        return firebaseNotificationDataSource.getAllNotifications()
    }

    suspend fun getUnreadNotifications(): List<NotificationModel> {
        return firebaseNotificationDataSource.getUnreadNotifications()
    }

    fun observeNotifications(): Flow<List<NotificationModel>> {
        return firebaseNotificationDataSource.observeNotifications()
    }

    suspend fun markAsRead(notificationId: String) {
        firebaseNotificationDataSource.markAsRead(notificationId)
    }

    suspend fun acceptJoinRequest(notificationId: String) {
        firebaseNotificationDataSource.acceptJoinRequest(notificationId)
    }

    suspend fun rejectJoinRequest(notificationId: String) {
        firebaseNotificationDataSource.rejectJoinRequest(notificationId)
    }

    suspend fun getUnreadNotificationsCount(): Int {
        return firebaseNotificationDataSource.getUnreadNotificationsCount()
    }

    suspend fun markAllAsRead() {
        firebaseNotificationDataSource.markAllAsRead()
    }

    companion object {
        @Volatile
        private var instance: NotificationRepository? = null

        fun getInstance(
            firebaseNotificationDataSource: FirebaseNotificationDataSource
        ): NotificationRepository {
            return instance ?: synchronized(this) {
                instance ?: NotificationRepository(firebaseNotificationDataSource).also { instance = it }
            }
        }
    }
}