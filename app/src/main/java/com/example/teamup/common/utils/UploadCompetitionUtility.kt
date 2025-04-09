package com.example.teamup.common.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

fun uploadCompetitionWithMedia(
    context: Context,
    imageUri: Uri?,
    fileUri: Uri?,
    namaLomba: String,
    cabangLomba: List<String>,  // Diubah dari String menjadi List<String>
    tanggalPelaksanaan: String,
    deskripsiLomba: String,
    jumlahTim: Int,
    status: String = "Published",
    viewModel: CompetitionViewModel,
    onComplete: () -> Unit
) {
    val storageRef = Firebase.storage.reference
    var imageUrl = ""
    var fileUrl = ""
    var imageUploaded = false
    var fileUploaded = false
    var uploadErrors = 0

    // Function to check if both uploads are complete and add competition
    fun checkAndAddCompetition() {
        // If there are upload errors, don't add the competition
        if (uploadErrors > 0) {
            onComplete()
            return
        }

        val shouldAddWithImage = imageUri != null
        val shouldAddWithFile = fileUri != null

        // If both required files are uploaded or not needed, add the competition
        if ((shouldAddWithImage && imageUploaded || !shouldAddWithImage) &&
            (shouldAddWithFile && fileUploaded || !shouldAddWithFile)) {
            viewModel.addCompetition(
                namaLomba = namaLomba,
                cabangLombaList = cabangLomba, // Langsung gunakan list yang diberikan
                tanggalPelaksanaan = tanggalPelaksanaan,
                deskripsiLomba = deskripsiLomba,
                imageUrl = imageUrl,
                fileUrl = fileUrl,
                jumlahTim = jumlahTim,
                status = status
            )
            onComplete()
        }
    }

    // If no files to upload, add competition directly
    if (imageUri == null && fileUri == null) {
        viewModel.addCompetition(
            namaLomba = namaLomba,
            cabangLombaList = cabangLomba, // Langsung gunakan list yang diberikan
            tanggalPelaksanaan = tanggalPelaksanaan,
            deskripsiLomba = deskripsiLomba,
            imageUrl = imageUrl,
            fileUrl = fileUrl,
            jumlahTim = jumlahTim,
            status = status
        )
        onComplete()
        return
    }

    // Upload image if available
    if (imageUri != null) {
        try {
            // Get content resolver to verify access
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(imageUri)

            if (mimeType?.startsWith("image/") == true) {
                // Create a temporary file to handle potential content URI issues
                val inputStream = contentResolver.openInputStream(imageUri)
                inputStream?.use { input ->
                    val imageRef = storageRef.child("competitions/images/${UUID.randomUUID()}.jpg")

                    // Start the upload task
                    imageRef.putStream(input)
                        .addOnFailureListener { exception ->
                            uploadErrors++
                            viewModel.setError("Gagal mengupload gambar: ${exception.message}")
                            onComplete()
                        }
                        .addOnSuccessListener {
                            // Get download URL after successful upload
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                imageUrl = uri.toString()
                                imageUploaded = true
                                checkAndAddCompetition()
                            }.addOnFailureListener { exception ->
                                uploadErrors++
                                viewModel.setError("Gagal mendapatkan URL gambar: ${exception.message}")
                                onComplete()
                            }
                        }
                }
            } else {
                uploadErrors++
                viewModel.setError("Format file gambar tidak didukung")
                onComplete()
            }
        } catch (e: Exception) {
            uploadErrors++
            viewModel.setError("Error akses file gambar: ${e.message}")
            onComplete()
        }
    } else {
        imageUploaded = true
    }

    // Upload file if available
    if (fileUri != null) {
        try {
            // Get content resolver
            val contentResolver = context.contentResolver

            // Get file name and extension
            var fileName = "document"
            contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }

            // Handle the upload with input stream
            contentResolver.openInputStream(fileUri)?.use { inputStream ->
                val fileRef = storageRef.child("competitions/files/${UUID.randomUUID()}_$fileName")

                // Start upload task
                fileRef.putStream(inputStream)
                    .addOnFailureListener { exception ->
                        uploadErrors++
                        viewModel.setError("Gagal mengupload file: ${exception.message}")
                        onComplete()
                    }
                    .addOnSuccessListener {
                        // Get download URL after successful upload
                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            fileUrl = uri.toString()
                            fileUploaded = true
                            checkAndAddCompetition()
                        }.addOnFailureListener { exception ->
                            uploadErrors++
                            viewModel.setError("Gagal mendapatkan URL file: ${exception.message}")
                            onComplete()
                        }
                    }
            } ?: run {
                uploadErrors++
                viewModel.setError("Tidak dapat mengakses file")
                onComplete()
            }
        } catch (e: Exception) {
            uploadErrors++
            viewModel.setError("Error akses dokumen: ${e.message}")
            onComplete()
        }
    } else {
        fileUploaded = true
    }
}