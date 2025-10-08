package com.example.teamup.data.sources

import com.example.teamup.data.model.JoinRequestModel
import com.example.teamup.data.model.RequestStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JoinRequestDataSource(
    private val firestore: FirebaseFirestore
) {
    private val requestsCollection = firestore.collection("join_requests")

    // Kirim join request
    suspend fun sendJoinRequest(request: JoinRequestModel): Result<String> {
        return try {
            val docRef = requestsCollection.add(request.copy(
                id = "", // Auto-generate
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )).await()

            // Update id
            docRef.update("id", docRef.id).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get join requests untuk tim tertentu (untuk admin)
    fun getJoinRequestsForTeam(teamId: String): Flow<List<JoinRequestModel>> = callbackFlow {
        val listener = requestsCollection
            .whereEqualTo("teamId", teamId)
            .whereEqualTo("status", RequestStatus.PENDING.name)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(JoinRequestModel::class.java)?.copy(
                        status = RequestStatus.valueOf(doc.getString("status") ?: "PENDING")
                    )
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    // Update status join request (approve/reject)
    suspend fun updateJoinRequestStatus(
        requestId: String,
        status: RequestStatus
    ): Result<Unit> {
        return try {
            requestsCollection.document(requestId).update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cek apakah user sudah request join
    suspend fun hasExistingRequest(userId: String, teamId: String): Boolean {
        return try {
            val snapshot = requestsCollection
                .whereEqualTo("requesterId", userId)
                .whereEqualTo("teamId", teamId)
                .whereEqualTo("status", RequestStatus.PENDING.name)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}