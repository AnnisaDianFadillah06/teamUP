package com.example.teamup.data.sources.remote

import android.content.Context
import com.example.teamup.R
import com.example.teamup.data.model.NotificationModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

/**
 * Dummy implementation of FirebaseNotificationDataSource for UI testing
 * Uses the same interface as the real implementation but with dummy data
 */
class FirebaseNotificationDataSource(private val context: Context) {
    // Mutable state flow to simulate real-time updates
    private val notificationsFlow = MutableStateFlow<List<NotificationModel>>(emptyList())

    init {
        // Initialize with dummy data
        notificationsFlow.value = createDummyNotifications()
    }

    // Get all notifications
    suspend fun getAllNotifications(): List<NotificationModel> {
        return notificationsFlow.value
    }

    // Get unread notifications
    suspend fun getUnreadNotifications(): List<NotificationModel> {
        return notificationsFlow.value.filter { !it.isRead }
    }

    // Observe notifications in real-time
    fun observeNotifications(): Flow<List<NotificationModel>> {
        return notificationsFlow
    }

    // Mark notification as read
    suspend fun markAsRead(notificationId: String) {
        val notifications = notificationsFlow.value.toMutableList()
        val index = notifications.indexOfFirst { it.id == notificationId }

        if (index != -1) {
            val notification = notifications[index]
            notifications[index] = notification.copy(isRead = true)
            notificationsFlow.value = notifications
        }
    }

    // Accept join request
    suspend fun acceptJoinRequest(notificationId: String) {
        // Find the notification
        val joinRequest = notificationsFlow.value.find { it.id == notificationId }

        // Mark it as read
        markAsRead(notificationId)

        // Create a confirmation notification
        joinRequest?.let {
            val confirmationNotification = NotificationModel(
                id = UUID.randomUUID().toString(),
                senderName = "System",
                senderImageResId = R.drawable.captain_icon,
                message = "telah menerima permintaan bergabung tim ${it.additionalInfo ?: "Tim"}.",
                timestamp = Date(),
                isRead = false,
                type = NotificationModel.Type.GENERAL,
                teamId = it.teamId,
                additionalInfo = it.additionalInfo,
                senderId = "system"
            )

            notificationsFlow.update { currentList ->
                currentList + confirmationNotification
            }
        }
    }

    // Reject join request
    suspend fun rejectJoinRequest(notificationId: String) {
        // Find the notification
        val joinRequest = notificationsFlow.value.find { it.id == notificationId }

        // Mark it as read
        markAsRead(notificationId)

        // Create a rejection notification
        joinRequest?.let {
            val rejectionNotification = NotificationModel(
                id = UUID.randomUUID().toString(),
                senderName = "System",
                senderImageResId = R.drawable.captain_icon,
                message = "telah menolak permintaan bergabung tim ${it.additionalInfo ?: "Tim"}.",
                timestamp = Date(),
                isRead = false,
                type = NotificationModel.Type.GENERAL,
                teamId = it.teamId,
                additionalInfo = it.additionalInfo,
                senderId = "system"
            )

            notificationsFlow.update { currentList ->
                currentList + rejectionNotification
            }
        }
    }

    // Get count of unread notifications
    suspend fun getUnreadNotificationsCount(): Int {
        return notificationsFlow.value.count { !it.isRead }
    }

    // Mark all notifications as read
    suspend fun markAllAsRead() {
        val updatedNotifications = notificationsFlow.value.map { notification ->
            if (!notification.isRead) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }

        notificationsFlow.value = updatedNotifications
    }

    // Create dummy notifications for testing the UI
    private fun createDummyNotifications(): List<NotificationModel> {
        val calendar = Calendar.getInstance()

        return listOf(
            // Unread notifications
            NotificationModel(
                id = "1",
                senderName = "Budi Santoso",
                senderImageResId = R.drawable.captain_icon,
                message = "mengajukan bergabung dengan tim anda",
                timestamp = calendar.time,
                isRead = false,
                type = NotificationModel.Type.JOIN_REQUEST,
                additionalInfo = "Coding Competition 2023",
                teamId = "team1",
                senderId = "user1"
            ),

            NotificationModel(
                id = "2",
                senderName = "Design Competition",
                senderImageResId = R.drawable.captain_icon,
                message = "mengumumkan update penting",
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.HOUR, -3)
                }.time,
                isRead = false,
                type = NotificationModel.Type.ANNOUNCEMENT,
                additionalInfo = "Deadline pengumpulan karya diperpanjang hingga 20 Mei 2023",
                teamId = null,
                senderId = "system"
            ),

            NotificationModel(
                id = "3",
                senderName = "Dewi Lestari",
                senderImageResId = R.drawable.captain_icon,
                message = "mengundang anda bergabung dengan tim",
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                }.time,
                isRead = false,
                type = NotificationModel.Type.INVITATION,
                additionalInfo = "UI/UX Challenge 2023",
                teamId = "team2",
                senderId = "user2"
            ),

            // Read notifications
            NotificationModel(
                id = "4",
                senderName = "Ahmad Rizky",
                senderImageResId = R.drawable.captain_icon,
                message = "mengajukan bergabung dengan tim anda",
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -2)
                }.time,
                isRead = true,
                type = NotificationModel.Type.JOIN_REQUEST,
                additionalInfo = "Mobile App Development",
                teamId = "team3",
                senderId = "user3"
            ),

            NotificationModel(
                id = "5",
                senderName = "Tim Anda",
                senderImageResId = R.drawable.captain_icon,
                message = "telah berhasil terdaftar untuk lomba",
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -4)
                }.time,
                isRead = true,
                type = NotificationModel.Type.TEAM_UPDATE,
                additionalInfo = "Hackathon 2023",
                teamId = "yourTeam1",
                senderId = "system"
            ),

            NotificationModel(
                id = "6",
                senderName = "System",
                senderImageResId = R.drawable.captain_icon,
                message = "Ada lomba baru yang sesuai dengan minat anda",
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -5)
                }.time,
                isRead = true,
                type = NotificationModel.Type.GENERAL,
                additionalInfo = "Lomba Game Development 2023",
                teamId = null,
                senderId = "system"
            ),

            NotificationModel(
                id = "7",
                senderName = "Rina Wijaya",
                senderImageResId = R.drawable.captain_icon,
                message = "mengirim pesan kepada tim anda",
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -7)
                }.time,
                isRead = true,
                type = NotificationModel.Type.TEAM_UPDATE,
                additionalInfo = "Bisakah kita jadwalkan meeting untuk membahas strategi lomba?",
                teamId = "yourTeam1",
                senderId = "user4"
            ),

            NotificationModel(
                id = "8",
                senderName = "Programming Contest",
                senderImageResId = R.drawable.captain_icon,
                message = "telah mengubah persyaratan lomba",
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                }.time,
                isRead = true,
                type = NotificationModel.Type.ANNOUNCEMENT,
                additionalInfo = "Maksimal anggota tim sekarang adalah 5 orang",
                teamId = null,
                senderId = "system"
            ),

            NotificationModel(
                id = "9",
                senderName = "Dimas Prabowo",
                senderImageResId = R.drawable.captain_icon,
                message = "telah bergabung dengan tim anda",
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    add(Calendar.DAY_OF_MONTH, -5)
                }.time,
                isRead = true,
                type = NotificationModel.Type.TEAM_UPDATE,
                additionalInfo = "Backend Developer Contest",
                teamId = "yourTeam2",
                senderId = "user5"
            ),

            NotificationModel(
                id = "10",
                senderName = "Team Building Workshop",
                senderImageResId = R.drawable.captain_icon,
                message = "akan dimulai dalam 2 jam",
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -2)
                }.time,
                isRead = true,
                type = NotificationModel.Type.GENERAL,
                additionalInfo = "Silakan bergabung melalui link berikut: meetup.com/workshop",
                teamId = null,
                senderId = "system"
            )
        )
    }
}