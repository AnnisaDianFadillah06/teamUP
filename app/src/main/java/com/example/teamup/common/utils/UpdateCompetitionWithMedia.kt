package com.example.teamup.common.utils

import android.content.Context
import android.net.Uri
import com.example.teamup.data.model.CompetitionActivityStatus
import com.example.teamup.data.model.CompetitionVisibilityStatus
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

fun updateCompetitionWithMedia(
    context: Context,
    competitionId: String,
    imageUri: Uri?,
    fileUri: Uri?,
    namaLomba: String,
    cabangLomba: List<String>,
    tanggalPelaksanaan: String,
    deskripsiLomba: String,
//    jumlahTim: Int,
    currentImageUrl: String? = null,
    currentFileUrl: String? = null,
    visibilityStatus: String = CompetitionVisibilityStatus.PUBLISHED.value,
    activityStatus: String = CompetitionActivityStatus.ACTIVE.value,
    tanggalTutupPendaftaran: String? = null,
    autoCloseEnabled: Boolean = false,
    viewModel: CompetitionViewModel,
    keepExistingCabang: Boolean = false, // Add this parameter with default value
    onComplete: () -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Initialize storage
            val storage = FirebaseStorage.getInstance()

            // Variables to store updated URLs
            var updatedImageUrl = currentImageUrl ?: ""
            var updatedFileUrl = currentFileUrl ?: ""

            // Upload image if selected
            if (imageUri != null) {
                val imageRef = storage.reference.child("competition_images/${UUID.randomUUID()}")
                val uploadTask = imageRef.putFile(imageUri)

                // Wait for upload to complete
                uploadTask.await()

                // Get download URL
                updatedImageUrl = imageRef.downloadUrl.await().toString()
            }

            // Upload file if selected
            if (fileUri != null) {
                val fileRef = storage.reference.child("competition_files/${UUID.randomUUID()}")
                val uploadTask = fileRef.putFile(fileUri)

                // Wait for upload to complete
                uploadTask.await()

                // Get download URL
                updatedFileUrl = fileRef.downloadUrl.await().toString()
            }

            // Update competition in Firestore
            viewModel.updateCompetition(
                competitionId = competitionId,
                namaLomba = namaLomba,
                cabangLombaList = cabangLomba,
                tanggalPelaksanaan = tanggalPelaksanaan,
                deskripsiLomba = deskripsiLomba,
                imageUrl = updatedImageUrl,
                fileUrl = updatedFileUrl,
//                jumlahTim = jumlahTim,
                visibilityStatus = visibilityStatus,
                activityStatus = activityStatus,
                tanggalTutupPendaftaran = tanggalTutupPendaftaran,
                autoCloseEnabled = autoCloseEnabled
            )

            // Call onComplete on the main thread
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                onComplete()
            }
        } catch (e: Exception) {
            // Handle errors
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                viewModel.setError("Error updating competition: ${e.message}")
                onComplete()
            }
        }
    }
}