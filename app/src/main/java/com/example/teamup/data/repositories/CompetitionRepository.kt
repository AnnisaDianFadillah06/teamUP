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

