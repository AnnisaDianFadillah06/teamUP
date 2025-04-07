package com.example.teamup.data.repositories

import com.example.teamup.data.model.CompetitionModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CompetitionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val competitionsCollection = firestore.collection("competitions")

    suspend fun addCompetition(competition: CompetitionModel) {
        val newCompetition = hashMapOf(
            "namaLomba" to competition.namaLomba,
            "cabangLomba" to competition.cabangLomba,
            "tanggalPelaksanaan" to competition.tanggalPelaksanaan,
            "deskripsiLomba" to competition.deskripsiLomba,
            "jumlahTim" to competition.jumlahTim,
            "imageUrl" to competition.imageUrl,
            "fileUrl" to competition.fileUrl,  // Menambahkan fileUrl ke dokumen Firestore
            "createdAt" to competition.createdAt
        )
        competitionsCollection.add(newCompetition).await()
    }

    fun getAllCompetitions(): Flow<List<CompetitionModel>> = callbackFlow {
        val subscription = competitionsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val competitions = snapshot.documents.map { doc ->
                        CompetitionModel(
                            id = doc.id,
                            namaLomba = doc.getString("namaLomba") ?: "",
                            cabangLomba = doc.getString("cabangLomba") ?: "",
                            tanggalPelaksanaan = doc.getString("tanggalPelaksanaan") ?: "",
                            deskripsiLomba = doc.getString("deskripsiLomba") ?: "",
                            jumlahTim = doc.getLong("jumlahTim")?.toInt() ?: 0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            fileUrl = doc.getString("fileUrl") ?: "",  // Mengambil fileUrl dari dokumen
                            createdAt = doc.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now()
                        )
                    }
                    trySend(competitions)
                }
            }

        awaitClose { subscription.remove() }
    }

    companion object {
        @Volatile
        private var instance: CompetitionRepository? = null

        fun getInstance(): CompetitionRepository =
            instance ?: synchronized(this) {
                instance ?: CompetitionRepository().also { instance = it }
            }
    }

}

//
//
//class CompetitionRepository {
//    fun getCategories(): Flow<List<CompetitionModel>> = flow {
//        // In a real app, this would come from an API or database
//        val categories = listOf(
//            CompetitionModel(
//                id = "kmipn",
//                name = "KMIPN",
//                iconResId = R.drawable.google,
//                teamCount = 78,
//                namaLomba = "Kompetisi Mahasiswa Informatika Politeknik Nasional",
//                cabangLomba = "Pemrograman",
//                tanggalPelaksanaan = "12 Agustus 2025",
//                deskripsiLomba = "Lomba bergengsi antar Politeknik se-Indonesia dalam bidang teknologi informasi."
//            ),
//            CompetitionModel(
//                id = "pkm",
//                name = "PKM",
//                iconResId = R.drawable.dev_icon,
//                teamCount = 708,
//                namaLomba = "Program Kreativitas Mahasiswa",
//                cabangLomba = "Gagasan Futuristik",
//                tanggalPelaksanaan = "30 September 2025",
//                deskripsiLomba = "Kompetisi nasional untuk menyalurkan ide kreatif mahasiswa dalam bentuk proposal PKM."
//            ),
//            CompetitionModel(
//                id = "kri",
//                name = "KRI",
//                iconResId = R.drawable.earth_icon,
//                teamCount = 5,
//                namaLomba = "Kontes Robot Indonesia",
//                cabangLomba = "Robot Sepak Bola",
//                tanggalPelaksanaan = "5 November 2025",
//                deskripsiLomba = "Kompetisi robot antar universitas yang diselenggarakan oleh DIKTI."
//            ),
//            CompetitionModel(
//                id = "gemastik",
//                name = "Gemastik",
//                iconResId = R.drawable.facebook,
//                teamCount = 200,
//                namaLomba = "Pagelaran Mahasiswa Nasional Bidang TIK",
//                cabangLomba = "UI/UX Design",
//                tanggalPelaksanaan = "18 Oktober 2025",
//                deskripsiLomba = "Kompetisi TIK nasional yang mendorong inovasi digital mahasiswa Indonesia."
//            )
//        )
//        emit(categories)
//    }
//
//    fun getPopularTeams(): Flow<List<TeamModel>> = flow {
//        // In a real app, this would come from an API or database
//        val teams = listOf(
//            TeamModel(
//                id = "team1",
//                name = "Al-Fath",
//                category = "KMIPN - Cipta Inovasi",
//                avatarResId = R.drawable.captain_icon,
//                isJoined = true,
//                isFull = false,
//                description = "KMIPN - Cipta Inovasi",
//                memberCount = 2,
//                maxMembers = 5
//            ),
//            TeamModel(
//                id = "team2",
//                name = "Garuda",
//                category = "KMIPN - Smart City",
//                avatarResId = R.drawable.captain_icon,
//                isJoined = false,
//                isFull = false,
//                description = "KMIPN - Smart City",
//                memberCount = 3,
//                maxMembers = 5
//            ),
//            TeamModel(
//                id = "team3",
//                name = "Brawijaya",
//                category = "Gemastik - IoT",
//                avatarResId = R.drawable.captain_icon,
//                isJoined = false,
//                isFull = true,
//                description = "Gemastik - IoT",
//                memberCount = 5,
//                maxMembers = 5
//            ),
//        )
//        emit(teams)
//    }
//}