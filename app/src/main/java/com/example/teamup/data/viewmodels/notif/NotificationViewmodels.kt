// data/viewmodels/notification/NotificationViewModel.kt
package com.example.teamup.data.viewmodels.notif

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.notif.NotificationData
import com.example.teamup.data.repository.notif.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()

    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notifications: StateFlow<List<NotificationData>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("NotificationVM", "Loading notifications for user: $userId")

            try {
                val notificationList = repository.getUserNotifications(userId)
                android.util.Log.d("NotificationVM", "Received ${notificationList.size} notifications")

                _notifications.value = notificationList

                val unreadCount = repository.getUnreadCount(userId)
                android.util.Log.d("NotificationVM", "Unread count: $unreadCount")
                _unreadCount.value = unreadCount

            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Error loading notifications", e)
                _errorMessage.value = "Failed to load notifications: ${e.message}"
            } finally {
                _isLoading.value = false
                android.util.Log.d("NotificationVM", "Loading finished, isLoading: false")
            }
        }
    }

    fun markAsRead(notificationId: String, userId: String) {
        viewModelScope.launch {
            try {
                val success = repository.markAsRead(notificationId, userId)
                if (success) {
                    // Update unread count
                    _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to mark notification as read"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}