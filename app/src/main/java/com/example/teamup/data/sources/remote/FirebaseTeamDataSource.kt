package com.example.teamup.data.sources.remote

import com.example.teamup.R
import com.example.teamup.data.model.TeamModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseTeamDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val teamsCollection = firestore.collection("teams")

    suspend fun addTeam(team: TeamModel): String? {
        return try {
            // Create a map of the team data that can be stored in Firestore
            val teamMap = hashMapOf(
                "name" to team.name,
                "description" to team.description,
                "category" to team.category,
                "avatarResId" to team.avatarResId,
                "imageUrl" to team.imageUrl,
                "createdAt" to team.createdAt
            )

            val documentRef = teamsCollection.add(teamMap).await()
            println("DEBUG: Team added with ID: ${documentRef.id}")
            documentRef.id
        } catch (e: Exception) {
            println("DEBUG: Error adding team: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllTeams(): List<TeamModel> {
        return try {
            println("DEBUG: Fetching teams from Firebase...")
            val snapshot = teamsCollection.get().await()
            println("DEBUG: Got ${snapshot.documents.size} team documents from Firebase")

            val teams = snapshot.documents.mapNotNull { doc ->
                try {
                    val team = TeamModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        avatarResId = doc.getLong("avatarResId")?.toInt() ?: R.drawable.captain_icon,
                        imageUrl = doc.getString("imageUrl"),
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                    )
                    println("DEBUG: Successfully mapped team: ${team.name}, ID: ${team.id}, Category: ${team.category}")
                    team
                } catch (e: Exception) {
                    println("DEBUG: Error mapping document ${doc.id}: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }

            println("DEBUG: Total teams mapped: ${teams.size}")
            teams
        } catch (e: Exception) {
            println("DEBUG: Error getting teams: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}