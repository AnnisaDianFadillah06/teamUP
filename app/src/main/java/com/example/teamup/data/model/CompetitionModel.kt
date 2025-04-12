package com.example.teamup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class CompetitionModel(
    @DocumentId val id: String = "", // ID dokumen otomatis dari Firestore
    val namaLomba: String = "",
    val tanggalPelaksanaan: String = "",
    val deskripsiLomba: String = "",
//    val jumlahTim: Int = 0, // Jumlah tim yang ikut lomba
    val imageUrl: String = "", // URL untuk gambar pendukung
    val fileUrl: String = "", // URL untuk file pendukung
    val createdAt: Timestamp = Timestamp.now(),
    val visibilityStatus: String = CompetitionVisibilityStatus.PUBLISHED.value,
    val activityStatus: String = CompetitionActivityStatus.ACTIVE.value,
    val tanggalTutupPendaftaran: Timestamp? = null, // Tanggal batas pendaftaran
    val autoCloseEnabled: Boolean = false, // Kontrol untuk auto close berdasarkan tanggal

    // Transient field that won't be stored in Firestore
    @get:Exclude val cabangLomba: List<String> = emptyList()
) {
    // Utility function to check if registration should be auto-closed
    fun shouldBeInactive(): Boolean {
        if (!autoCloseEnabled || tanggalTutupPendaftaran == null) return false

        val now = Timestamp.now()
        return now.seconds > tanggalTutupPendaftaran.seconds
    }

    // Helper method to get formatted deadline date for UI display
    fun getFormattedDeadline(): String {
        if (tanggalTutupPendaftaran == null) return ""

        val date = tanggalTutupPendaftaran.toDate()
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return dateFormat.format(date)
    }

    // Helper method to get deadline in ISO format for date pickers
    fun getISODeadline(): String {
        if (tanggalTutupPendaftaran == null) return ""

        val date = tanggalTutupPendaftaran.toDate()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        return dateFormat.format(date)
    }
}