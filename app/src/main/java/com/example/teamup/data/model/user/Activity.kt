// data/model/user/Activity.kt
package com.example.teamup.data.model.user

import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class Activity(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val content: String = "",
    val imageUrl: String? = null, // Keep for backward compatibility
    val mediaUrls: List<String> = emptyList(), // Multiple media files support
    val visibility: String = "public", // public, connections, private
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Converts this Activity into a Map ready for Firestore
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "content" to content,
        "imageUrl" to (imageUrl ?: ""),
        "mediaUrls" to mediaUrls,
        "visibility" to visibility,
        "likesCount" to likesCount,
        "commentsCount" to commentsCount,
        "sharesCount" to sharesCount,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        /**
         * Creates Activity from Map (used in repository and data source)
         */
        fun fromMap(map: Map<String, Any>): Activity = Activity(
            id = map["id"] as? String ?: UUID.randomUUID().toString(),
            userId = map["userId"] as? String ?: "",
            content = map["content"] as? String ?: "",
            imageUrl = (map["imageUrl"] as? String).takeIf { !it.isNullOrEmpty() },
            mediaUrls = (map["mediaUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            visibility = map["visibility"] as? String ?: "public",
            likesCount = (map["likesCount"] as? Number)?.toInt() ?: 0,
            commentsCount = (map["commentsCount"] as? Number)?.toInt() ?: 0,
            sharesCount = (map["sharesCount"] as? Number)?.toInt() ?: 0,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    /**
     * Get all media URLs including imageUrl for backward compatibility
     */
    fun getAllMediaUrls(): List<String> {
        val allUrls = mutableListOf<String>()
        imageUrl?.let { allUrls.add(it) }
        allUrls.addAll(mediaUrls)
        return allUrls.distinct()
    }

    /**
     * Check if activity has any media
     */
    fun hasMedia(): Boolean = !imageUrl.isNullOrEmpty() || mediaUrls.isNotEmpty()

    /**
     * Get the first media URL (useful for thumbnails)
     */
    fun getFirstMediaUrl(): String? = imageUrl ?: mediaUrls.firstOrNull()
}

/**
 * Extension function to convert Map to Activity
 */
fun Map<String, Any>.toActivity(): Activity = Activity.fromMap(this)