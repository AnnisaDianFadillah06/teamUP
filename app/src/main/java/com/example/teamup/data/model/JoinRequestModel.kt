package com.example.teamup.data.model

import com.google.firebase.Timestamp

data class JoinRequestModel(
    val id: String = "",
    val teamId: String = "",
    val teamName: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterEmail: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val expiredAt: Timestamp? = null // Optional: createdAt + 7 hari
)

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}