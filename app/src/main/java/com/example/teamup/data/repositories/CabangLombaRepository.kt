package com.example.teamup.data.repositories

import com.example.teamup.data.model.CabangLombaModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CabangLombaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val cabangLombaCollection = firestore.collection("cabang_lomba")

    suspend fun addCabangLomba(cabangLomba: CabangLombaModel) {
        val newCabangLomba = hashMapOf(
            "competitionId" to cabangLomba.competitionId,
            "namaCabang" to cabangLomba.namaCabang,
            "createdAt" to cabangLomba.createdAt
        )
        cabangLombaCollection.add(newCabangLomba).await()
    }

    suspend fun addMultipleCabangLomba(cabangLombaList: List<CabangLombaModel>) {
        // Use a batch write for multiple operations
        val batch = firestore.batch()
        cabangLombaList.forEach { cabangLomba ->
            val newDoc = cabangLombaCollection.document()
            val data = hashMapOf(
                "competitionId" to cabangLomba.competitionId,
                "namaCabang" to cabangLomba.namaCabang,
                "createdAt" to cabangLomba.createdAt
            )
            batch.set(newDoc, data)
        }
        batch.commit().await()
    }

    // Added new method to get ALL cabang lomba entries
    fun getAllCabangLomba(): Flow<List<CabangLombaModel>> = callbackFlow {
        val subscription = cabangLombaCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val cabangList = snapshot.documents.map { doc ->
                        CabangLombaModel(
                            id = doc.id,
                            competitionId = doc.getString("competitionId") ?: "",
                            namaCabang = doc.getString("namaCabang") ?: "",
                            createdAt = doc.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now()
                        )
                    }
                    trySend(cabangList)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getCabangLombaByCompetitionId(competitionId: String): Flow<List<CabangLombaModel>> = callbackFlow {
        val subscription = cabangLombaCollection
            .whereEqualTo("competitionId", competitionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val cabangList = snapshot.documents.map { doc ->
                        CabangLombaModel(
                            id = doc.id,
                            competitionId = doc.getString("competitionId") ?: "",
                            namaCabang = doc.getString("namaCabang") ?: "",
                            createdAt = doc.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now()
                        )
                    }
                    trySend(cabangList)
                }
            }
        awaitClose { subscription.remove() }
    }

    companion object {
        @Volatile
        private var instance: CabangLombaRepository? = null
        fun getInstance(): CabangLombaRepository =
            instance ?: synchronized(this) {
                instance ?: CabangLombaRepository().also { instance = it }
            }
    }
}