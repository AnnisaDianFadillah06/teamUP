package com.example.teamup.data.model

import androidx.annotation.DrawableRes
import com.example.teamup.R

data class CompetitionModel(
    val id: String,
    val name: String,
    @DrawableRes val iconResId: Int,
    val teamCount: Int
)