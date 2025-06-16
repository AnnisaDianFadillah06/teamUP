package com.example.teamup.data.repositories

import com.example.teamup.data.model.ChatMessageModel
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(teamId: String): Flow<List<ChatMessageModel>>
    suspend fun sendMessage(teamId: String, message: ChatMessageModel): Result<Unit>
}