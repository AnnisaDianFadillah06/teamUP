package com.example.teamup.data.repositories

import com.example.teamup.data.model.InvitationModel
import com.example.teamup.data.model.InvitationStatus
import kotlinx.coroutines.flow.Flow

interface InvitationRepository {
    suspend fun createInvitation(invitation: InvitationModel): Result<String>
    suspend fun createBulkInvitations(invitations: List<InvitationModel>): Result<List<String>>
    suspend fun updateInvitationStatus(inviteId: String, status: InvitationStatus): Result<Unit>
    fun getTeamInvitations(teamId: String): Flow<List<InvitationModel>>
    fun getUserInvitations(userId: String): Flow<List<InvitationModel>>
    suspend fun getInvitationById(inviteId: String): Result<InvitationModel?>
}