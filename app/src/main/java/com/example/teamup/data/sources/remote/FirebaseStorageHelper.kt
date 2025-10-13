package com.example.teamup.data.sources.remote

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageHelper {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    /**
     * Uploads an image to Firebase Storage and returns the download URL
     */
    suspend fun uploadImage(imageUri: Uri, folderPath: String = "team_avatars"): String {
        val filename = UUID.randomUUID().toString()
        val fileRef = storageRef.child("$folderPath/$filename")

        return try {
            val uploadTask = fileRef.putFile(imageUri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e
        }
    }
}