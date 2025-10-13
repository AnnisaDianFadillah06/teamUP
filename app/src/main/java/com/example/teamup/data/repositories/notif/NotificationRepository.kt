// data/repository/NotificationRepositoryImpl.kt
package com.example.teamup.data.repository.notif

import com.example.teamup.data.model.notif.NotificationData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationsCollection = firestore.collection("notifications")

    suspend fun getUserNotifications(userId: String): List<NotificationData> {
        return try {
            android.util.Log.d("NotificationRepo", "Starting to fetch notifications...")

            // Pertama coba ambil semua notifications tanpa filter untuk testing
            val result = notificationsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            android.util.Log.d("NotificationRepo", "Documents found: ${result.documents.size}")

            val notifications = result.documents.mapNotNull { document ->
                try {
                    android.util.Log.d("NotificationRepo", "Processing document: ${document.id}")
                    android.util.Log.d("NotificationRepo", "Document data: ${document.data}")

                    val data = document.data
                    if (data != null) {
                        val notification = NotificationData(
                            id = document.id,
                            title = data["title"] as? String ?: "",
                            body = data["body"] as? String ?: "",
                            type = data["type"] as? String ?: "broadcast",
                            status = data["status"] as? String ?: "sent",
                            sentBy = data["sentBy"] as? String ?: "",
                            createdAt = when (val timestamp = data["createdAt"]) {
                                is com.google.firebase.Timestamp -> timestamp.toDate().time
                                is Long -> timestamp
                                else -> System.currentTimeMillis()
                            },
                            data = data["data"] as? Map<String, Any> ?: emptyMap(),
                            isRead = false,
                            actionUrl = ""
                        )
                        android.util.Log.d("NotificationRepo", "Mapped notification: $notification")
                        notification
                    } else {
                        android.util.Log.w("NotificationRepo", "Document data is null for ${document.id}")
                        null
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NotificationRepo", "Error mapping document ${document.id}", e)
                    null
                }
            }

            android.util.Log.d("NotificationRepo", "Final notifications count: ${notifications.size}")
            notifications

        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "Error fetching notifications", e)
            emptyList()
        }
    }

    suspend fun markAsRead(notificationId: String, userId: String): Boolean {
        return try {
            firestore.collection("user_notifications")
                .document("${userId}_${notificationId}")
                .set(mapOf(
                    "userId" to userId,
                    "notificationId" to notificationId,
                    "isRead" to true,
                    "readAt" to System.currentTimeMillis()
                ))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUnreadCount(userId: String): Int {
        return try {
            val notifications = getUserNotifications(userId)
            val readNotifications = firestore.collection("user_notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", true)
                .get()
                .await()

            val readIds = readNotifications.documents.map {
                it.data?.get("notificationId") as? String ?: ""
            }.filter { it.isNotEmpty() }

            notifications.count { it.id !in readIds }
        } catch (e: Exception) {
            0
        }
    }
}