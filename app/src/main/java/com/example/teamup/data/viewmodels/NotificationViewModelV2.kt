package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.NotificationModelV2
import com.example.teamup.data.repositories.NotificationRepositoryV2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModelV2(
    private val notificationRepository: NotificationRepositoryV2
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationModelV2>>(emptyList())
    val notifications: StateFlow<List<NotificationModelV2>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadNotifications()
        observeNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val notifications = notificationRepository.getAllNotifications()
                _notifications.value = notifications
                updateUnreadCount()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationRepository.observeNotifications().collect { notifications ->
                _notifications.value = notifications
                updateUnreadCount()
            }
        }
    }

    private suspend fun updateUnreadCount() {
        try {
            val count = notificationRepository.getUnreadNotificationsCount()
            _unreadCount.value = count
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
                // Update local state
                _notifications.value = _notifications.value.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
                updateUnreadCount()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun acceptJoinRequest(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.acceptJoinRequest(notificationId)
                markAsRead(notificationId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun rejectJoinRequest(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.rejectJoinRequest(notificationId)
                markAsRead(notificationId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                notificationRepository.markAllAsRead()
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                _unreadCount.value = 0
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    class Factory(
        private val notificationRepository: NotificationRepositoryV2
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationViewModelV2::class.java)) {
                return NotificationViewModelV2(notificationRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}