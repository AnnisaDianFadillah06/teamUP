package com.example.teamup.data.repositories

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Uploads a profile image to Firebase Storage and returns its download URL
     */
    fun uploadProfileImage(userId: String, imageUri: Uri, callback: (String?, Boolean) -> Unit) {
        val storageRef = storage.reference
            .child("profile_images/$userId/${UUID.randomUUID()}")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        callback(uri.toString(), true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProfileRepository", "Failed to get download URL: ${e.message}")
                        callback(null, false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileRepository", "Failed to upload image: ${e.message}")
                callback(null, false)
            }
    }

    /**
     * Saves or updates the given UserProfile in Firestore
     */
    fun saveUserProfile(userProfile: UserProfile, callback: (Boolean) -> Unit) {
        val userId = if (userProfile.uid.isNotEmpty()) userProfile.uid else auth.currentUser?.uid

        if (userId.isNullOrEmpty()) {
            Log.e("ProfileRepository", "User ID is null or empty")
            callback(false)
            return
        }

        val data = userProfile.toMap()
        db.collection("users")
            .document(userId)
            .set(data)
            .addOnSuccessListener {
                Log.d("ProfileRepository", "Profile successfully saved for $userId")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("ProfileRepository", "Error saving profile: ${e.message}")
                callback(false)
            }
    }

    /**
     * Retrieves the UserProfile for the given userId from Firestore
     */
    fun getUserProfile(userId: String, callback: (UserProfile?, Boolean) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = document.toObject(UserProfile::class.java)
                    callback(profile, profile != null)
                } else {
                    callback(null, false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileRepository", "Error getting profile: ${e.message}")
                callback(null, false)
            }
    }
}
