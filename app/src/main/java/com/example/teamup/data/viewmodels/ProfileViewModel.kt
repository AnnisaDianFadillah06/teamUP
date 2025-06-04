//ProfileViewModel.kt
package com.example.teamup.data.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.Experience
import com.example.teamup.data.model.UserProfile
import com.example.teamup.data.model.UserProfileData
import com.example.teamup.data.model.Education
import com.example.teamup.data.model.toEducation
import com.example.teamup.data.model.toExperience
import com.example.teamup.data.model.toMap
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
                    // Parse education data
                    val educationMap = doc.get("education") as? Map<String, Any>
                    val education = educationMap?.toEducation()

                    // Parse experiences data
                    val experiencesData = doc.get("experiences") as? List<Map<String, Any>> ?: emptyList()
                    val experiences = experiencesData.map { it.toExperience() }

                    _userData.value = UserProfileData(
                        userId = userId,
                        fullName = doc.getString("fullName") ?: "",
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        skills = doc.get("skills") as? List<String> ?: emptyList(),
                        profilePictureUrl = doc.getString("profilePictureUrl") ?: "",
                        profileCompleted = doc.getBoolean("profileCompleted") ?: false,
                        uid = doc.getString("uid"),
                        isActive = doc.getBoolean("isActive") ?: true,
                        emailVerified = doc.getBoolean("emailVerified") ?: false,
                        phoneVerified = doc.getBoolean("phoneVerified") ?: false,
                        role = doc.getString("role") ?: "user",
                        education = education,
                        experiences = experiences
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

    // Enhanced saveUserProfile with document creation if not exists
    fun saveUserProfile(
        userId: String,
        fullName: String,
        username: String,
        email: String,
        phone: String,
        skills: List<String>,
        imageUri: Uri?,
        callback: (Boolean) -> Unit
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val docRef = firestore.collection("users").document(userId)
                val existingDoc = docRef.get().await()
                val isNewUser = !existingDoc.exists()

                // Create map of data to save/update
                val profileData = mutableMapOf<String, Any>().apply {
                    put("userId", userId)
                    put("uid", userId) // Add uid field
                    put("fullName", fullName)
                    put("username", username)
                    put("email", email)
                    put("phone", phone)
                    put("skills", skills)
                    put("profileCompleted", true)
                    put("isActive", true)
                    put("emailVerified", false)
                    put("phoneVerified", false)
                    put("createdAt", System.currentTimeMillis())
                    put("updatedAt", System.currentTimeMillis())
                    if (isNewUser) put("role", "user")
                }

                // Handle image upload if provided
                if (imageUri != null) {
                    try {
                        val ref = storage.reference
                            .child("profile_images/$userId/${UUID.randomUUID()}.jpg")
                        ref.putFile(imageUri).await()
                        val downloadUrl = ref.downloadUrl.await().toString()
                        profileData["profilePictureUrl"] = downloadUrl
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Failed to upload image", e)
                        _errorMessage.value = "Failed to upload image: ${e.message}"
                        _isLoading.value = false
                        callback(false)
                        return@launch
                    }
                } else {
                    // Preserve existing profilePictureUrl if no new image
                    _userData.value?.profilePictureUrl?.let {
                        if (it.isNotEmpty()) {
                            profileData["profilePictureUrl"] = it
                        }
                    }
                }

                // Use set with merge option to create document if it doesn't exist
                firestore.collection("users")
                    .document(userId)
                    .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                // Update local state
                _userData.value = UserProfileData(
                    userId = userId,
                    uid = userId,
                    fullName = fullName,
                    username = username,
                    email = email,
                    phone = phone,
                    skills = skills,
                    profilePictureUrl = profileData["profilePictureUrl"] as? String ?: "",
                    profileCompleted = true,
                    isActive = true,
                    emailVerified = false,
                    phoneVerified = false,
                    role = "user",
                    education = _userData.value?.education,
                    experiences = _userData.value?.experiences ?: emptyList()
                )

                _isLoading.value = false
                callback(true)
            } catch (e: Exception) {
                _errorMessage.value = "Error saving profile: ${e.message}"
                Log.e("ProfileViewModel", "Error saving profile", e)
                _isLoading.value = false
                callback(false)
            }
        }
    }

    // Save profile with image and education (for completing profile)
    fun saveCompleteProfile(
        userId: String,
        school: String,
        degree: String,
        fieldOfStudy: String,
        skills: List<String>,
        imageUri: Uri,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            // Check if document exists first
            val docRef = firestore.collection("users").document(userId)
            val doc = docRef.get().await()

            if (!doc.exists()) {
                _errorMessage.value = "User document not found. Please complete registration first."
                _isLoading.value = false
                callback(false)
                return@launch
            }

            // Upload image
            val ref = storage.reference
                .child("profile_images/$userId/${UUID.randomUUID()}.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            // Create education object
            val education = Education(
                id = UUID.randomUUID().toString(),
                school = school,
                degree = degree,
                fieldOfStudy = fieldOfStudy,
                isCurrentlyStudying = true
            )

            // Prepare updates
            val updates = mapOf<String, Any>(
                "education" to education.toMap(),
                "skills" to skills,
                "profilePictureUrl" to downloadUrl,
                "profileCompleted" to true,
                "updatedAt" to System.currentTimeMillis()
            )

            // Update document
            docRef.update(updates).await()

            // Update local state
            _userData.value = _userData.value?.copy(
                education = education,
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

    // Update specific user fields with existence check
    fun updateUserProfile(
        userId: String,
        updates: Map<String, Any>,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val docRef = firestore.collection("users").document(userId)
            val doc = docRef.get().await()

            if (!doc.exists()) {
                _errorMessage.value = "User document not found"
                _isLoading.value = false
                callback(false)
                return@launch
            }

            // Add timestamp to updates
            val updatesWithTimestamp = updates.toMutableMap().apply {
                put("updatedAt", System.currentTimeMillis())
            }

            docRef.update(updatesWithTimestamp).await()

            // Update local state with the new values
            val currentData = _userData.value ?: return@launch
            val updatedData = currentData.copy(
                fullName = updates["fullName"] as? String ?: currentData.fullName,
                username = updates["username"] as? String ?: currentData.username,
                phone = updates["phone"] as? String ?: currentData.phone,
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

    // Create initial user document (call this after successful registration)
    fun createUserDocument(
        userId: String,
        fullName: String,
        username: String,
        email: String,
        phone: String,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val userData = mapOf(
                "userId" to userId,
                "uid" to userId,
                "fullName" to fullName,
                "username" to username,
                "email" to email,
                "phone" to phone,
                "skills" to emptyList<String>(),
                "profilePictureUrl" to "",
                "profileCompleted" to false,
                "isActive" to true,
                "emailVerified" to false,
                "phoneVerified" to false,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis(),
                "role" to "user"
            )

            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            callback(true)
        } catch (e: Exception) {
            _errorMessage.value = "Error creating user document: ${e.message}"
            Log.e("ProfileViewModel", "Error creating user document", e)
            callback(false)
        } finally {
            _isLoading.value = false
        }
    }

    fun saveEducationData(
        school: String,
        degree: String,
        fieldOfStudy: String,
        startDate: String,
        endDate: String,
        grade: String,
        activities: String,
        description: String,
        mediaUris: List<Uri>
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val education = Education(
                id = UUID.randomUUID().toString(),
                school = school,
                degree = degree,
                fieldOfStudy = fieldOfStudy,
                startDate = startDate,
                endDate = endDate,
                grade = grade,
                activities = activities,
                description = description,
                isCurrentlyStudying = (endDate == "Saat ini")
            )

            val userId = _userData.value?.userId ?: return@launch
            firestore.collection("users")
                .document(userId)
                .update("education", education.toMap())
                .await()

            _userData.value = _userData.value?.copy(education = education)
        } catch (e: Exception) {
            _errorMessage.value = "Gagal menyimpan data pendidikan: ${e.message}"
            Log.e("ProfileViewModel", "saveEducationData error", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun saveExperienceData(
        experience: Map<String, Any>,
        onResult: (Boolean) -> Unit
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val userId = _userData.value?.userId ?: return@launch
            val docRef = firestore.collection("users").document(userId)

            val currentExperiences = _userData.value?.experiences?.toMutableList() ?: mutableListOf()
            currentExperiences.add(experience.toExperience())

            docRef.update("experiences", currentExperiences.map { it.toMap() }).await()

            _userData.value = _userData.value?.copy(experiences = currentExperiences)
            onResult(true)
        } catch (e: Exception) {
            _errorMessage.value = "Gagal menyimpan pengalaman: ${e.message}"
            Log.e("ProfileViewModel", "saveExperienceData error", e)
            onResult(false)
        } finally {
            _isLoading.value = false
        }
    }

    fun createPost(
        userId: String,
        content: String,
        imageUri: Uri?,
        onResult: (Boolean) -> Unit
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val postId = UUID.randomUUID().toString()
            val postRef = firestore.collection("posts").document(postId)

            val postData = mutableMapOf<String, Any>(
                "postId" to postId,
                "userId" to userId,
                "content" to content,
                "timestamp" to System.currentTimeMillis()
            )

            // Upload image if available
            if (imageUri != null) {
                val imageRef = storage.reference
                    .child("post_images/$userId/$postId.jpg")
                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()
                postData["imageUrl"] = downloadUrl
            }

            postRef.set(postData).await()

            onResult(true)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Failed to create post", e)
            _errorMessage.value = "Gagal membuat postingan: ${e.message}"
            onResult(false)
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}