package com.example.teamup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class MemberInviteModelV2(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val profileImageRes: Int = 0,
    val status: String = "", // "PENDING", "WAITING", "ACCEPTED", "REJECTED"
    val teamId: String = "",
    val teamName: String = "",
    val university: String = "",
    val major: String = "",
    val skills: List<String> = emptyList()
)