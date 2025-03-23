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
    val createdAt: Timestamp = Timestamp.now()
)