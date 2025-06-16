package com.example.teamup.data.viewmodels.user

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.user.Education
import com.example.teamup.data.repositories.user.EducationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class EducationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EducationRepository()

    private val _educations = MutableStateFlow<List<Education>>(emptyList())
    val educations: StateFlow<List<Education>> = _educations

    private val _currentEducation = MutableStateFlow<Education?>(null)
    val currentEducation: StateFlow<Education?> = _currentEducation

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    companion object {
        private const val TAG = "EducationViewModel"
    }

    fun loadEducations(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.getUserEducations(userId)
                    .catch { e ->
                        _errorMessage.value = "Gagal memuat data pendidikan: ${e.message}"
                        Log.e(TAG, "Error loading educations", e)
                        emit(emptyList())
                    }
                    .collect { educationList ->
                        _educations.value = educationList
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data pendidikan: ${e.message}"
                Log.e(TAG, "Error loading educations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadEducation(userId: String, educationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val education = repository.getEducation(userId, educationId)
                _currentEducation.value = education

                if (education == null) {
                    _errorMessage.value = "Data pendidikan tidak ditemukan"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data pendidikan: ${e.message}"
                Log.e(TAG, "Error loading education", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveEducation(
        userId: String,
        school: String,
        degree: String,
        fieldOfStudy: String,
        startDate: String,
        endDate: String,
        grade: String,
        activities: String,
        description: String,
        mediaUris: List<Uri>,
        isCurrentlyStudying: Boolean,
        educationId: String? = null, // null for new education, provide for update
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val education = Education(
                    id = educationId ?: "",
                    userId = userId,
                    school = school,
                    degree = degree,
                    fieldOfStudy = fieldOfStudy,
                    startDate = startDate,
                    endDate = if (isCurrentlyStudying) "" else endDate,
                    grade = grade,
                    activities = activities,
                    description = description,
                    isCurrentlyStudying = isCurrentlyStudying,
                    mediaUrls = _currentEducation.value?.mediaUrls ?: emptyList(), // Keep existing media URLs for updates
                    createdAt = if (educationId == null) System.currentTimeMillis() else (_currentEducation.value?.createdAt ?: System.currentTimeMillis()),
                    updatedAt = System.currentTimeMillis()
                )

                val success = if (educationId == null) {
                    // Adding new education
                    repository.addEducation(userId, education, mediaUris)
                } else {
                    // Updating existing education
                    repository.updateEducation(userId, education, mediaUris)
                }

                if (success) {
                    // Reload educations to get updated data
                    loadEducations(userId)
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menyimpan data pendidikan"
                    callback(false)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menyimpan data pendidikan: ${e.message}"
                Log.e(TAG, "Error saving education", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEducation(
        userId: String,
        educationId: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val success = repository.deleteEducation(userId, educationId)

                if (success) {
                    // Reload educations to get updated data
                    loadEducations(userId)
                    // Clear current education if it was the one deleted
                    if (_currentEducation.value?.id == educationId) {
                        _currentEducation.value = null
                    }
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menghapus data pendidikan"
                    callback(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus data pendidikan: ${e.message}"
                Log.e(TAG, "Error deleting education", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEducationMedia(
        mediaUrl: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val success = repository.deleteEducationMedia(mediaUrl)

                if (success) {
                    // Update current education by removing the deleted media URL
                    _currentEducation.value?.let { education ->
                        val updatedMediaUrls = education.mediaUrls.filter { it != mediaUrl }
                        _currentEducation.value = education.copy(mediaUrls = updatedMediaUrls)
                    }
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menghapus media pendidikan"
                    callback(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus media pendidikan: ${e.message}"
                Log.e(TAG, "Error deleting education media", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCurrentEducation() {
        _currentEducation.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun setCurrentEducation(education: Education) {
        _currentEducation.value = education
    }
}