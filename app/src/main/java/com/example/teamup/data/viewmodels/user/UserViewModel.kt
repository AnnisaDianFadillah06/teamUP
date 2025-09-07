package com.example.teamup.data.viewmodels.user

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.repositories.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository()

    private val _users = MutableStateFlow<List<UserProfileData>>(emptyList())
    val users: StateFlow<List<UserProfileData>> = _users

    private val _currentUser = MutableStateFlow<UserProfileData?>(null)
    val currentUser: StateFlow<UserProfileData?> = _currentUser

    private val _searchResults = MutableStateFlow<List<UserProfileData>>(emptyList())
    val searchResults: StateFlow<List<UserProfileData>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun getCurrentUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = repository.getUser(userId)
                _currentUser.value = user
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data pengguna saat ini: ${e.message}"
                Log.e("UserViewModel", "Error getting current user", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = repository.getUser(userId)
                _currentUser.value = user
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data pengguna: ${e.message}"
                Log.e("UserViewModel", "Error loading user", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createUser(
        userData: UserProfileData,
        profileImageUri: Uri?,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.createUser(userData, profileImageUri)
                if (success) {
                    _currentUser.value = userData
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal membuat pengguna baru"
                    callback(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal membuat pengguna: ${e.message}"
                Log.e("UserViewModel", "Error creating user", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(
        userData: UserProfileData,
        profileImageUri: Uri?,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateUser(userData, profileImageUri)
                if (success) {
                    _currentUser.value = userData
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal mengupdate pengguna"
                    callback(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengupdate pengguna: ${e.message}"
                Log.e("UserViewModel", "Error updating user", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserField(
        userId: String,
        fieldName: String,
        fieldValue: Any,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = _currentUser.value ?: repository.getUser(userId)
                if (currentUser == null) {
                    _errorMessage.value = "Pengguna tidak ditemukan"
                    callback(false)
                    return@launch
                }

                val updatedUser = when (fieldName) {
                    "fullName" -> currentUser.copy(fullName = fieldValue as String)
                    "username" -> currentUser.copy(username = fieldValue as String)
                    "email" -> currentUser.copy(email = fieldValue as String)
                    "phone" -> currentUser.copy(phone = fieldValue as String)
                    "profilePictureUrl" -> currentUser.copy(profilePictureUrl = fieldValue as String)
                    "emailVerified" -> currentUser.copy(emailVerified = fieldValue as Boolean)
                    "phoneVerified" -> currentUser.copy(phoneVerified = fieldValue as Boolean)
                    "isActive" -> currentUser.copy(isActive = fieldValue as Boolean)
                    "profileCompleted" -> currentUser.copy(profileCompleted = fieldValue as Boolean)
                    "role" -> currentUser.copy(role = fieldValue as String)
                    else -> {
                        _errorMessage.value = "Field tidak dikenal: $fieldName"
                        callback(false)
                        return@launch
                    }
                }.copy(updatedAt = System.currentTimeMillis())

                val success = repository.updateUser(updatedUser, null)
                if (success) {
                    _currentUser.value = updatedUser
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal mengupdate field $fieldName"
                    callback(false)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengupdate field: ${e.message}"
                Log.e("UserViewModel", "Error updating user field", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(
        userId: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.deleteUser(userId)
                if (success) {
                    _currentUser.value = null
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menghapus pengguna"
                    callback(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus pengguna: ${e.message}"
                Log.e("UserViewModel", "Error deleting user", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            repository.searchUsers(query)
                .catch { e ->
                    _errorMessage.value = "Gagal mencari pengguna: ${e.message}"
                    Log.e("UserViewModel", "Error searching users", e)
                }
                .collect { users ->
                    _searchResults.value = users
                    _isLoading.value = false
                }
        }
    }

    fun getActiveUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getActiveUsers()
                .catch { e ->
                    _errorMessage.value = "Gagal memuat pengguna aktif: ${e.message}"
                    Log.e("UserViewModel", "Error getting active users", e)
                }
                .collect { users ->
                    _users.value = users
                    _isLoading.value = false
                }
        }
    }

    fun getUsersByRole(role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getUsersByRole(role)
                .catch { e ->
                    _errorMessage.value = "Gagal memuat pengguna berdasarkan role: ${e.message}"
                    Log.e("UserViewModel", "Error getting users by role", e)
                }
                .collect { users ->
                    _users.value = users
                    _isLoading.value = false
                }
        }
    }

    fun getCompletedProfiles() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCompletedProfiles()
                .catch { e ->
                    _errorMessage.value = "Gagal memuat profil yang sudah lengkap: ${e.message}"
                    Log.e("UserViewModel", "Error getting completed profiles", e)
                }
                .collect { users ->
                    _users.value = users
                    _isLoading.value = false
                }
        }
    }

    fun updateProfilePicture(
        userId: String,
        imageUri: Uri,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = _currentUser.value ?: repository.getUser(userId)
                if (currentUser == null) {
                    _errorMessage.value = "Pengguna tidak ditemukan"
                    callback(false)
                    return@launch
                }

                val success = repository.updateUser(currentUser, imageUri)
                if (success) {
                    // Reload user to get updated profile picture URL
                    loadUser(userId)
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal mengupdate foto profil"
                    callback(false)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengupdate foto profil: ${e.message}"
                Log.e("UserViewModel", "Error updating profile picture", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deactivateUser(
        userId: String,
        callback: (Boolean) -> Unit
    ) {
        updateUserField(userId, "isActive", false, callback)
    }

    fun activateUser(
        userId: String,
        callback: (Boolean) -> Unit
    ) {
        updateUserField(userId, "isActive", true, callback)
    }

    fun verifyEmail(
        userId: String,
        callback: (Boolean) -> Unit
    ) {
        updateUserField(userId, "emailVerified", true, callback)
    }

    fun verifyPhone(
        userId: String,
        callback: (Boolean) -> Unit
    ) {
        updateUserField(userId, "phoneVerified", true, callback)
    }

    fun markProfileCompleted(
        userId: String,
        callback: (Boolean) -> Unit
    ) {
        updateUserField(userId, "profileCompleted", true, callback)
    }

    fun changeUserRole(
        userId: String,
        newRole: String,
        callback: (Boolean) -> Unit
    ) {
        updateUserField(userId, "role", newRole, callback)
    }

    fun refreshCurrentUser(userId: String) {
        getCurrentUser(userId)
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun clearCurrentUser() {
        _currentUser.value = null
    }

    fun clearUsers() {
        _users.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}