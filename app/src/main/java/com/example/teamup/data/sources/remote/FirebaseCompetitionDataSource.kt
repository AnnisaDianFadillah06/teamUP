package com.example.teamup.data.sources.remote

import com.example.teamup.data.model.CompetitionModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseCompetitionDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val competitionsCollection = "competitions"

    suspend fun addCompetition(competition: CompetitionModel): String? {
        return try {
            // Buat dokumen baru dengan ID otomatis
            val docRef = db.collection(competitionsCollection).document()
            // Copy model dengan ID yang baru dibuat
            val competitionWithId = competition.copy(id = docRef.id)
            // Set data ke Firestore
            docRef.set(competitionWithId).await()
            // Kembalikan ID jika berhasil
            docRef.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllCompetitions(): List<CompetitionModel> {
        return try {
            val snapshot = db.collection(competitionsCollection)
                .orderBy("createdAt")
                .get()
                .await()
            snapshot.toObjects(CompetitionModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}