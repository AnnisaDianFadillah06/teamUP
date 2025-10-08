package com.example.teamup.data.repositories

import com.example.teamup.data.model.NotificationModel
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun getAllNotifications(): List<NotificationModel>
    suspend fun getUnreadNotifications(): List<NotificationModel>
    fun observeNotifications(): Flow<List<NotificationModel>>
    suspend fun markAsRead(notificationId: String)
    suspend fun acceptJoinRequest(notificationId: String)
    suspend fun rejectJoinRequest(notificationId: String)
    suspend fun getUnreadNotificationsCount(): Int
    suspend fun markAllAsRead()

    // TAMBAH METHOD BARU:
    suspend fun createNotification(notification: NotificationModel): Result<String>
}