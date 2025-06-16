package com.example.teamup.data.viewmodels.user

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.model.user.Education
import com.example.teamup.data.model.user.Skill
import com.example.teamup.data.model.user.Activity
import com.example.teamup.data.model.user.Experience
import com.example.teamup.data.repositories.user.EducationRepository
import com.example.teamup.data.repositories.user.SkillRepository
import com.example.teamup.data.repositories.user.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository()
    private val educationRepository = EducationRepository()
    private val skillRepository = SkillRepository()

    // Main user data
    private val _userData = MutableStateFlow<UserProfileData?>(null)
    val userData: StateFlow<UserProfileData?> = _userData.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Subcollection data StateFlows
    private val _userEducations = MutableStateFlow<List<Education>>(emptyList())
    val userEducations: StateFlow<List<Education>> = _userEducations.asStateFlow()

    private val _userSkills = MutableStateFlow<List<Skill>>(emptyList())
    val userSkills: StateFlow<List<Skill>> = _userSkills.asStateFlow()

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    private val _experiences = MutableStateFlow<List<Experience>>(emptyList())
    val experiences: StateFlow<List<Experience>> = _experiences.asStateFlow()

    private val _educations = MutableStateFlow<List<Education>>(emptyList())
    val educations: StateFlow<List<Education>> = _educations.asStateFlow()

    private val _skills = MutableStateFlow<List<String>>(emptyList())
    val skills: StateFlow<List<String>> = _skills.asStateFlow()

    // Utility to check network connectivity
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = ContextCompat.getSystemService(
            getApplication(),
            ConnectivityManager::class.java
        )
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager?.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    // Utility for retry with exponential backoff
    private suspend fun <T> retryWithBackoff(
        attempts: Int = 3,
        delayMillis: Long = 1000L,
        block: suspend () -> T
    ): Result<T> {
        repeat(attempts) { attempt ->
            try {
                return Result.success(block())
            } catch (e: Exception) {
                if (attempt == attempts - 1) return Result.failure(e)
                delay(delayMillis * (1 shl attempt)) // Exponential backoff
            }
        }
        return Result.failure(Exception("Max retry attempts reached"))
    }

    fun getUserData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (!isNetworkAvailable()) {
                    _errorMessage.value = "Tidak ada koneksi internet. Data akan disinkronkan saat online."
                }

                val user = repository.getUser(userId)
                _userData.value = user

            } catch (e: Exception) {
                _errorMessage.value = "Error fetching user data: ${e.message}"
                Log.e("ProfileViewModel", "Error fetching user data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveUserProfile(
        userId: String,
        fullName: String,
        username: String,
        email: String,
        phone: String,
        imageUri: Uri?,
        callback: (Boolean) -> Unit
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (!isNetworkAvailable()) {
                    _errorMessage.value = "Tidak ada koneksi internet. Data akan disinkronkan saat online."
                    // Allow offline persistence to handle the operation
                }

                Log.d("ProfileViewModel", "Mencoba menyimpan profil untuk userId: $userId")

                // Get existing user data
                val existingUser = repository.getUser(userId)
                val isNewUser = existingUser == null

                // Create updated user data
                val profileData = UserProfileData(
                    uid = userId,
                    userId = userId,
                    fullName = fullName,
                    username = username,
                    email = email,
                    phone = phone,
                    profilePictureUrl = existingUser?.profilePictureUrl ?: "",
                    emailVerified = existingUser?.emailVerified ?: false,
                    phoneVerified = existingUser?.phoneVerified ?: false,
                    isActive = true,
                    role = existingUser?.role ?: "user",
                    profileCompleted = true,
                    createdAt = existingUser?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val success = if (isNewUser) {
                    repository.createUser(profileData, imageUri)
                } else {
                    repository.updateUser(profileData, imageUri)
                }

                if (success) {
                    // Update local state
                    _userData.value = profileData.copy(
                        profilePictureUrl = if (imageUri != null) "" else profileData.profilePictureUrl // Will be updated after upload
                    )
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menyimpan profil"
                    callback(false)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menyimpan profil: ${e.message}"
                Log.e("ProfileViewModel", "Error saving profile", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Save complete profile with education data
    fun saveCompleteProfile(
        userId: String,
        fullName: String,
        username: String,
        email: String,
        phone: String,
        school: String,
        degree: String,
        fieldOfStudy: String,
        imageUri: Uri?,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (!isNetworkAvailable()) {
                    _errorMessage.value = "Tidak ada koneksi internet. Data akan disinkronkan saat online."
                }

                Log.d("ProfileViewModel", "Saving complete profile for userId: $userId")

                // Get existing user data
                val existingUser = repository.getUser(userId)
                val isNewUser = existingUser == null

                // Create updated user data
                val profileData = UserProfileData(
                    uid = userId,
                    userId = userId,
                    fullName = fullName,
                    username = username,
                    email = email,
                    phone = phone,
                    profilePictureUrl = existingUser?.profilePictureUrl ?: "",
                    emailVerified = existingUser?.emailVerified ?: false,
                    phoneVerified = existingUser?.phoneVerified ?: false,
                    isActive = true,
                    role = existingUser?.role ?: "user",
                    profileCompleted = true,
                    createdAt = existingUser?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                // First save/update the user profile
                val profileSuccess = if (isNewUser) {
                    repository.createUser(profileData, imageUri)
                } else {
                    repository.updateUser(profileData, imageUri)
                }

                if (profileSuccess) {
                    // If we have education data, save it as well
                    if (school.isNotBlank() && fieldOfStudy.isNotBlank()) {
                        val education = Education(
                            id = "",  // Will be generated by repository
                            school = school,
                            degree = degree,
                            fieldOfStudy = fieldOfStudy,
                            startDate = "",
                            endDate = "",
                            isCurrentlyStudying = true,
                            description = "",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )

                        // Add education to user's education subcollection
                        // You'll need to implement this in your repository
                        // For now, we'll just save the profile
                    }

                    // Update local state
                    _userData.value = profileData
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menyimpan profil lengkap"
                    callback(false)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error saving complete profile: ${e.message}"
                Log.e("ProfileViewModel", "Error saving complete profile", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user's education data - using existing userEducations StateFlow
     */
    fun loadUserEducations(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                educationRepository.getUserEducations(userId).collect { educations ->
                    _userEducations.value = educations
                    // Also update the educations StateFlow for compatibility
                    _educations.value = educations
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading educations", e)
                _errorMessage.value = "Gagal memuat data pendidikan: ${e.message}"
                _userEducations.value = emptyList()
                _educations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user's skills data - using existing userSkills StateFlow
     */
    fun loadUserSkills(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                skillRepository.getUserSkills(userId).collect { skillsList ->
                    _userSkills.value = skillsList
                    // Also update the skills StateFlow with skill names
                    _skills.value = skillsList.map { it.name }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading skills", e)
                _errorMessage.value = "Gagal memuat data skills: ${e.message}"
                _userSkills.value = emptyList()
                _skills.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user's activities data
     */
    fun loadUserActivities(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Call your repository to get activities
                val activitiesList = repository.getUserActivities(userId)
                _activities.value = activitiesList
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading activities: ${e.message}")
                _activities.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user's experiences data
     */
    fun loadUserExperiences(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Call your repository to get experiences
                val experiencesList = repository.getUserExperiences(userId)
                _experiences.value = experiencesList
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading experiences: ${e.message}")
                _experiences.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load all user profile data including subcollections
     */
    fun loadCompleteUserProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load main user data
                getUserData(userId)

                // Load all subcollection data in parallel
                launch { loadUserEducations(userId) }
                launch { loadUserSkills(userId) }
                launch { loadUserActivities(userId) }
                launch { loadUserExperiences(userId) }

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading complete profile", e)
                _errorMessage.value = "Gagal memuat profil lengkap: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add or update education from profile settings
     */
    fun saveEducationFromProfile(
        userId: String,
        school: String,
        degree: String,
        fieldOfStudy: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Check if user already has an education entry
                val existingEducations = _userEducations.value
                val currentEducation = existingEducations.firstOrNull { it.isCurrentlyStudying }

                if (currentEducation != null) {
                    // Update existing education
                    val updatedEducation = currentEducation.copy(
                        school = school,
                        degree = degree,
                        fieldOfStudy = fieldOfStudy,
                        updatedAt = System.currentTimeMillis()
                    )

                    val success = educationRepository.updateEducation(userId, updatedEducation, emptyList())
                    callback(success)
                } else {
                    // Create new education
                    val newEducation = Education(
                        userId = userId,
                        school = school,
                        degree = degree,
                        fieldOfStudy = fieldOfStudy,
                        startDate = "",
                        endDate = "",
                        isCurrentlyStudying = true,
                        description = "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    val success = educationRepository.addEducation(userId, newEducation, emptyList())
                    callback(success)
                }

                // Refresh education data
                if (callback == { true }) {
                    loadUserEducations(userId)
                }

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving education from profile", e)
                _errorMessage.value = "Gagal menyimpan data pendidikan: ${e.message}"
                callback(false)
            }
        }
    }

    /**
     * Add skills from profile settings (comma-separated string)
     */
    fun saveSkillsFromProfile(
        userId: String,
        skillsString: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val skillNames = skillsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                var successCount = 0

                // Get existing skills to avoid duplicates
                val existingSkills = _userSkills.value.map { it.name.lowercase() }

                for (skillName in skillNames) {
                    if (skillName.lowercase() !in existingSkills) {
                        val newSkill = Skill(
                            userId = userId,
                            name = skillName,
                            level = "Beginner", // Default level
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )

                        val success = skillRepository.addSkill(userId, newSkill)
                        if (success) successCount++
                    }
                }

                // Refresh skills data
                loadUserSkills(userId)
                callback(successCount > 0 || skillNames.isEmpty())

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving skills from profile", e)
                _errorMessage.value = "Gagal menyimpan data skills: ${e.message}"
                callback(false)
            }
        }
    }

    /**
     * Get current education summary for profile display
     */
    fun getCurrentEducationSummary(): String {
        val currentEducation = _userEducations.value.firstOrNull { it.isCurrentlyStudying }
        return when {
            currentEducation != null -> {
                "${currentEducation.fieldOfStudy} at ${currentEducation.school}"
            }
            _userEducations.value.isNotEmpty() -> {
                val latestEducation = _userEducations.value.maxByOrNull { it.createdAt }
                "${latestEducation?.fieldOfStudy} at ${latestEducation?.school}"
            }
            else -> ""
        }
    }

    /**
     * Get skills summary for profile display
     */
    fun getSkillsSummary(): String {
        return _userSkills.value.take(5).joinToString(", ") { it.name }
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
            val userData = UserProfileData(
                uid = userId,
                userId = userId,
                fullName = fullName,
                username = username,
                email = email,
                phone = phone,
                profilePictureUrl = "",
                emailVerified = false,
                phoneVerified = false,
                isActive = true,
                role = "user",
                profileCompleted = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val success = repository.createUser(userData, null)
            if (success) {
                _userData.value = userData
                callback(true)
            } else {
                _errorMessage.value = "Gagal membuat dokumen pengguna"
                callback(false)
            }

        } catch (e: Exception) {
            _errorMessage.value = "Error creating user document: ${e.message}"
            Log.e("ProfileViewModel", "Error creating user document", e)
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
            val currentUser = repository.getUser(userId)
            if (currentUser == null) {
                _errorMessage.value = "User document not found"
                callback(false)
                return@launch
            }

            // Create updated user data
            val updatedUser = currentUser.copy(
                fullName = updates["fullName"] as? String ?: currentUser.fullName,
                username = updates["username"] as? String ?: currentUser.username,
                phone = updates["phone"] as? String ?: currentUser.phone,
                profilePictureUrl = updates["profilePictureUrl"] as? String ?: currentUser.profilePictureUrl,
                emailVerified = updates["emailVerified"] as? Boolean ?: currentUser.emailVerified,
                phoneVerified = updates["phoneVerified"] as? Boolean ?: currentUser.phoneVerified,
                profileCompleted = updates["profileCompleted"] as? Boolean ?: currentUser.profileCompleted,
                updatedAt = System.currentTimeMillis()
            )

            val success = repository.updateUser(updatedUser, null)
            if (success) {
                _userData.value = updatedUser
                callback(true)
            } else {
                _errorMessage.value = "Gagal mengupdate profil"
                callback(false)
            }

        } catch (e: Exception) {
            _errorMessage.value = "Error updating profile: ${e.message}"
            Log.e("ProfileViewModel", "Error updating profile", e)
            callback(false)
        } finally {
            _isLoading.value = false
        }
    }

    fun updateProfilePicture(
        userId: String,
        imageUri: Uri,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val currentUser = _userData.value
            if (currentUser == null) {
                _errorMessage.value = "User data not found"
                callback(false)
                return@launch
            }

            val success = repository.updateUser(currentUser, imageUri)
            if (success) {
                // The repository will handle updating the profile picture URL
                getUserData(userId) // Refresh data to get updated profile picture URL
                callback(true)
            } else {
                _errorMessage.value = "Gagal mengupdate foto profil"
                callback(false)
            }

        } catch (e: Exception) {
            _errorMessage.value = "Error updating profile picture: ${e.message}"
            Log.e("ProfileViewModel", "Error updating profile picture", e)
            callback(false)
        } finally {
            _isLoading.value = false
        }
    }

    fun deleteUser(
        userId: String,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val success = repository.deleteUser(userId)
            if (success) {
                _userData.value = null
                callback(true)
            } else {
                _errorMessage.value = "Gagal menghapus akun"
                callback(false)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error deleting user: ${e.message}"
            Log.e("ProfileViewModel", "Error deleting user", e)
            callback(false)
        } finally {
            _isLoading.value = false
        }
    }

    fun verifyEmail(
        userId: String,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        try {
            val updates = mapOf("emailVerified" to true)
            updateUserProfile(userId, updates) { success ->
                callback(success)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error verifying email: ${e.message}"
            Log.e("ProfileViewModel", "Error verifying email", e)
            callback(false)
        }
    }

    fun verifyPhone(
        userId: String,
        callback: (Boolean) -> Unit
    ) = viewModelScope.launch {
        try {
            val updates = mapOf("phoneVerified" to true)
            updateUserProfile(userId, updates) { success ->
                callback(success)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error verifying phone: ${e.message}"
            Log.e("ProfileViewModel", "Error verifying phone", e)
            callback(false)
        }
    }

    fun searchUsers(query: String, callback: (List<UserProfileData>) -> Unit) {
        viewModelScope.launch {
            try {
                repository.searchUsers(query).collect { users ->
                    callback(users)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error searching users: ${e.message}"
                Log.e("ProfileViewModel", "Error searching users", e)
                callback(emptyList())
            }
        }
    }

    fun clearUserData() {
        _userData.value = null
        _userEducations.value = emptyList()
        _userSkills.value = emptyList()
        _activities.value = emptyList()
        _experiences.value = emptyList()
        _educations.value = emptyList()
        _skills.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}