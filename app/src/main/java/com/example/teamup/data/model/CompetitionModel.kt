package com.example.teamup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class CompetitionModel(
    @DocumentId val id: String = "", // ID dokumen otomatis dari Firestore
    val namaLomba: String = "",
    val cabangLomba: String = "",
    val tanggalPelaksanaan: String = "",
    val deskripsiLomba: String = "",
    val imageUrl: String = "", // URL untuk gambar pendukung
    val fileUrl: String = "", // URL untuk file pendukung
    val jumlahTim: Int = 0, // Jumlah tim yang ikut lomba
    val createdAt: Timestamp = Timestamp.now()
)