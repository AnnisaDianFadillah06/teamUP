package com.example.teamup.data.sources.remote

import com.example.teamup.data.model.TeamModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseTeamDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val teamsCollection = "teams"

    suspend fun addTeam(team: TeamModel): String? {
        return try {
            // Buat dokumen baru dengan ID otomatis
            val docRef = db.collection(teamsCollection).document()

            // Copy model dengan ID yang baru dibuat
            val teamWithId = team.copy(id = docRef.id)

            // Set data ke Firestore
            docRef.set(teamWithId).await()

            // Kembalikan ID jika berhasil
            docRef.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllTeams(): List<TeamModel> {
        return try {
            val snapshot = db.collection(teamsCollection)
                .orderBy("createdAt")
                .get()
                .await()

            snapshot.toObjects(TeamModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}