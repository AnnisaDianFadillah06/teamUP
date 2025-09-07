// data/model/user/Experience.kt
package com.example.teamup.data.model.user

import com.google.firebase.firestore.DocumentId

data class Experience(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val position: String = "",
    val jobType: String = "",
    val company: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrentRole: Boolean = false,
    val location: String = "",
    val locationType: String = "",
    val description: String = "",
    val skills: List<String> = emptyList(),
    val mediaUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Alias untuk kompatibilitas dengan kode UI yang menggunakan isCurrent
    val isCurrent: Boolean get() = isCurrentRole

    /**
     * Converts this Experience into a Map ready for Firestore
     */
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "position" to position,
        "jobType" to jobType,
        "company" to company,
        "startDate" to startDate,
        "endDate" to endDate,
        "isCurrentRole" to isCurrentRole,
        "location" to location,
        "locationType" to locationType,
        "description" to description,
        "skills" to skills,
        "mediaUrls" to mediaUrls,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}

/**
 * Extension function to convert Map to Experience
 */
fun Map<String, Any>.toExperience(): Experience = Experience(
    id = this["id"] as? String ?: "",
    userId = this["userId"] as? String ?: "",
    position = this["position"] as? String ?: "",
    jobType = this["jobType"] as? String ?: "",
    company = this["company"] as? String ?: "",
    startDate = this["startDate"] as? String ?: "",
    endDate = this["endDate"] as? String ?: "",
    isCurrentRole = this["isCurrentRole"] as? Boolean ?: false,
    location = this["location"] as? String ?: "",
    locationType = this["locationType"] as? String ?: "",
    description = this["description"] as? String ?: "",
    skills = (this["skills"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    mediaUrls = (this["mediaUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    createdAt = this["createdAt"] as? Long ?: System.currentTimeMillis(),
    updatedAt = this["updatedAt"] as? Long ?: System.currentTimeMillis()
)