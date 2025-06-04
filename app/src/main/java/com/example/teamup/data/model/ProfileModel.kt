package com.example.teamup.data.model

data class ProfileModel(
    val id: String,
    val name: String,
    val email: String,
    val imageResId: Int = 0, // Keep for backward compatibility
    val university: String,
    val major: String,
    val skills: List<String>,
    val profilePictureUrl: String = "", // Add URL for profile picture from Firestore
    val isSelected: Boolean = false
)