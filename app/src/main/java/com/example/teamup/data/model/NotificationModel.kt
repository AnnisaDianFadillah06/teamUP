package com.example.teamup.data.model

import java.util.*

data class NotificationModel(
    val id: String,
    val senderName: String,
    val senderImageResId: Int,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean = false,
    val type: Type = Type.GENERAL,
    val teamId: String? = null,
    val additionalInfo: String? = null, // Could be team name, category, etc.
    val senderId: String? = null
) {
    enum class Type {
        GENERAL,        // General notification
        JOIN_REQUEST,   // Request to join a team
        INVITATION,     // Invitation to join a team
        ANNOUNCEMENT,   // Team or competition announcement
        TEAM_UPDATE     // Updates to team status
    }
}