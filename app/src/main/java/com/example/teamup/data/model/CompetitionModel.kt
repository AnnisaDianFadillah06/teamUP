package com.example.teamup.data.model

import androidx.annotation.DrawableRes

data class CompetitionModel(
    val id: String,
    val name: String,
    @DrawableRes val iconResId: Int,
    val teamCount: Int,
    val namaLomba: String,
    val cabangLomba: String,
    val tanggalPelaksanaan: String,
    val deskripsiLomba: String
)