package com.example.teamup.data.repositories

import com.example.teamup.data.model.InvitationModel
import com.example.teamup.data.model.InvitationStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InvitationRepositoryImpl(
    private val firestore: FirebaseFirestore
) : InvitationRepository {

    private val invitationsCollection = firestore.collection("invitations")

    override suspend fun createInvitation(invitation: InvitationModel): Result<String> {
        return try {
            val docRef = invitationsCollection.document()
            val invitationWithId = invitation.copy(
                id = docRef.id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            docRef.set(invitationWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createBulkInvitations(
        invitations: List<InvitationModel>
    ): Result<List<String>> {
        return try {
            val batch = firestore.batch()
            val inviteIds = mutableListOf<String>()

            invitations.forEach { invitation ->
                val docRef = invitationsCollection.document()
                val invitationWithId = invitation.copy(
                    id = docRef.id,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
                batch.set(docRef, invitationWithId)
                inviteIds.add(docRef.id)
            }

            batch.commit().await()
            Result.success(inviteIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateInvitationStatus(
        inviteId: String,
        status: InvitationStatus
    ): Result<Unit> {
        return try {
            invitationsCollection.document(inviteId)
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

    override fun getTeamInvitations(teamId: String): Flow<List<InvitationModel>> {
        return callbackFlow {
            val listener = invitationsCollection
                .whereEqualTo("teamId", teamId)
                .whereEqualTo("status", InvitationStatus.WAITING.name)
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
    }

    override fun getUserInvitations(userId: String): Flow<List<InvitationModel>> {
        return callbackFlow {
            val listener = invitationsCollection
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("status", InvitationStatus.WAITING.name)
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
    }

    override suspend fun getInvitationById(inviteId: String): Result<InvitationModel?> {
        return try {
            val doc = invitationsCollection.document(inviteId).get().await()
            Result.success(doc.toObject(InvitationModel::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}