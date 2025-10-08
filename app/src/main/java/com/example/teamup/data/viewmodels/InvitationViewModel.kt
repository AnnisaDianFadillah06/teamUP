package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.*
import com.example.teamup.data.repositories.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InvitationViewModel(
    private val invitationRepository: InvitationRepository,
    private val teamRepository: TeamRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InvitationUiState>(InvitationUiState.Idle)
    val uiState: StateFlow<InvitationUiState> = _uiState.asStateFlow()

    private val _teamInvitations = MutableStateFlow<List<InvitationModel>>(emptyList())
    val teamInvitations: StateFlow<List<InvitationModel>> = _teamInvitations.asStateFlow()

    private val _userInvitations = MutableStateFlow<List<InvitationModel>>(emptyList())
    val userInvitations: StateFlow<List<InvitationModel>> = _userInvitations.asStateFlow()

    /**
     * Kirim invite ke multiple recipients sekaligus
     */
    fun sendInvitations(
        teamId: String,
        teamName: String,
        senderId: String,
        senderName: String,
        senderEmail: String,
        recipientsList: List<InvitationModel>
    ) {
        viewModelScope.launch {
            _uiState.value = InvitationUiState.Loading

            // Validasi tim tidak penuh
            val teamMembersResult = teamRepository.getTeamMembers(teamId)
            if (teamMembersResult.isSuccess) {
                val currentMembers = teamMembersResult.getOrNull() ?: emptyList()
                val teamDoc = teamRepository.getTeamById(teamId)
                val maxMembers = teamDoc?.maxMembers ?: 10

                if (currentMembers.size + recipientsList.size > maxMembers) {
                    _uiState.value = InvitationUiState.Error(
                        "Tim akan penuh jika semua invite diterima. Sisa slot: ${maxMembers - currentMembers.size}"
                    )
                    return@launch
                }
            }

            // Filter recipient yang sudah jadi member
            val validRecipients = recipientsList.filter { recipient ->
                val isMemberResult = teamRepository.isUserMemberOfTeam(teamId, recipient.id)
                !(isMemberResult.isSuccess && isMemberResult.getOrNull() == true)
            }

            if (validRecipients.isEmpty()) {
                _uiState.value = InvitationUiState.Error("Semua user sudah menjadi member")
                return@launch
            }

            // Prepare full invitation models
            val invitations = recipientsList.map { recipient ->
                recipient.copy(
                    teamId = teamId,
                    teamName = teamName,
                    senderId = senderId,
                    senderName = senderName,
                    senderEmail = senderEmail,
                    status = InvitationStatus.WAITING,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
            }

            // Send bulk invitations
            val result = invitationRepository.createBulkInvitations(invitations)

            if (result.isSuccess) {
                val inviteIds = result.getOrNull() ?: emptyList()

                // Send notifications to all recipients
                invitations.forEachIndexed { index, invitation ->
                    if (index < inviteIds.size) {
                        sendNotificationToRecipient(
                            recipientId = invitation.recipientId,
                            senderName = senderName,
                            teamName = teamName,
                            inviteId = inviteIds[index],
                            teamId = teamId
                        )
                    }
                }

                _uiState.value = InvitationUiState.Success(
                    "Undangan berhasil dikirim ke ${invitations.size} orang"
                )
            } else {
                _uiState.value = InvitationUiState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal mengirim undangan"
                )
            }
        }
    }

    /**
     * Handle accept/reject invitation dari invitee
     */
    fun handleInvitation(
        inviteId: String,
        accept: Boolean,
        teamId: String,
        recipientId: String,
        recipientName: String,
        teamName: String,
        senderId: String
    ) {
        viewModelScope.launch {
            _uiState.value = InvitationUiState.Loading

            val newStatus = if (accept) InvitationStatus.ACCEPTED else InvitationStatus.REJECTED

            // Update status invitation
            val updateResult = invitationRepository.updateInvitationStatus(inviteId, newStatus)

            if (updateResult.isFailure) {
                _uiState.value = InvitationUiState.Error("Gagal memproses undangan")
                return@launch
            }

            if (accept) {
                // Add member ke team (atomic transaction)
                val addMemberResult = teamRepository.addMemberToTeam(teamId, recipientId)

                if (addMemberResult.isFailure) {
                    _uiState.value = InvitationUiState.Error(
                        addMemberResult.exceptionOrNull()?.message ?: "Gagal bergabung ke tim"
                    )
                    return@launch
                }

                // Kirim notif sukses ke admin
                sendNotificationToSender(
                    senderId = senderId,
                    recipientName = recipientName,
                    accepted = true,
                    teamName = teamName,
                    teamId = teamId
                )

                _uiState.value = InvitationUiState.Success("Berhasil bergabung ke tim $teamName")
            } else {
                // Kirim notif reject ke admin
                sendNotificationToSender(
                    senderId = senderId,
                    recipientName = recipientName,
                    accepted = false,
                    teamName = teamName,
                    teamId = teamId
                )

                _uiState.value = InvitationUiState.Success("Undangan ditolak")
            }
        }
    }

    /**
     * Load invitations untuk tim tertentu (untuk admin lihat pending invites)
     */
    fun loadTeamInvitations(teamId: String) {
        viewModelScope.launch {
            invitationRepository.getTeamInvitations(teamId).collect { invitations ->
                _teamInvitations.value = invitations
            }
        }
    }

    /**
     * Load invitations untuk user tertentu (untuk invitee lihat undangan)
     */
    fun loadUserInvitations(userId: String) {
        viewModelScope.launch {
            invitationRepository.getUserInvitations(userId).collect { invitations ->
                _userInvitations.value = invitations
            }
        }
    }

    private fun sendNotificationToRecipient(
        recipientId: String,
        senderName: String,
        teamName: String,
        inviteId: String,
        teamId: String
    ) {
        viewModelScope.launch {
            val notification = NotificationModel(
                userId = recipientId,
                type = NotificationType.INVITE,
                title = "Undangan Tim",
                message = "$senderName mengundangmu bergabung ke tim $teamName",
                relatedId = inviteId,
                relatedType = "INVITE",
                senderName = senderName,
                createdAt = Timestamp.now(),
                isRead = false,
                actionData = mapOf(
                    "inviteId" to inviteId,
                    "teamId" to teamId
                )
            )
            notificationRepository.createNotification(notification)
        }
    }

    private fun sendNotificationToSender(
        senderId: String,
        recipientName: String,
        accepted: Boolean,
        teamName: String,
        teamId: String
    ) {
        viewModelScope.launch {
            val notification = NotificationModel(
                userId = senderId,
                type = if (accepted) NotificationType.INVITE_ACCEPTED else NotificationType.INVITE_REJECTED,
                title = if (accepted) "Undangan Diterima" else "Undangan Ditolak",
                message = if (accepted)
                    "$recipientName menerima undangan dan bergabung ke tim $teamName"
                else
                    "$recipientName menolak undangan ke tim $teamName",
                relatedId = teamId,
                relatedType = "TEAM",
                senderName = recipientName,
                createdAt = Timestamp.now(),
                isRead = false
            )
            notificationRepository.createNotification(notification)
        }
    }

    fun resetState() {
        _uiState.value = InvitationUiState.Idle
    }
}

sealed class InvitationUiState {
    object Idle : InvitationUiState()
    object Loading : InvitationUiState()
    data class Success(val message: String) : InvitationUiState()
    data class Error(val message: String) : InvitationUiState()
}

// Data class helper untuk recipient info
data class RecipientData(
    val id: String,
    val name: String,
    val email: String
)