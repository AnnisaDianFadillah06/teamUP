// data/model/Education.kt
package com.example.teamup.data.model.user

import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class Education(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val school: String = "",
    val degree: String = "",
    val fieldOfStudy: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val grade: String = "",
    val activities: String = "",
    val description: String = "",
    val mediaUrls: List<String> = emptyList(),
    val isCurrentlyStudying: Boolean = false,

    // TAMBAHAN BARU:
    val currentSemester: String = "",  // "Semester 5"
    val currentLevel: String = "",     // "Tingkat 3" atau "Kelas 12"

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Converts this Education into a Map ready for Firestore
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "school" to school,
        "degree" to degree,
        "fieldOfStudy" to fieldOfStudy,
        "startDate" to startDate,
        "endDate" to endDate,
        "grade" to grade,
        "activities" to activities,
        "description" to description,
        "mediaUrls" to mediaUrls,
        "isCurrentlyStudying" to isCurrentlyStudying,
        "currentSemester" to currentSemester,    // TAMBAH
        "currentLevel" to currentLevel,          // TAMBAH
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    /**
     * Creates a copy of this Education with updated timestamp
     */
    fun withUpdatedTimestamp(): Education = this.copy(
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Creates a copy of this Education with the specified userId
     */
    fun withUserId(userId: String): Education = this.copy(
        userId = userId,
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Validates if the education data is complete for saving
     */
    fun isValid(): Boolean {
        return school.isNotBlank() &&
                userId.isNotBlank() &&
                startDate.isNotBlank() &&
                (isCurrentlyStudying || endDate.isNotBlank())
    }

    companion object {
        /**
         * Creates Education from Map (used in repository)
         */
        fun fromMap(map: Map<String, Any>): Education = Education(
            id = map["id"] as? String ?: UUID.randomUUID().toString(),
            userId = map["userId"] as? String ?: "",
            school = map["school"] as? String ?: "",
            degree = map["degree"] as? String ?: "",
            fieldOfStudy = map["fieldOfStudy"] as? String ?: "",
            startDate = map["startDate"] as? String ?: "",
            endDate = map["endDate"] as? String ?: "",
            grade = map["grade"] as? String ?: "",
            activities = map["activities"] as? String ?: "",
            description = map["description"] as? String ?: "",
            mediaUrls = (map["mediaUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            isCurrentlyStudying = map["isCurrentlyStudying"] as? Boolean ?: false,
            currentSemester = map["currentSemester"] as? String ?: "",      // TAMBAH
            currentLevel = map["currentLevel"] as? String ?: "",            // TAMBAH
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
            updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis()
        )

        /**
         * Creates a new Education instance with the provided data
         * Automatically sets userId and timestamps
         */
        fun create(
            userId: String,
            school: String,
            degree: String = "",
            fieldOfStudy: String = "",
            startDate: String,
            endDate: String = "",
            grade: String = "",
            activities: String = "",
            description: String = "",
            mediaUrls: List<String> = emptyList(),
            isCurrentlyStudying: Boolean = false
        ): Education {
            val currentTime = System.currentTimeMillis()
            return Education(
                id = UUID.randomUUID().toString(),
                userId = userId,
                school = school,
                degree = degree,
                fieldOfStudy = fieldOfStudy,
                startDate = startDate,
                endDate = if (isCurrentlyStudying) "" else endDate,
                grade = grade,
                activities = activities,
                description = description,
                mediaUrls = mediaUrls,
                isCurrentlyStudying = isCurrentlyStudying,
                createdAt = currentTime,
                updatedAt = currentTime
            )
        }
    }
}

/**
 * Extension function to convert Map to Education
 */
fun Map<String, Any>.toEducation(): Education = Education.fromMap(this)

/**
 * Extension function to get the display text for education period
 */
fun Education.getDisplayPeriod(): String {
    return if (isCurrentlyStudying) {
        "$startDate - Present"
    } else {
        if (endDate.isNotBlank()) {
            "$startDate - $endDate"
        } else {
            startDate
        }
    }
}

/**
 * Extension function to get the full education title
 */
fun Education.getDisplayTitle(): String {
    return buildString {
        if (degree.isNotEmpty()) {
            append(degree)
            if (fieldOfStudy.isNotEmpty()) {
                append(" in $fieldOfStudy")
            }
        } else if (fieldOfStudy.isNotEmpty()) {
            append(fieldOfStudy)
        } else {
            append("Education")
        }
    }
}