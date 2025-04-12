package com.example.teamup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class CompetitionModel(
    @DocumentId val id: String = "", // ID dokumen otomatis dari Firestore
    val namaLomba: String = "",
    val tanggalPelaksanaan: String = "",
    val deskripsiLomba: String = "",
    val jumlahTim: Int = 0, // Jumlah tim yang ikut lomba
    val imageUrl: String = "", // URL untuk gambar pendukung
    val fileUrl: String = "", // URL untuk file pendukung
    val createdAt: Timestamp = Timestamp.now(),
    val visibilityStatus: String = CompetitionVisibilityStatus.PUBLISHED.value,
    val activityStatus: String = CompetitionActivityStatus.ACTIVE.value,
    val tanggalTutupPendaftaran: Timestamp? = null, // Tanggal batas pendaftaran
    val autoCloseEnabled: Boolean = false // Kontrol untuk auto close berdasarkan tanggal
) {
    // Utility function to check if registration should be auto-closed
    fun shouldBeInactive(): Boolean {
        if (!autoCloseEnabled || tanggalTutupPendaftaran == null) return false

        val now = Timestamp.now()
        return now.seconds > tanggalTutupPendaftaran.seconds
    }
}