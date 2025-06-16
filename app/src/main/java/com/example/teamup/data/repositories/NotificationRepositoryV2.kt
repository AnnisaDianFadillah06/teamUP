package com.example.teamup.data.repositories

import com.example.teamup.data.model.NotificationModelV2
import com.example.teamup.data.sources.remote.FirebaseNotificationDataSourceV2
import kotlinx.coroutines.flow.Flow

class NotificationRepositoryV2 private constructor(
    private val firebaseNotificationDataSource: FirebaseNotificationDataSourceV2
) {

    suspend fun getAllNotifications(): List<NotificationModelV2> {
        return firebaseNotificationDataSource.getAllNotifications()
    }

    suspend fun getUnreadNotifications(): List<NotificationModelV2> {
        return firebaseNotificationDataSource.getUnreadNotifications()
    }

    fun observeNotifications(): Flow<List<NotificationModelV2>> {
        return firebaseNotificationDataSource.observeNotifications()
    }

    suspend fun markAsRead(notificationId: String) {
        firebaseNotificationDataSource.markAsRead(notificationId)
    }

    suspend fun acceptJoinRequest(notificationId: String): Boolean {
        return firebaseNotificationDataSource.acceptJoinRequest(notificationId)
    }

    suspend fun rejectJoinRequest(notificationId: String): Boolean {
        return firebaseNotificationDataSource.rejectJoinRequest(notificationId)
    }

    suspend fun getUnreadNotificationsCount(): Int {
        return firebaseNotificationDataSource.getUnreadNotificationsCount()
    }

    suspend fun markAllAsRead() {
        firebaseNotificationDataSource.markAllAsRead()
    }

    companion object {
        @Volatile
        private var instance: NotificationRepositoryV2? = null

        fun getInstance(
            firebaseNotificationDataSource: FirebaseNotificationDataSourceV2
        ): NotificationRepositoryV2 {
            return instance ?: synchronized(this) {
                instance ?: NotificationRepositoryV2(firebaseNotificationDataSource).also { instance = it }
            }
        }
    }
}