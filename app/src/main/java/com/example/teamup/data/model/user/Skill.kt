// ===== 1. data/model/user/Skill.kt =====
package com.example.teamup.data.model.user

import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class Skill(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val level: String = "", // Beginner, Intermediate, Advanced, Expert
    val fromExperienceId: String? = null, // Optional: jika skill berasal dari experience
    val fromEducationId: String? = null, // Optional: jika skill berasal dari education
    val isEndorsed: Boolean = false,
    val endorsementCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Converts this Skill into a Map ready for Firestore
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "level" to level,
        "fromExperienceId" to (fromExperienceId ?: ""),
        "fromEducationId" to (fromEducationId ?: ""),
        "isEndorsed" to isEndorsed,
        "endorsementCount" to endorsementCount,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        /**
         * Creates Skill from Map (used in repository)
         */
        fun fromMap(map: Map<String, Any>): Skill = Skill(
            id = map["id"] as? String ?: UUID.randomUUID().toString(),
            userId = map["userId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            level = map["level"] as? String ?: "",
            fromExperienceId = (map["fromExperienceId"] as? String).takeIf { !it.isNullOrEmpty() },
            fromEducationId = (map["fromEducationId"] as? String).takeIf { !it.isNullOrEmpty() },
            isEndorsed = map["isEndorsed"] as? Boolean ?: false,
            endorsementCount = map["endorsementCount"] as? Int ?: 0,
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
            updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis()
        )
    }
}

/**
 * Extension function to convert Map to Skill
 */
fun Map<String, Any>.toSkill(): Skill = Skill.fromMap(this)
