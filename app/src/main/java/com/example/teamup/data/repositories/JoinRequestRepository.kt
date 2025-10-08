package com.example.teamup.data.repositories

import com.example.teamup.data.model.JoinRequestModel
import com.example.teamup.data.model.RequestStatus
import kotlinx.coroutines.flow.Flow

interface JoinRequestRepository {
    suspend fun createJoinRequest(request: JoinRequestModel): Result<String>
    suspend fun updateRequestStatus(requestId: String, status: RequestStatus): Result<Unit>
    fun getTeamJoinRequests(teamId: String): Flow<List<JoinRequestModel>>
    fun getUserJoinRequests(userId: String): Flow<List<JoinRequestModel>>
    suspend fun getRequestById(requestId: String): Result<JoinRequestModel?>
}