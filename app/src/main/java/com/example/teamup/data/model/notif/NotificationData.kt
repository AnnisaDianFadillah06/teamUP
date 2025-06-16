// data/model/notification/NotificationData.kt
package com.example.teamup.data.model.notif

data class NotificationData(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "broadcast", // broadcast, system, announcement, etc.
    val status: String = "sent", // sent, pending, etc.
    val sentBy: String = "", // admin, system, etc.
    val createdAt: Long = System.currentTimeMillis(),
    val data: Map<String, Any> = emptyMap(), // additional data
    val isRead: Boolean = false, // untuk local tracking
    val actionUrl: String = "" // untuk redirect jika ada
)