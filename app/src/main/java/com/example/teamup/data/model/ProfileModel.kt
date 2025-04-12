package com.example.teamup.data.model

data class ProfileModel(
    val id: String,
    val name: String,
    val email: String,
    val imageResId: Int,
    val university: String,
    val major: String,
    val skills: List<String>,
    var isSelected: Boolean = false
)