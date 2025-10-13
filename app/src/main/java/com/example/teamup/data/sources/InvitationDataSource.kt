package com.example.teamup.data.sources

import com.example.teamup.data.model.InvitationModel
import com.example.teamup.firebase.FirebaseManager.firestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Update status invitation (accept/reject)
suspend fun updateInvitationStatus(
    invitationId: String,
    status: String // "ACCEPTED" atau "REJECTED"
): Result<Unit> {
    return try {
        firestore.collection("invitations")
            .document(invitationId)
            .update(
                mapOf(
                    "status" to status,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Get invitations untuk user tertentu
fun getInvitationsForUser(userId: String): Flow<List<InvitationModel>> = callbackFlow {
    val listener = firestore.collection("invitations")
        .whereEqualTo("recipientId", userId)
        .whereEqualTo("status", "WAITING")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val invitations = snapshot?.documents?.mapNotNull {
                it.toObject(InvitationModel::class.java)
            } ?: emptyList()
            trySend(invitations)
        }
    awaitClose { listener.remove() }
}