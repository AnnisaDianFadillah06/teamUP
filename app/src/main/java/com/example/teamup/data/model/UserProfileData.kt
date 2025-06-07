
package com.example.teamup.data.model

data class UserProfileData(
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val university: String = "",
    val major: String = "",
    val skills: List<String> = emptyList(),
    val profilePictureUrl: String = "",
    val profileCompleted: Boolean = false
)
