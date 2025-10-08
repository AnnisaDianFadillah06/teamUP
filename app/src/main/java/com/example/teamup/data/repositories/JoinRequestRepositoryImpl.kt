package com.example.teamup.data.repositories

import com.example.teamup.data.model.JoinRequestModel
import com.example.teamup.data.model.RequestStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JoinRequestRepositoryImpl(
    private val firestore: FirebaseFirestore
) : JoinRequestRepository {

    private val requestsCollection = firestore.collection("join_requests")

    override suspend fun createJoinRequest(request: JoinRequestModel): Result<String> {
        return try {
            val docRef = requestsCollection.document()
            val requestWithId = request.copy(id = docRef.id)
            docRef.set(requestWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRequestStatus(
        requestId: String,
        status: RequestStatus
    ): Result<Unit> {
        return try {
            requestsCollection.document(requestId)
                .update(
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

    override fun getTeamJoinRequests(teamId: String): Flow<List<JoinRequestModel>> {
        return callbackFlow {
            val listener = requestsCollection
                .whereEqualTo("teamId", teamId)
                .whereEqualTo("status", RequestStatus.PENDING.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val requests = snapshot?.documents?.mapNotNull {
                        it.toObject(JoinRequestModel::class.java)
                    } ?: emptyList()
                    trySend(requests)
                }
            awaitClose { listener.remove() }
        }
    }

    override fun getUserJoinRequests(userId: String): Flow<List<JoinRequestModel>> {
        return callbackFlow {
            val listener = requestsCollection
                .whereEqualTo("requesterId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val requests = snapshot?.documents?.mapNotNull {
                        it.toObject(JoinRequestModel::class.java)
                    } ?: emptyList()
                    trySend(requests)
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun getRequestById(requestId: String): Result<JoinRequestModel?> {
        return try {
            val doc = requestsCollection.document(requestId).get().await()
            Result.success(doc.toObject(JoinRequestModel::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}