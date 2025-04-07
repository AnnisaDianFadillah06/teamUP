package com.example.teamup.data.repositories

import com.example.teamup.R
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.model.TeamModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CompetitionRepository {
    fun getCategories(): Flow<List<CompetitionModel>> = flow {
        // In a real app, this would come from an API or database
        val categories = listOf(
            CompetitionModel(
                id = "kmipn",
                name = "KMIPN",
                iconResId = R.drawable.google,
                teamCount = 78,
                namaLomba = "Kompetisi Mahasiswa Informatika Politeknik Nasional",
                cabangLomba = "Pemrograman",
                tanggalPelaksanaan = "12 Agustus 2025",
                deskripsiLomba = "Lomba bergengsi antar Politeknik se-Indonesia dalam bidang teknologi informasi."
            ),
            CompetitionModel(
                id = "pkm",
                name = "PKM",
                iconResId = R.drawable.dev_icon,
                teamCount = 708,
                namaLomba = "Program Kreativitas Mahasiswa",
                cabangLomba = "Gagasan Futuristik",
                tanggalPelaksanaan = "30 September 2025",
                deskripsiLomba = "Kompetisi nasional untuk menyalurkan ide kreatif mahasiswa dalam bentuk proposal PKM."
            ),
            CompetitionModel(
                id = "kri",
                name = "KRI",
                iconResId = R.drawable.earth_icon,
                teamCount = 5,
                namaLomba = "Kontes Robot Indonesia",
                cabangLomba = "Robot Sepak Bola",
                tanggalPelaksanaan = "5 November 2025",
                deskripsiLomba = "Kompetisi robot antar universitas yang diselenggarakan oleh DIKTI."
            ),
            CompetitionModel(
                id = "gemastik",
                name = "Gemastik",
                iconResId = R.drawable.facebook,
                teamCount = 200,
                namaLomba = "Pagelaran Mahasiswa Nasional Bidang TIK",
                cabangLomba = "UI/UX Design",
                tanggalPelaksanaan = "18 Oktober 2025",
                deskripsiLomba = "Kompetisi TIK nasional yang mendorong inovasi digital mahasiswa Indonesia."
            )
        )
        emit(categories)
    }

    fun getPopularTeams(): Flow<List<TeamModel>> = flow {
        // In a real app, this would come from an API or database
        val teams = listOf(
            TeamModel(
                id = "team1",
                name = "Al-Fath",
                category = "KMIPN - Cipta Inovasi",
                avatarResId = R.drawable.captain_icon,
                isJoined = true,
                isFull = false,
                description = "KMIPN - Cipta Inovasi",
                memberCount = 2,
                maxMembers = 5
            ),
            TeamModel(
                id = "team2",
                name = "Garuda",
                category = "KMIPN - Smart City",
                avatarResId = R.drawable.captain_icon,
                isJoined = false,
                isFull = false,
                description = "KMIPN - Smart City",
                memberCount = 3,
                maxMembers = 5
            ),
            TeamModel(
                id = "team3",
                name = "Brawijaya",
                category = "Gemastik - IoT",
                avatarResId = R.drawable.captain_icon,
                isJoined = false,
                isFull = true,
                description = "Gemastik - IoT",
                memberCount = 5,
                maxMembers = 5
            ),
        )
        emit(teams)
    }
}