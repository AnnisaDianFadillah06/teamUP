// data/model/UserProfileData.kt

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

    // Bio/About section - ini yang cocok ada di UserProfileData
    val bio: String = "",
    val location: String = "",
    val website: String = "",

    // These will be loaded separately from subcollections
    // Remove the default empty lists since they should be loaded separately
) {
    /**
     * Converts this UserProfileData into a Map ready for Firestore
     * Note: subcollection data (educations, experiences, etc.) are not included
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
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

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
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
            updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis()
        )
    }
}

/**
 * Extension function to convert Map to UserProfileData
 */
fun Map<String, Any?>.toUserProfileData(): UserProfileData = UserProfileData.fromMap(this)