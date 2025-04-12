package com.example.teamup.common.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.teamup.data.model.CompetitionActivityStatus
import com.example.teamup.data.model.CompetitionVisibilityStatus
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Uploads a competition with optional image and file attachments
 */
fun uploadCompetitionWithMedia(
    context: Context,
    imageUri: Uri?,
    fileUri: Uri?,
    namaLomba: String,
    cabangLomba: List<String>,
    tanggalPelaksanaan: String,
    deskripsiLomba: String,
    jumlahTim: Int = 0,
    visibilityStatus: String = CompetitionVisibilityStatus.PUBLISHED.value,
    activityStatus: String = CompetitionActivityStatus.ACTIVE.value,
    tanggalTutupPendaftaran: String? = null,
    autoCloseEnabled: Boolean = false,
    viewModel: CompetitionViewModel,
    onComplete: () -> Unit
) {
    // Using Kotlin Coroutines
    MainScope().launch {
        try {
            // Upload image if exists
            var imageUrl = ""
            if (imageUri != null) {
                try {
                    val imageName = "competitions/images/${UUID.randomUUID()}"
                    val storageRef = FirebaseStorage.getInstance().reference.child(imageName)

                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                            storageRef.putStream(inputStream).await()
                            imageUrl = storageRef.downloadUrl.await().toString()
                        } ?: throw Exception("Tidak dapat mengakses file gambar")
                    }
                } catch (e: Exception) {
                    viewModel.setError("Gagal mengupload gambar: ${e.message}")
                    onComplete()
                    return@launch
                }
            }

            // Upload file if exists
            var fileUrl = ""
            var fileName = ""
            if (fileUri != null) {
                try {
                    // Get file name
                    context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex >= 0) {
                                fileName = cursor.getString(nameIndex)
                            }
                        }
                    }

                    val storagePath = "competitions/files/${UUID.randomUUID()}_$fileName"
                    val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)

                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                            storageRef.putStream(inputStream).await()
                            fileUrl = storageRef.downloadUrl.await().toString()
                        } ?: throw Exception("Tidak dapat mengakses file dokumen")
                    }
                } catch (e: Exception) {
                    viewModel.setError("Gagal mengupload dokumen: ${e.message}")
                    onComplete()
                    return@launch
                }
            }

            // Add competition data to Firestore
            viewModel.addCompetition(
                namaLomba = namaLomba,
                cabangLombaList = cabangLomba,
                tanggalPelaksanaan = tanggalPelaksanaan,
                deskripsiLomba = deskripsiLomba,
                imageUrl = imageUrl,
                fileUrl = fileUrl,
                jumlahTim = jumlahTim,
                visibilityStatus = visibilityStatus,
                activityStatus = activityStatus,
                tanggalTutupPendaftaran = tanggalTutupPendaftaran,
                autoCloseEnabled = autoCloseEnabled
            )

        } catch (e: Exception) {
            viewModel.setError("Error: ${e.message}")
        } finally {
            onComplete()
        }
    }
}