package com.example.teamup.data.model


data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val university: String = "",
    val major: String = "",
    val skills: List<String> = emptyList(),
    val profilePictureUrl: String? = null
) {
    /**
     * Converts this UserProfile into a Map ready for Firestore
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "fullName" to fullName,
        "username" to username,
        "email" to email,
        "phone" to phone,
        "university" to university,
        "major" to major,
        "skills" to skills,
        "profilePictureUrl" to profilePictureUrl
    )
}

