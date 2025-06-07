package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.NotificationModel
import com.example.teamup.data.repositories.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _unreadNotifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val unreadNotifications: StateFlow<List<NotificationModel>> = _unreadNotifications

    private val _readNotifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val readNotifications: StateFlow<List<NotificationModel>> = _readNotifications

    // Count of unread notifications
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    fun loadNotifications() {
        viewModelScope.launch {
            try {
                // Fetch notifications from repository
                val allNotifications = notificationRepository.getAllNotifications()

                _unreadNotifications.value = allNotifications.filter { !it.isRead }
                _readNotifications.value = allNotifications.filter { it.isRead }
                _unreadCount.value = _unreadNotifications.value.size

                // Set up real-time updates
                notificationRepository.observeNotifications().collect { notifications ->
                    _unreadNotifications.value = notifications.filter { !it.isRead }
                    _readNotifications.value = notifications.filter { it.isRead }
                    _unreadCount.value = _unreadNotifications.value.size
                }
            } catch (e: Exception) {
                // Handle errors
                e.printStackTrace()
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)

                // Update local state
                val notification = _unreadNotifications.value.find { it.id == notificationId } ?: return@launch

                // Create a read version of the notification
                val readNotification = notification.copy(isRead = true)

                // Update lists
                _unreadNotifications.value = _unreadNotifications.value.filter { it.id != notificationId }
                _readNotifications.value = _readNotifications.value + readNotification
                _unreadCount.value = _unreadNotifications.value.size
            } catch (e: Exception) {
                // Handle errors
                e.printStackTrace()
            }
        }
    }

    fun handleJoinRequest(notificationId: String, accepted: Boolean) {
        viewModelScope.launch {
            try {
                if (accepted) {
                    notificationRepository.acceptJoinRequest(notificationId)
                } else {
                    notificationRepository.rejectJoinRequest(notificationId)
                }

                // Mark as read regardless of action
                markAsRead(notificationId)
            } catch (e: Exception) {
                // Handle errors
                e.printStackTrace()
            }
        }
    }

    // Factory for creating NotificationViewModel with proper dependencies
    class Factory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
                return NotificationViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        @Volatile
        private var instance: NotificationViewModel? = null

        fun getInstance(repository: NotificationRepository): NotificationViewModel {
            return instance ?: synchronized(this) {
                instance ?: NotificationViewModel(repository).also { instance = it }
            }
        }
    }
}