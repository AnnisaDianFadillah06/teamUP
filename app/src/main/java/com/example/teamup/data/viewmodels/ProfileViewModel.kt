package com.example.teamup.data.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.UserProfile
import com.example.teamup.data.model.UserProfileData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


class ProfileViewModel : ViewModel() {

    private val _userData = MutableStateFlow<UserProfileData?>(null)
    val userData: StateFlow<UserProfileData?> = _userData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getUserData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                if (doc.exists()) {
                    _userData.value = UserProfileData(
                        userId = userId,
                        fullName = doc.getString("fullName") ?: "",
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        university = doc.getString("university") ?: "",
                        major = doc.getString("major") ?: "",
                        skills = doc.get("skills") as? List<String> ?: emptyList(),
                        profilePictureUrl = doc.getString("profilePictureUrl") ?: "",
                        profileCompleted = doc.getBoolean("profileCompleted") ?: false
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching user data: ${e.message}"
                Log.e("ProfileViewModel", "Error fetching user data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCompleteProfile(
        userId: String,
        university: String,
        major: String,
        skills: List<String>,
        imageUri: Uri,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            // 1. Upload dengan path unik
            val ref = storage.reference
                .child("profile_images/$userId/${UUID.randomUUID()}.jpg")
            ref.putFile(imageUri).await()

            // 2. Ambil URL
            val downloadUrl = ref.downloadUrl.await().toString()

            // 3. Update Firestore, pastikan key sama
            val updates = mapOf(
                "university" to university,
                "major" to major,
                "skills" to skills,
                "profilePictureUrl" to downloadUrl,
                "profileCompleted" to true
            )
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()

            // 4. Update local state
            _userData.value = _userData.value?.copy(
                university = university,
                major = major,
                skills = skills,
                profilePictureUrl = downloadUrl,
                profileCompleted = true
            )
            callback(true)
        } catch (e: StorageException) {
            _errorMessage.value = "Gagal mengunggah gambar: ${e.message}"
            callback(false)
        } catch (e: Exception) {
            _errorMessage.value = "Error saving profile: ${e.message}"
            callback(false)
        } finally {
            _isLoading.value = false
        }
    }

    // Fixed saveUserProfile method - Firestore only, no image upload
    fun saveUserProfile(
        userId: String,
        fullName: String,
        username: String,
        email: String,
        phone: String,
        university: String,
        major: String,
        skills: List<String>,
        imageUri: Uri?, // Optional URI, not used in this implementation
        callback: (Boolean) -> Unit
    ) {
        _isLoading.value = true

        try {
            // Create user profile object with the correct field names
            val profile = UserProfile(
                uid = userId,
                fullName = fullName,
                username = username,
                email = email,
                phone = phone,
                university = university,
                major = major,
                skills = skills,
                profilePictureUrl = null // No profile picture URL initially
            )

            // Extra field for profile completion status
            val profileData = profile.toMap().toMutableMap()
            profileData["profileCompleted"] = true

            // Save to Firestore
            firestore.collection("users")
                .document(userId)
                .set(profileData)
                .addOnSuccessListener {
                    _isLoading.value = false
                    callback(true)
                }
                .addOnFailureListener { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                    callback(false)
                }
        } catch (e: Exception) {
            _errorMessage.value = "Error saving profile: ${e.message}"
            _isLoading.value = false
            callback(false)
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }
}