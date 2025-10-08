package com.example.teamup.data.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.*
import com.example.teamup.data.repositories.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JoinRequestViewModel(
    private val joinRequestRepository: JoinRequestRepository,
    private val teamRepository: TeamRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinRequestUiState>(JoinRequestUiState.Idle)
    val uiState: StateFlow<JoinRequestUiState> = _uiState.asStateFlow()

    private val _teamRequests = MutableStateFlow<List<JoinRequestModel>>(emptyList())
    val teamRequests: StateFlow<List<JoinRequestModel>> = _teamRequests.asStateFlow()

    fun sendJoinRequest(
        teamId: String,
        teamName: String,
        requesterId: String,
        requesterName: String,
        requesterEmail: String,
        captainId: String
    ) {
        viewModelScope.launch {
            _uiState.value = JoinRequestUiState.Loading
            Log.d("JoinRequestViewModel", "State changed to Loading")


            // Cek dulu apakah user sudah member
            val isMemberResult = teamRepository.isUserMemberOfTeam(teamId, requesterId)
            if (isMemberResult.isSuccess && isMemberResult.getOrNull() == true) {
                _uiState.value = JoinRequestUiState.Error("Kamu sudah menjadi member tim ini")
                return@launch
            }

            val request = JoinRequestModel(
                teamId = teamId,
                teamName = teamName,
                requesterId = requesterId,
                requesterName = requesterName,
                requesterEmail = requesterEmail,
                status = RequestStatus.PENDING,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val result = joinRequestRepository.createJoinRequest(request)

            if (result.isSuccess) {
                val requestId = result.getOrNull()!!

                // Kirim notifikasi ke admin
                sendNotificationToAdmin(
                    captainId = captainId,
                    requesterName = requesterName,
                    teamName = teamName,
                    requestId = requestId,
                    teamId = teamId
                )

                _uiState.value = JoinRequestUiState.Success("Permintaan bergabung terkirim")
            } else {
                _uiState.value = JoinRequestUiState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal mengirim permintaan"
                )
            }
        }
    }

    fun handleJoinRequest(
        requestId: String,
        approve: Boolean,
        teamId: String,
        requesterId: String,
        requesterName: String,
        teamName: String
    ) {
        viewModelScope.launch {
            _uiState.value = JoinRequestUiState.Loading

            val newStatus = if (approve) RequestStatus.APPROVED else RequestStatus.REJECTED

            // Update status request
            val updateResult = joinRequestRepository.updateRequestStatus(requestId, newStatus)

            if (updateResult.isFailure) {
                _uiState.value = JoinRequestUiState.Error("Gagal memproses permintaan")
                return@launch
            }

            if (approve) {
                // Add member ke team (atomic transaction)
                val addMemberResult = teamRepository.addMemberToTeam(teamId, requesterId)

                if (addMemberResult.isFailure) {
                    _uiState.value = JoinRequestUiState.Error(
                        addMemberResult.exceptionOrNull()?.message ?: "Gagal menambahkan member"
                    )
                    return@launch
                }

                // Kirim notif sukses ke requester
                sendNotificationToRequester(
                    requesterId = requesterId,
                    approved = true,
                    teamName = teamName,
                    teamId = teamId
                )

                _uiState.value = JoinRequestUiState.Success("$requesterName berhasil ditambahkan")
            } else {
                // Kirim notif ditolak ke requester
                sendNotificationToRequester(
                    requesterId = requesterId,
                    approved = false,
                    teamName = teamName,
                    teamId = teamId
                )

                _uiState.value = JoinRequestUiState.Success("Permintaan ditolak")
            }
        }
    }

    fun loadTeamJoinRequests(teamId: String) {
        viewModelScope.launch {
            joinRequestRepository.getTeamJoinRequests(teamId).collect { requests ->
                _teamRequests.value = requests
            }
        }
    }

    private fun sendNotificationToAdmin(
        captainId: String,
        requesterName: String,
        teamName: String,
        requestId: String,
        teamId: String
    ) {
        viewModelScope.launch {
            val notification = NotificationModel(
                userId = captainId,
                type = NotificationType.JOIN_REQUEST,
                title = "Permintaan Bergabung",
                message = "$requesterName ingin bergabung ke tim $teamName",
                relatedId = requestId,
                relatedType = "REQUEST",
                senderName = requesterName,
                createdAt = Timestamp.now(),
                isRead = false,
                actionData = mapOf(
                    "requestId" to requestId,
                    "teamId" to teamId
                )
            )
            notificationRepository.createNotification(notification)
        }
    }

    private fun sendNotificationToRequester(
        requesterId: String,
        approved: Boolean,
        teamName: String,
        teamId: String
    ) {
        viewModelScope.launch {
            val notification = NotificationModel(
                userId = requesterId,
                type = if (approved) NotificationType.JOIN_APPROVED else NotificationType.JOIN_REJECTED,
                title = if (approved) "Bergabung Berhasil" else "Permintaan Ditolak",
                message = if (approved)
                    "Selamat! Kamu sekarang menjadi member tim $teamName"
                else
                    "Maaf, permintaan bergabung ke tim $teamName ditolak",
                relatedId = teamId,
                relatedType = "TEAM",
                senderName = "System",
                createdAt = Timestamp.now(),
                isRead = false
            )
            notificationRepository.createNotification(notification)
        }
    }

    fun resetState() {
        _uiState.value = JoinRequestUiState.Idle
    }
}

sealed class JoinRequestUiState {
    object Idle : JoinRequestUiState()
    object Loading : JoinRequestUiState()
    data class Success(val message: String) : JoinRequestUiState()
    data class Error(val message: String) : JoinRequestUiState()
}