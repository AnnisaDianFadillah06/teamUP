package com.example.teamup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class NotificationModelV2(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImageUrl: String = "",
    val recipientId: String = "",
    val message: String = "",
    val type: String = "", // "INVITATION", "JOIN_REQUEST", "TEAM_UPDATE", "GENERAL"
    val isRead: Boolean = false,
    val teamId: String = "",
    val teamName: String = "",
    val additionalInfo: String = "",
    val inviteId: String = "", // For invitation-related notifications
    val timestamp: com.google.firebase.Timestamp? = null
)
