// data/viewmodels/user/ExperienceViewModel.kt
package com.example.teamup.data.viewmodels.user

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.user.Experience
import com.example.teamup.data.repositories.user.ExperienceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

class ExperienceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExperienceRepository()

    private val _experiences = MutableStateFlow<List<Experience>>(emptyList())
    val experiences: StateFlow<List<Experience>> = _experiences

    private val _currentExperience = MutableStateFlow<Experience?>(null)
    val currentExperience: StateFlow<Experience?> = _currentExperience

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    companion object {
        private const val TAG = "ExperienceViewModel"
    }

    fun loadExperiences(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getUserExperiences(userId)
                    .catch { e ->
                        _errorMessage.value = "Gagal memuat data pengalaman: ${e.message}"
                        Log.e(TAG, "Error loading experiences", e)
                    }
                    .collect { experienceList ->
                        _experiences.value = experienceList.sortedByDescending { it.createdAt }
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data pengalaman: ${e.message}"
                Log.e(TAG, "Error loading experiences", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadExperience(userId: String, experienceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val experience = repository.getExperience(userId, experienceId)
                _currentExperience.value = experience
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data pengalaman: ${e.message}"
                Log.e(TAG, "Error loading experience", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveExperience(
        userId: String,
        position: String,
        jobType: String,
        company: String,
        startDate: String,
        endDate: String,
        isCurrentRole: Boolean,
        location: String,
        locationType: String,
        description: String,
        skills: List<String>,
        mediaUris: List<Uri>,
        experienceId: String? = null, // null for new experience, provide for update
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Perbaikan: pastikan ID tidak kosong untuk update
                val finalExperienceId = experienceId?.takeIf { it.isNotEmpty() }

                val experience = Experience(
                    id = finalExperienceId ?: "", // kosong untuk baru, ada ID untuk update
                    userId = userId,
                    position = position,
                    jobType = jobType,
                    company = company,
                    startDate = startDate,
                    endDate = if (isCurrentRole) "" else endDate,
                    isCurrentRole = isCurrentRole,
                    location = location,
                    locationType = locationType,
                    description = description,
                    skills = skills,
                    mediaUrls = emptyList(), // Will be populated by repository
                    createdAt = if (finalExperienceId == null) {
                        System.currentTimeMillis()
                    } else {
                        _currentExperience.value?.createdAt ?: System.currentTimeMillis()
                    },
                    updatedAt = System.currentTimeMillis()
                )

                Log.d(TAG, "Saving experience - isUpdate: ${finalExperienceId != null}, ID: $finalExperienceId")

                val success = if (finalExperienceId == null) {
                    // Tambah baru
                    repository.addExperience(userId, experience, mediaUris)
                } else {
                    // Update existing - pastikan ID tidak kosong
                    val experienceWithId = experience.copy(id = finalExperienceId)
                    repository.updateExperience(userId, experienceWithId, mediaUris)
                }

                if (success) {
                    loadExperiences(userId) // Reload to get updated data
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menyimpan data pengalaman"
                    callback(false)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menyimpan data pengalaman: ${e.message}"
                Log.e(TAG, "Error saving experience", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExperience(
        userId: String,
        experienceId: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.deleteExperience(userId, experienceId)
                if (success) {
                    loadExperiences(userId) // Reload to get updated data
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menghapus data pengalaman"
                    callback(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus data pengalaman: ${e.message}"
                Log.e(TAG, "Error deleting experience", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCurrentExperiences(userId: String) {
        viewModelScope.launch {
            try {
                repository.getUserExperiences(userId)
                    .catch { e ->
                        Log.e(TAG, "Error getting current experiences", e)
                    }
                    .collect { experiences ->
                        val currentExperiences = experiences.filter { it.isCurrentRole }
                        // You can emit this to a separate StateFlow if needed
                        Log.d(TAG, "Found ${currentExperiences.size} current experiences")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting current experiences", e)
            }
        }
    }

    fun deleteExperienceMedia(mediaUrl: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.deleteExperienceMedia(mediaUrl)
                callback(success)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting experience media", e)
                callback(false)
            }
        }
    }

    fun clearCurrentExperience() {
        _currentExperience.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}