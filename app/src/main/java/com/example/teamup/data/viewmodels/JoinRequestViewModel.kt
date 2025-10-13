package com.example.teamup.data.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.*
import com.example.teamup.data.repositories.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class JoinRequestViewModel(
    private val joinRequestRepository: JoinRequestRepository,
    private val teamRepository: TeamRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinRequestUiState>(JoinRequestUiState.Idle)
    val uiState: StateFlow<JoinRequestUiState> = _uiState.asStateFlow()

    private val _teamRequests = MutableStateFlow<List<JoinRequestModel>>(emptyList())
    val teamRequests: StateFlow<List<JoinRequestModel>> = _teamRequests.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    // ✅ FUNGSI BARU: Get full name dari Firestore
    private suspend fun getUserFullName(userId: String): String {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("fullName") ?: "Unknown User"
        } catch (e: Exception) {
            Log.e("JoinRequestViewModel", "Error getting user name: ${e.message}")
            "Unknown User"
        }
    }

    fun sendJoinRequest(
        teamId: String,
        teamName: String,
        requesterId: String,
        requesterName: String, // Ini masih dipakai untuk backup
        requesterEmail: String,
        captainId: String
    ) {
        viewModelScope.launch {
            _uiState.value = JoinRequestUiState.Loading
            Log.d("JoinRequestViewModel", "sendJoinRequest called")
            Log.d("JoinRequestViewModel", "TeamId: $teamId, CaptainId: $captainId")

            // ✅ VALIDASI: Cek captainId tidak kosong
            if (captainId.isEmpty()) {
                Log.e("JoinRequestViewModel", "ERROR: captainId is empty!")
                _uiState.value = JoinRequestUiState.Error("Error: Admin tim tidak ditemukan")
                return@launch
            }

            // ✅ Ambil nama lengkap dari Firestore
            val fullName = getUserFullName(requesterId)
            Log.d("JoinRequestViewModel", "User full name from Firestore: $fullName")

            // Cek dulu apakah user sudah member
            val isMemberResult = teamRepository.isUserMemberOfTeam(teamId, requesterId)
            if (isMemberResult.isSuccess && isMemberResult.getOrNull() == true) {
                _uiState.value = JoinRequestUiState.Error("Kamu sudah menjadi member tim ini")
                return@launch
            }

            // ✅ Cek apakah sudah pernah request (PENDING)
            val existingRequest = joinRequestRepository.getUserJoinRequests(requesterId)
                .first() // Ambil data pertama kali
                .find { it.teamId == teamId && it.status == RequestStatus.PENDING }

            if (existingRequest != null) {
                _uiState.value = JoinRequestUiState.Error("Permintaan bergabung sudah dikirim sebelumnya")
                return@launch
            }

            val request = JoinRequestModel(
                teamId = teamId,
                teamName = teamName,
                requesterId = requesterId,
                requesterName = fullName, // ✅ Gunakan nama dari Firestore
                requesterEmail = requesterEmail,
                status = RequestStatus.PENDING,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val result = joinRequestRepository.createJoinRequest(request)

            if (result.isSuccess) {
                val requestId = result.getOrNull()!!

                Log.d("JoinRequestViewModel", "Join request created: $requestId")
                Log.d("JoinRequestViewModel", "Sending notification to captainId: $captainId")

                // Kirim notifikasi ke admin dengan nama lengkap
                sendNotificationToAdmin(
                    captainId = captainId,
                    requesterName = fullName, // ✅ Gunakan nama dari Firestore
                    teamName = teamName,
                    requestId = requestId,
                    teamId = teamId,
                    requesterId = requesterId // ✅ TAMBAH requesterId untuk ambil userId di notifikasi
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
                // ✅ Cek dulu apakah user sudah member (double check)
                val isMemberResult = teamRepository.isUserMemberOfTeam(teamId, requesterId)
                if (isMemberResult.isSuccess && isMemberResult.getOrNull() == true) {
                    _uiState.value = JoinRequestUiState.Error("User sudah menjadi member tim ini")
                    return@launch
                }

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

    // ✅ FIX: Tambah requesterId parameter
    private fun sendNotificationToAdmin(
        captainId: String,
        requesterName: String,
        teamName: String,
        requestId: String,
        teamId: String,
        requesterId: String // ✅ TAMBAH INI
    ) {
        viewModelScope.launch {
            Log.d("JoinRequestViewModel", "Creating notification for userId: $captainId")

            val notification = NotificationModel(
                userId = captainId, // Admin yang terima notif
                type = NotificationType.JOIN_REQUEST,
                title = "Permintaan Bergabung",
                message = "$requesterName ingin bergabung ke tim $teamName",
                relatedId = requestId,
                relatedType = "REQUEST",
                senderName = requesterName, // ✅ Nama lengkap dari Firestore
                createdAt = Timestamp.now(),
                isRead = false,
                actionData = mapOf(
                    "requestId" to requestId,
                    "teamId" to teamId,
                    "requesterId" to requesterId // ✅ Tambahkan ini untuk trace userId
                )
            )

            val result = notificationRepository.createNotification(notification)
            if (result.isSuccess) {
                Log.d("JoinRequestViewModel", "Notification created successfully: ${result.getOrNull()}")
            } else {
                Log.e("JoinRequestViewModel", "Failed to create notification: ${result.exceptionOrNull()?.message}")
            }
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