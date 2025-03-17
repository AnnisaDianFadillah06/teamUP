package com.example.teamup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class TeamModel(
    @DocumentId val id: String = "", // ID dokumen otomatis dari Firestore
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val createdAt: Timestamp = Timestamp.now()
)