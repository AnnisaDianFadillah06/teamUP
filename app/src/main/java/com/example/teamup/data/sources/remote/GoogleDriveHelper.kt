package com.example.teamup.data.sources.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream

class GoogleDriveHelper(private val context: Context) {

    private var drive: Drive? = null
    private var isInitialized = false

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("service-account-key.json")
                val credential = GoogleCredential.fromStream(inputStream)
                    .createScoped(listOf(DriveScopes.DRIVE))

                drive = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName("TeamUp")
                    .build()

                isInitialized = true
                Log.d("GoogleDriveHelper", "Google Drive initialized with Service Account")

            } catch (e: Exception) {
                Log.d("GoogleDriveHelper", "Failed to initialize Google Drive: ${e.message}")
                isInitialized = false
                throw e
            }
        }
    }

    fun isInitialized(): Boolean = isInitialized && drive != null

    suspend fun uploadTeamProfileImage(teamId: String, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (drive == null) {
                    Log.d("GoogleDriveHelper", "Drive client is null, trying to initialize")
                    initialize()
                    if (drive == null) throw Exception("Drive client could not be initialized")
                }

                val driveService = drive!!
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
                val fileName = "team_${teamId}_${System.currentTimeMillis()}.${mimeType.split("/").last()}"

                Log.d("GoogleDriveHelper", "Uploading image: $fileName, mimeType: $mimeType")

                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = fileName

                val inputStream = contentResolver.openInputStream(imageUri)
                    ?: throw Exception("Could not open input stream for URI: $imageUri")
                val bufferedInputStream = BufferedInputStream(inputStream)
                val mediaContent = InputStreamContent(mimeType, bufferedInputStream)

                val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()

                Log.d("GoogleDriveHelper", "File uploaded with ID: ${uploadedFile.id}")

                val permission = com.google.api.services.drive.model.Permission()
                    .setType("anyone")
                    .setRole("reader")

                driveService.permissions().create(uploadedFile.id, permission).execute()

                Log.d("GoogleDriveHelper", "Permissions set to public for file: ${uploadedFile.id}")

                uploadedFile.id
            } catch (e: Exception) {
                Log.d("GoogleDriveHelper", "Error uploading image to Google Drive: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    fun getFileUrl(fileId: String?): String? {
        return fileId?.let { "https://drive.google.com/uc?id=$it" }
    }

    suspend fun deleteFile(fileId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (drive == null) throw Exception("Drive client not initialized")
                drive!!.files().delete(fileId).execute()
                Log.d("GoogleDriveHelper", "File deleted successfully: $fileId")
                true
            } catch (e: Exception) {
                Log.d("GoogleDriveHelper", "Error deleting file: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }
}
