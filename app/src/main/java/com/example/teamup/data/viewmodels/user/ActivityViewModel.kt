package com.example.teamup.data.viewmodels.user

import android.app.Application
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.user.Activity
import com.example.teamup.data.repositories.user.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ActivityRepository()

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _userActivities = MutableStateFlow<List<Activity>>(emptyList())
    val userActivities: StateFlow<List<Activity>> = _userActivities

    private val _publicActivities = MutableStateFlow<List<Activity>>(emptyList())
    val publicActivities: StateFlow<List<Activity>> = _publicActivities

    private val _currentActivity = MutableStateFlow<Activity?>(null)
    val currentActivity: StateFlow<Activity?> = _currentActivity

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // For feed pagination
    private val _hasMoreActivities = MutableStateFlow(true)
    val hasMoreActivities: StateFlow<Boolean> = _hasMoreActivities

    /**
     * Load public activities for main feed
     */
    fun loadPublicActivities(limit: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getPublicActivities(limit)
                result.fold(
                    onSuccess = { activityList ->
                        _publicActivities.value = activityList
                        _activities.value = activityList // For backward compatibility
                        _hasMoreActivities.value = activityList.size >= limit
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Gagal memuat aktivitas publik: ${exception.message}"
                        Log.e("ActivityViewModel", "Error loading public activities", exception)
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user's own activities
     */
    fun loadUserActivities(userId: String, limit: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getUserActivities(userId, limit)
                result.fold(
                    onSuccess = { activityList ->
                        _userActivities.value = activityList
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Gagal memuat aktivitas pengguna: ${exception.message}"
                        Log.e("ActivityViewModel", "Error loading user activities", exception)
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load specific activity by ID
     */
    fun loadActivity(userId: String, activityId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activity = repository.getActivity(userId, activityId) // Method ini tidak ada
                _currentActivity.value = activity
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data aktivitas: ${e.message}"
                Log.e(TAG, "Error loading activity", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Create new activity with single image
     */
    fun createActivity(
        userId: String,
        content: String,
        imageUri: Uri? = null,
        visibility: String = "public",
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activity = Activity(
                    userId = userId,
                    content = content,
                    visibility = visibility,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val mediaUris = imageUri?.let { listOf(it) }
                val result = repository.createActivity(userId, activity, mediaUris)

                result.fold(
                    onSuccess = { activityId ->
                        Log.d("ActivityViewModel", "Activity created successfully: $activityId")
                        // Refresh activities
                        loadUserActivities(userId)
                        loadPublicActivities()
                        callback(true)
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Gagal membuat aktivitas: ${exception.message}"
                        Log.e("ActivityViewModel", "Error creating activity", exception)
                        callback(false)
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create new activity with multiple media files
     */
    fun createActivityWithMedia(
        userId: String,
        content: String,
        mediaUris: List<Uri>? = null,
        visibility: String = "public",
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activity = Activity(
                    userId = userId,
                    content = content,
                    visibility = visibility,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val result = repository.createActivity(userId, activity, mediaUris)

                result.fold(
                    onSuccess = { activityId ->
                        Log.d("ActivityViewModel", "Activity with media created successfully: $activityId")
                        loadUserActivities(userId)
                        loadPublicActivities()
                        callback(true)
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Gagal membuat aktivitas: ${exception.message}"
                        Log.e("ActivityViewModel", "Error creating activity with media", exception)
                        callback(false)
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update existing activity
     */
    fun updateActivity(
        userId: String,
        activity: Activity,
        newMediaUris: List<Uri>? = null,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedActivity = activity.copy(
                    updatedAt = System.currentTimeMillis()
                )

                val result = repository.updateActivity(userId, updatedActivity, newMediaUris)

                result.fold(
                    onSuccess = {
                        Log.d("ActivityViewModel", "Activity updated successfully: ${activity.id}")
                        loadUserActivities(userId)
                        loadPublicActivities()
                        // Update current activity
                        _currentActivity.value = updatedActivity
                        callback(true)
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Gagal mengupdate aktivitas: ${exception.message}"
                        Log.e("ActivityViewModel", "Error updating activity", exception)
                        callback(false)
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update activity with new content and visibility
     */
    fun updateActivityContent(
        userId: String,
        activityId: String,
        content: String,
        visibility: String = "public",
        newMediaUris: List<Uri>? = null,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val currentActivity = _currentActivity.value
            if (currentActivity?.id == activityId) {
                val updatedActivity = currentActivity.copy(
                    content = content,
                    visibility = visibility
                )
                updateActivity(userId, updatedActivity, newMediaUris, callback)
            } else {
                // Load activity first, then update
                loadActivity(userId, activityId)
                val activity = _currentActivity.value
                if (activity != null) {
                    val updatedActivity = activity.copy(
                        content = content,
                        visibility = visibility
                    )
                    updateActivity(userId, updatedActivity, newMediaUris, callback)
                } else {
                    _errorMessage.value = "Aktivitas tidak ditemukan"
                    callback(false)
                }
            }
        }
    }

    /**
     * Delete activity
     */
    fun deleteActivity(
        userId: String,
        activityId: String,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.deleteActivity(userId, activityId)

                result.fold(
                    onSuccess = {
                        Log.d("ActivityViewModel", "Activity deleted successfully: $activityId")
                        loadUserActivities(userId)
                        loadPublicActivities()
                        // Clear current activity if it was the deleted one
                        if (_currentActivity.value?.id == activityId) {
                            _currentActivity.value = null
                        }
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Gagal menghapus aktivitas: ${exception.message}"
                        Log.e("ActivityViewModel", "Error deleting activity", exception)
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Search user's activities
     */
    fun searchUserActivities(userId: String, query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadUserActivities(userId)
                return@launch
            }

            try {
                val result = repository.searchActivities(userId, query)
                result.fold(
                    onSuccess = { activityList ->
                        _userActivities.value = activityList
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Gagal mencari aktivitas: ${exception.message}"
                        Log.e("ActivityViewModel", "Error searching activities", exception)
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mencari aktivitas: ${e.message}"
                Log.e("ActivityViewModel", "Error searching activities", e)
            }
        }
    }

    /**
     * Filter activities by visibility
     */
    fun filterActivitiesByVisibility(userId: String, visibility: String) {
        viewModelScope.launch {
            try {
                val result = repository.getActivitiesByVisibility(userId, visibility)
                result.fold(
                    onSuccess = { activityList ->
                        _userActivities.value = activityList
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Gagal memfilter aktivitas: ${exception.message}"
                        Log.e("ActivityViewModel", "Error filtering activities", exception)
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memfilter aktivitas: ${e.message}"
                Log.e("ActivityViewModel", "Error filtering activities", e)
            }
        }
    }

    /**
     * Update activity visibility
     */
    fun updateActivityVisibility(
        userId: String,
        activityId: String,
        visibility: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.updateActivityVisibility(userId, activityId, visibility)
                result.fold(
                    onSuccess = {
                        Log.d("ActivityViewModel", "Activity visibility updated: $activityId")
                        loadUserActivities(userId)
                        // Update current activity if it's the same one
                        _currentActivity.value?.let { current ->
                            if (current.id == activityId) {
                                _currentActivity.value = current.copy(visibility = visibility)
                            }
                        }
                        callback(true)
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Gagal mengupdate visibilitas: ${exception.message}"
                        Log.e("ActivityViewModel", "Error updating visibility", exception)
                        callback(false)
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengupdate visibilitas: ${e.message}"
                Log.e("ActivityViewModel", "Error updating visibility", e)
                callback(false)
            }
        }
    }

    /**
     * Refresh all activities
     */
    fun refreshActivities() {
        loadPublicActivities()
        _hasMoreActivities.value = true
    }

    /**
     * Refresh user activities
     */
    fun refreshUserActivities(userId: String) {
        loadUserActivities(userId)
    }

    /**
     * Clear current activity
     */
    fun clearCurrentActivity() {
        _currentActivity.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Upload activity image (for compatibility)
     */
    fun uploadActivityImage(userId: String, activityId: String, imageUri: Uri, callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val imageUrl = repository.uploadActivityImage(userId, activityId, imageUri)
                callback(imageUrl)
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengupload gambar: ${e.message}"
                Log.e("ActivityViewModel", "Error uploading image", e)
                callback(null)
            }
        }
    }

    /**
     * Upload multiple media files
     */
    fun uploadActivityMedia(userId: String, activityId: String, mediaUris: List<Uri>, callback: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val mediaUrls = repository.uploadActivityMedia(userId, activityId, mediaUris)
                callback(mediaUrls)
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengupload media: ${e.message}"
                Log.e("ActivityViewModel", "Error uploading media", e)
                callback(emptyList())
            }
        }
    }

    /**
     * Set current activity directly (untuk edit mode)
     */
    fun setCurrentActivity(activity: Activity) {
        _currentActivity.value = activity
    }
}