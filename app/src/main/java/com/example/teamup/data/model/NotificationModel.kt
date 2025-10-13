package com.example.teamup.data.model

import com.google.firebase.Timestamp
import java.util.*

// Tambah di enum NotificationType
enum class NotificationType {
    JOIN_REQUEST,       // Admin terima notif ada yang mau join
    JOIN_APPROVED,      // User terima notif request di-approve
    JOIN_REJECTED,      // User terima notif request di-reject
    INVITE,             // User terima invite dari admin
    INVITE_ACCEPTED,    // Admin terima notif invite di-accept
    INVITE_REJECTED,    // Admin terima notif invite di-reject
    GENERAL             // Existing
}

// Update NotificationModel
data class NotificationModel(
    val id: String = "",
    val userId: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val title: String = "",
    val message: String = "",
    val relatedId: String = "",  // teamId, requestId, atau inviteId
    val relatedType: String = "", // "TEAM", "REQUEST", "INVITE"
    val senderName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val actionData: Map<String, String>? = null // untuk data tambahan
)