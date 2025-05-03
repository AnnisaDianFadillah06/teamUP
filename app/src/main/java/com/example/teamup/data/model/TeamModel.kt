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
    val driveFileId: String? = null, // ID file di Google Drive
    val createdAt: Timestamp = Timestamp.now(),
    val members: List<String> = emptyList(), // List of member user IDs
    val maxMembers: Int = 5, // Maximum members allowed (default 5)
    val isPrivate: Boolean = true, // Team privacy setting
    val memberCount: Int = 0, // Current number of members
    val isJoined: Boolean = false, // Whether current user has joined
    val isFull: Boolean = false, // Whether team has reached capacity
    val captainId: String = "" // ID of the team captain/admin
)