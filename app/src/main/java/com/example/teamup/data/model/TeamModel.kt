package com.example.teamup.data.model

import androidx.annotation.DrawableRes
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class TeamModel(
    @DocumentId val id: String = "", // ID dokumen otomatis dari Firestore
    val name: String = "",
    val description: String = "",
    val category: String = "",
    @DrawableRes val avatarResId: Int, // Local resource as fallback
    val imageUrl: String? = null, // URL to Firebase Storage image
    val createdAt: Timestamp = Timestamp.now(),
    val isJoined: Boolean = false,
    val isFull: Boolean = false,
    val memberCount: Int = 0,
    val maxMembers: Int = 5
    //isJoined: To track whether the current user has joined this team
    //isFull: To indicate if the team has reached its maximum capacity
    //memberCount: The current number of team members
    //maxMembers: The maximum number of members allowed in the team (default 5)
)