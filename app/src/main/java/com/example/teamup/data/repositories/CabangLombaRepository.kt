package com.example.teamup.data.repositories

import com.example.teamup.data.model.CabangLombaModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class CabangLombaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val cabangLombaCollection = firestore.collection("cabang_lomba")

    suspend fun addCabangLomba(cabangLomba: CabangLombaModel): String {
        val docRef = cabangLombaCollection.add(cabangLomba).await()
        return docRef.id
    }

    suspend fun addMultipleCabangLomba(cabangLombaList: List<CabangLombaModel>) {
        val batch = firestore.batch()

        cabangLombaList.forEach { cabangLomba ->
            val docRef = cabangLombaCollection.document()
            batch.set(docRef, cabangLomba)
        }

        batch.commit().await()
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
                    val cabangLombaList = snapshot.documents.map { doc ->
                        CabangLombaModel(
                            id = doc.id,
                            competitionId = doc.getString("competitionId") ?: "",
                            namaCabang = doc.getString("namaCabang") ?: ""
                        )
                    }
                    trySend(cabangLombaList)
                }
            }

        awaitClose { subscription.remove() }
    }

    fun getAllCabangLomba(): Flow<List<CabangLombaModel>> = callbackFlow {
        val subscription = cabangLombaCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val cabangLombaList = snapshot.documents.map { doc ->
                        CabangLombaModel(
                            id = doc.id,
                            competitionId = doc.getString("competitionId") ?: "",
                            namaCabang = doc.getString("namaCabang") ?: ""
                        )
                    }
                    trySend(cabangLombaList)
                }
            }

        awaitClose { subscription.remove() }
    }

    // New function to update cabang lomba entries by merging with existing ones
    suspend fun updateCabangLombaForCompetition(competitionId: String, newCabangNames: List<String>) {
        // First get existing cabang lomba
        val existingCabangLomba = getCabangLombaByCompetitionId(competitionId).first()
        val existingNames = existingCabangLomba.map { it.namaCabang }

        // Find new cabang names that don't exist yet
        val cabangNamesToAdd = newCabangNames.filter { !existingNames.contains(it) }

        // Create models for new cabang names
        val newCabangModels = cabangNamesToAdd.map { cabangName ->
            CabangLombaModel(
                competitionId = competitionId,
                namaCabang = cabangName
            )
        }

        // Only add new cabang lomba if there are any
        if (newCabangModels.isNotEmpty()) {
            addMultipleCabangLomba(newCabangModels)
        }
    }

//    suspend fun deleteCabangLombaByCompetitionId(competitionId: String) {
//        // First get all documents with matching competitionId
//        val documents = cabangLombaCollection
//            .whereEqualTo("competitionId", competitionId)
//            .get()
//            .await()
//
//        // Then delete them in a batch
//        val batch = firestore.batch()
//        documents.forEach { document ->
//            batch.delete(document.reference)
//        }
//
//        batch.commit().await()
//    }

    companion object {
        @Volatile
        private var instance: CabangLombaRepository? = null

        fun getInstance(): CabangLombaRepository =
            instance ?: synchronized(this) {
                instance ?: CabangLombaRepository().also { instance = it }
            }
    }
}