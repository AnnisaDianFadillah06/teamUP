package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.MemberInviteModelV2
import com.example.teamup.data.model.ProfileModel
import com.example.teamup.data.repositories.InviteMemberRepositoryV2
import com.example.teamup.data.repositories.NotificationRepositoryV2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InviteMemberViewModel(
    private val inviteMemberRepository: InviteMemberRepositoryV2,
    private val notificationRepository: NotificationRepositoryV2
) : ViewModel() {

    private val _pendingInvitations = MutableStateFlow<List<MemberInviteModelV2>>(emptyList())
    val pendingInvitations: StateFlow<List<MemberInviteModelV2>> = _pendingInvitations.asStateFlow()

    private val _waitingInvitations = MutableStateFlow<List<MemberInviteModelV2>>(emptyList())
    val waitingInvitations: StateFlow<List<MemberInviteModelV2>> = _waitingInvitations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    init {
        loadInvitations()
    }

    fun loadInvitations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val pending = inviteMemberRepository.getPendingInvitations()
                val waiting = inviteMemberRepository.getWaitingInvitations()

                _pendingInvitations.value = pending
                _waitingInvitations.value = waiting
            } catch (e: Exception) {
                _actionState.value = ActionState.Error(e.message ?: "Error loading invitations")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendInvitations(members: List<ProfileModel>, teamId: String, teamName: String) {
        viewModelScope.launch {
            try {
                _actionState.value = ActionState.Loading

                val success = inviteMemberRepository.sendInvitations(members, teamId, teamName)

                if (success) {
                    _actionState.value = ActionState.Success("Undangan berhasil dikirim ke ${members.size} anggota")
                    loadInvitations() // Refresh data setelah kirim undangan
                } else {
                    _actionState.value = ActionState.Error("Gagal mengirim undangan")
                }
            } catch (e: Exception) {
                _actionState.value = ActionState.Error(e.message ?: "Terjadi kesalahan saat mengirim undangan")
            }
        }
    }

    fun rejectInvitation(inviteId: String) {
        viewModelScope.launch {
            try {
                _actionState.value = ActionState.Loading

                val success = inviteMemberRepository.rejectInvitation(inviteId)

                if (success) {
                    _actionState.value = ActionState.Success("Undangan ditolak")
                    loadInvitations()
                } else {
                    _actionState.value = ActionState.Error("Gagal menolak undangan")
                }
            } catch (e: Exception) {
                _actionState.value = ActionState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ActionState.Idle
    }

    sealed class ActionState {
        object Idle : ActionState()
        object Loading : ActionState()
        data class Success(val message: String) : ActionState()
        data class Error(val message: String) : ActionState()
    }

    class Factory(
        private val inviteMemberRepository: InviteMemberRepositoryV2,
        private val notificationRepository: NotificationRepositoryV2
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InviteMemberViewModel::class.java)) {
                return InviteMemberViewModel(inviteMemberRepository, notificationRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun acceptInvitation(inviteId: String) {
        viewModelScope.launch {
            try {
                _actionState.value = ActionState.Loading
                val success = inviteMemberRepository.acceptInvitation(inviteId)
                if (success) {
                    _actionState.value = ActionState.Success("Undangan diterima")
                    loadInvitations()
                } else {
                    _actionState.value = ActionState.Error("Gagal menerima undangan")
                }
            } catch (e: Exception) {
                _actionState.value = ActionState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}