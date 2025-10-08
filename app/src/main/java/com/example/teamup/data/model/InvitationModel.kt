package com.example.teamup.data.model

import com.google.firebase.Timestamp

data class InvitationModel(
    val id: String = "",
    val teamId: String = "",
    val teamName: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val recipientEmail: String = "",
    val status: InvitationStatus = InvitationStatus.WAITING,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class InvitationStatus {
    WAITING,
    ACCEPTED,
    REJECTED
}