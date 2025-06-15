package com.example.teamup.data.sources.remote

import com.example.teamup.data.model.ChatMessageModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseChatDataSource(
    private val firestore: FirebaseFirestore
) {
    fun getMessages(teamId: String): Flow<List<ChatMessageModel>> = callbackFlow {
        val messagesRef = firestore.collection("teams")
            .document(teamId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Changed to ASCENDING

        val listener = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList()).isSuccess
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ChatMessageModel::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(messages).isSuccess
        }

        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(teamId: String, message: ChatMessageModel): Result<Unit> {
        return try {
            val teamDoc = firestore.collection("teams").document(teamId).get().await()
            val members = teamDoc.get("members") as? List<String> ?: emptyList()

            if (!members.contains(message.senderId)) {
                return Result.failure(Exception("User is not a team member"))
            }

            firestore.collection("teams")
                .document(teamId)
                .collection("messages")
                .add(message)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}