package com.example.teamup.data.model

data class ChatMessageModel(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long,
    val isCurrentUser: Boolean
)
