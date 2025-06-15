package com.example.teamup.data.repositories

import com.example.teamup.data.model.ChatMessageModel
import com.example.teamup.data.sources.remote.FirebaseChatDataSource
import kotlinx.coroutines.flow.Flow

class ChatRepositoryImpl(
    private val firebaseChatDataSource: FirebaseChatDataSource
) : ChatRepository {
    override fun getMessages(teamId: String): Flow<List<ChatMessageModel>> {
        return firebaseChatDataSource.getMessages(teamId)
    }

    override suspend fun sendMessage(teamId: String, message: ChatMessageModel): Result<Unit> {
        return firebaseChatDataSource.sendMessage(teamId, message)
    }
}