package com.example.teamup.data.model

import androidx.annotation.DrawableRes
import com.google.firebase.firestore.DocumentId

data class MemberInviteModel (
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    @DrawableRes val profileImage: Int,
    val status: String = "" // "PENDING" or "WAITING"
)
