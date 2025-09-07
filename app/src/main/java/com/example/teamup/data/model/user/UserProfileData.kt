package com.example.teamup.data.model.user

import com.google.firebase.firestore.DocumentId

data class UserProfileData(
    @DocumentId
    val userId: String = "",
    val uid: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
    val phone: String = "",
    val phoneVerified: Boolean = false,
    val profilePictureUrl: String = "",
    val isActive: Boolean = true,
    val role: String = "user",
    val profileCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Bio/About section
    val bio: String = "",
    val location: String = "",
    val website: String = "",

    // Education & Skills - DIPINDAH DARI SUBCOLLECTION KE MAIN DOCUMENT
    val university: String = "",
    val major: String = "",
    val skills: List<String> = emptyList(),
    val specialization: String = "", // field baru untuk role/spesialisasi profesi

) {
    /**
     * Converts this UserProfileData into a Map ready for Firestore
     */
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "uid" to uid,
        "fullName" to fullName,
        "username" to username,
        "email" to email,
        "emailVerified" to emailVerified,
        "phone" to phone,
        "phoneVerified" to phoneVerified,
        "profilePictureUrl" to profilePictureUrl,
        "isActive" to isActive,
        "role" to role,
        "profileCompleted" to profileCompleted,
        "bio" to bio,
        "location" to location,
        "website" to website,
        "university" to university,
        "major" to major,
        "skills" to skills,
        "specialization" to specialization,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    /**
     * Convert to legacy UserProfile for backward compatibility
     */
    fun toUserProfile(): com.example.teamup.data.model.UserProfile {
        return com.example.teamup.data.model.UserProfile(
            uid = uid.ifEmpty { userId }, // fallback to userId if uid is empty
            fullName = fullName,
            username = username,
            email = email,
            phone = phone,
            university = university,
            major = major,
            skills = skills,
            profilePictureUrl = profilePictureUrl.ifEmpty { null }
        )
    }

    companion object {
        /**
         * Creates UserProfileData from Map (used in repository)
         */
        fun fromMap(map: Map<String, Any?>): UserProfileData = UserProfileData(
            userId = map["userId"] as? String ?: "",
            uid = map["uid"] as? String ?: "",
            fullName = map["fullName"] as? String ?: "",
            username = map["username"] as? String ?: "",
            email = map["email"] as? String ?: "",
            emailVerified = map["emailVerified"] as? Boolean ?: false,
            phone = map["phone"] as? String ?: "",
            phoneVerified = map["phoneVerified"] as? Boolean ?: false,
            profilePictureUrl = map["profilePictureUrl"] as? String ?: "",
            isActive = map["isActive"] as? Boolean ?: true,
            role = map["role"] as? String ?: "user",
            profileCompleted = map["profileCompleted"] as? Boolean ?: false,
            bio = map["bio"] as? String ?: "",
            location = map["location"] as? String ?: "",
            website = map["website"] as? String ?: "",
            university = map["university"] as? String ?: "",
            major = map["major"] as? String ?: "",
            skills = (map["skills"] as? List<String>) ?: emptyList(),
            specialization = map["specialization"] as? String ?: "",
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
            updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis()
        )

        /**
         * Create from legacy UserProfile
         */
        fun fromUserProfile(userProfile: com.example.teamup.data.model.UserProfile): UserProfileData {
            return UserProfileData(
                userId = userProfile.uid,
                uid = userProfile.uid,
                fullName = userProfile.fullName,
                username = userProfile.username,
                email = userProfile.email,
                phone = userProfile.phone,
                university = userProfile.university,
                major = userProfile.major,
                skills = userProfile.skills,
                profilePictureUrl = userProfile.profilePictureUrl ?: "",
                profileCompleted = userProfile.university.isNotEmpty() && userProfile.major.isNotEmpty()
            )
        }
    }
}

/**
 * Extension function to convert Map to UserProfileData
 */
fun Map<String, Any?>.toUserProfileData(): UserProfileData = UserProfileData.fromMap(this)