package com.example.teamup.data.repositories

import com.example.teamup.data.model.CompetitionActivityStatus
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.model.CompetitionVisibilityStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CompetitionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val competitionsCollection = firestore.collection("competitions")

    suspend fun addCompetition(competition: CompetitionModel): String {
        val newCompetition = hashMapOf(
            "namaLomba" to competition.namaLomba,
            "tanggalPelaksanaan" to competition.tanggalPelaksanaan,
            "deskripsiLomba" to competition.deskripsiLomba,
//            "jumlahTim" to competition.jumlahTim,
            "imageUrl" to competition.imageUrl,
            "fileUrl" to competition.fileUrl,
            "createdAt" to competition.createdAt,
            "visibilityStatus" to competition.visibilityStatus,
            "activityStatus" to competition.activityStatus,
            "tanggalTutupPendaftaran" to competition.tanggalTutupPendaftaran,
            "autoCloseEnabled" to competition.autoCloseEnabled
        )
        val documentRef = competitionsCollection.add(newCompetition).await()
        return documentRef.id // Return the new document ID
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
                        val competition = CompetitionModel(
                            id = doc.id,
                            namaLomba = doc.getString("namaLomba") ?: "",
                            tanggalPelaksanaan = doc.getString("tanggalPelaksanaan") ?: "",
                            deskripsiLomba = doc.getString("deskripsiLomba") ?: "",
//                            jumlahTim = doc.getLong("jumlahTim")?.toInt() ?: 0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            fileUrl = doc.getString("fileUrl") ?: "",
                            createdAt = doc.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now(),
                            visibilityStatus = doc.getString("visibilityStatus") ?: CompetitionVisibilityStatus.PUBLISHED.value,
                            activityStatus = doc.getString("activityStatus") ?: CompetitionActivityStatus.ACTIVE.value,
                            tanggalTutupPendaftaran = doc.getTimestamp("tanggalTutupPendaftaran"),
                            autoCloseEnabled = doc.getBoolean("autoCloseEnabled") ?: false
                        )

                        // Check if we need to update the status based on auto-close
                        if (competition.shouldBeInactive() && competition.activityStatus == CompetitionActivityStatus.ACTIVE.value) {
                            // Update the status to inactive in Firestore
                            doc.reference.update("activityStatus", CompetitionActivityStatus.INACTIVE.value)

                            // Return competition with updated status
                            competition.copy(activityStatus = CompetitionActivityStatus.INACTIVE.value)
                        } else {
                            competition
                        }
                    }
                    trySend(competitions)
                }
            }

        awaitClose { subscription.remove() }
    }

    // Get a competition by ID
    suspend fun getCompetitionById(id: String): CompetitionModel? {
        val document = competitionsCollection.document(id).get().await()
        return if (document.exists()) {
            CompetitionModel(
                id = document.id,
                namaLomba = document.getString("namaLomba") ?: "",
                tanggalPelaksanaan = document.getString("tanggalPelaksanaan") ?: "",
                deskripsiLomba = document.getString("deskripsiLomba") ?: "",
//                jumlahTim = document.getLong("jumlahTim")?.toInt() ?: 0,
                imageUrl = document.getString("imageUrl") ?: "",
                fileUrl = document.getString("fileUrl") ?: "",
                createdAt = document.getTimestamp("createdAt") ?: Timestamp.now(),
                visibilityStatus = document.getString("visibilityStatus") ?: CompetitionVisibilityStatus.PUBLISHED.value,
                activityStatus = document.getString("activityStatus") ?: CompetitionActivityStatus.ACTIVE.value,
                tanggalTutupPendaftaran = document.getTimestamp("tanggalTutupPendaftaran"),
                autoCloseEnabled = document.getBoolean("autoCloseEnabled") ?: false
            )
        } else {
            null
        }
    }

    // Update competition method with flexible field updates
    suspend fun updateCompetition(competitionId: String, updates: Map<String, Any?>) {
        // Filter out null values before updating
        val filteredUpdates = updates.filterValues { it != null }
        competitionsCollection.document(competitionId).update(filteredUpdates).await()
    }

    // Add method to update competition status
    suspend fun updateCompetitionStatus(
        competitionId: String,
        visibilityStatus: String? = null,
        activityStatus: String? = null,
        tanggalTutupPendaftaran: Timestamp? = null,
        autoCloseEnabled: Boolean? = null
    ) {
        val updates = mutableMapOf<String, Any?>()

        visibilityStatus?.let { updates["visibilityStatus"] = it }
        activityStatus?.let { updates["activityStatus"] = it }
        tanggalTutupPendaftaran?.let { updates["tanggalTutupPendaftaran"] = it }
        autoCloseEnabled?.let { updates["autoCloseEnabled"] = it }

        if (updates.isNotEmpty()) {
            competitionsCollection.document(competitionId).update(updates).await()
        }
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