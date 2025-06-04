package com.example.teamup.data.model

data class UserProfileData(
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",

    val profilePictureUrl: String = "",
    val profileCompleted: Boolean = false,

    val education: Education? = null,
    val skills: List<String> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val experiences: List<Experience> = emptyList(),

    val uid: String? = null,
    val createdAt: Any? = null,
    val isActive: Boolean = true,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val role: String = "user"
)

data class Activity(
    val id: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val mediaUrls: List<String> = emptyList(),
    val links: List<String> = emptyList()
)

data class Experience(
    val id: String = "",
    val position: String = "",
    val company: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false,
    val location: String = "",
    val description: String = "",
    val skills: List<String> = emptyList(),
    val mediaUrls: List<String> = emptyList()
)

data class Education(
    val id: String = "",
    val school: String = "",
    val degree: String = "",
    val fieldOfStudy: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrentlyStudying: Boolean = false,
    val grade: String = "",
    val activities: String = "",
    val description: String = "",
    val mediaUrls: List<String> = emptyList()
)

//Converters.kt nnati mau aku pindahin ke sana
// Untuk serialisasi data ke Firestore
fun Education.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "school" to school,
    "degree" to degree,
    "fieldOfStudy" to fieldOfStudy,
    "startDate" to startDate,
    "endDate" to endDate,
    "isCurrentlyStudying" to isCurrentlyStudying,
    "grade" to grade,
    "activities" to activities,
    "description" to description,
    "mediaUrls" to mediaUrls
)

fun Experience.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "position" to position,
    "company" to company,
    "startDate" to startDate,
    "endDate" to endDate,
    "isCurrent" to isCurrent,
    "location" to location,
    "description" to description,
    "skills" to skills,
    "mediaUrls" to mediaUrls
)

fun Map<String, Any>.toEducation(): Education = Education(
    id = this["id"] as? String ?: "",
    school = this["school"] as? String ?: "",
    degree = this["degree"] as? String ?: "",
    fieldOfStudy = this["fieldOfStudy"] as? String ?: "",
    startDate = this["startDate"] as? String ?: "",
    endDate = this["endDate"] as? String ?: "",
    isCurrentlyStudying = this["isCurrentlyStudying"] as? Boolean ?: false,
    grade = this["grade"] as? String ?: "",
    activities = this["activities"] as? String ?: "",
    description = this["description"] as? String ?: "",
    mediaUrls = this["mediaUrls"] as? List<String> ?: emptyList()
)

fun Map<String, Any>.toExperience(): Experience = Experience(
    id = this["id"] as? String ?: "",
    position = this["position"] as? String ?: "",
    company = this["company"] as? String ?: "",
    startDate = this["startDate"] as? String ?: "",
    endDate = this["endDate"] as? String ?: "",
    isCurrent = this["isCurrent"] as? Boolean ?: false,
    location = this["location"] as? String ?: "",
    description = this["description"] as? String ?: "",
    skills = this["skills"] as? List<String> ?: emptyList(),
    mediaUrls = this["mediaUrls"] as? List<String> ?: emptyList()
)

