package com.example.teamup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ChatMessageModel(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isCurrentUser: Boolean = false // This is UI-specific, not stored in Firestore
)