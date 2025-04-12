package com.example.teamup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class CabangLombaModel(
    @DocumentId val id: String = "",
    val competitionId: String = "", // Reference to the competition
    val namaCabang: String = "",
    val createdAt: Timestamp = Timestamp.now()
)