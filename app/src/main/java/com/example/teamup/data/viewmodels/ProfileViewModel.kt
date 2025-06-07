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

    // Save profile with image
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
            // 1. Upload with unique path
            val ref = storage.reference
                .child("profile_images/$userId/${UUID.randomUUID()}.jpg")
            ref.putFile(imageUri).await()

            // 2. Get URL
            val downloadUrl = ref.downloadUrl.await().toString()

            // Get current user data to preserve other fields
            val currentUserData = _userData.value

            // 3. Update Firestore, ensure we maintain all fields
            val updates = mutableMapOf<String, Any>().apply {
                // Only update fields the user has modified
                put("university", university)
                put("major", major)
                put("skills", skills)
                put("profilePictureUrl", downloadUrl)
                put("profileCompleted", true)

                // Preserve existing fields if available
                currentUserData?.let {
                    put("fullName", it.fullName)
                    put("username", it.username)
                    put("phone", it.phone)
                }
            }

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
            _errorMessage.value = "Failed to upload image: ${e.message}"
            Log.e("ProfileViewModel", "Failed to upload image", e)
            callback(false)
        } catch (e: Exception) {
            _errorMessage.value = "Error saving profile: ${e.message}"
            Log.e("ProfileViewModel", "Error saving profile", e)
            callback(false)
        } finally {
            _isLoading.value = false
        }
    }

    // Update specific user fields
    fun updateUserProfile(
        userId: String,
        updates: Map<String, Any>,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()

            // Update local state with the new values
            val currentData = _userData.value ?: return@launch

            // Create an updated copy with modified fields
            val updatedData = currentData.copy(
                fullName = updates["fullName"] as? String ?: currentData.fullName,
                username = updates["username"] as? String ?: currentData.username,
                phone = updates["phone"] as? String ?: currentData.phone,
                university = updates["university"] as? String ?: currentData.university,
                major = updates["major"] as? String ?: currentData.major,
                skills = updates["skills"] as? List<String> ?: currentData.skills
            )

            _userData.value = updatedData
            callback(true)
        } catch (e: Exception) {
            _errorMessage.value = "Error updating profile: ${e.message}"
            Log.e("ProfileViewModel", "Error updating profile", e)
            callback(false)
        } finally {
            _isLoading.value = false
        }
    }

    // Enhanced saveUserProfile with comprehensive fields
    fun saveUserProfile(
        userId: String,
        fullName: String,
        username: String,
        email: String,
        phone: String,
        university: String,
        major: String,
        skills: List<String>,
        imageUri: Uri?, // Optional URI for image update
        callback: (Boolean) -> Unit
    ) {
        _isLoading.value = true

        try {
            // Create map of data to update
            val profileData = mutableMapOf<String, Any>().apply {
                put("fullName", fullName)
                put("username", username)
                put("email", email)
                put("phone", phone)
                put("university", university)
                put("major", major)
                put("skills", skills)
                put("profileCompleted", true)
            }

            // If imageUri is not null, handle the image upload first
            if (imageUri != null) {
                viewModelScope.launch {
                    try {
                        val ref = storage.reference
                            .child("profile_images/$userId/${UUID.randomUUID()}.jpg")
                        ref.putFile(imageUri).await()

                        // Get the download URL and add to profile data
                        val downloadUrl = ref.downloadUrl.await().toString()
                        profileData["profilePictureUrl"] = downloadUrl

                        // Now update Firestore with all data including new image URL
                        updateFirestoreAndState(userId, profileData, callback)
                    } catch (e: Exception) {
                        _errorMessage.value = "Failed to upload image: ${e.message}"
                        _isLoading.value = false
                        callback(false)
                    }
                }
            } else {
                // Preserve existing profilePictureUrl
                _userData.value?.profilePictureUrl?.let {
                    if (it.isNotEmpty()) {
                        profileData["profilePictureUrl"] = it
                    }
                }

                // Update Firestore without changing the image
                updateFirestoreAndState(userId, profileData, callback)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error saving profile: ${e.message}"
            _isLoading.value = false
            callback(false)
        }
    }

    // Helper method to update Firestore and local state
    private fun updateFirestoreAndState(
        userId: String,
        profileData: Map<String, Any>,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        try {
            firestore.collection("users")
                .document(userId)
                .update(profileData)
                .await()

            // Update local state
            _userData.value = _userData.value?.copy(
                fullName = profileData["fullName"] as String,
                username = profileData["username"] as String,
                email = profileData["email"] as String,
                phone = profileData["phone"] as String,
                university = profileData["university"] as String,
                major = profileData["major"] as String,
                skills = profileData["skills"] as List<String>,
                profilePictureUrl = profileData["profilePictureUrl"] as? String ?: _userData.value?.profilePictureUrl ?: "",
                profileCompleted = true
            )

            _isLoading.value = false
            callback(true)
        } catch (e: Exception) {
            _errorMessage.value = "Error updating profile: ${e.message}"
            _isLoading.value = false
            callback(false)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}