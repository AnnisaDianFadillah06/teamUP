package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.ProfileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SharedMemberViewModel : ViewModel() {

    private val _selectedMembers = MutableStateFlow<List<ProfileModel>>(emptyList())
    val selectedMembers: StateFlow<List<ProfileModel>> = _selectedMembers.asStateFlow()

    private val _sendInviteState = MutableStateFlow<SendInviteState>(SendInviteState.Idle)
    val sendInviteState: StateFlow<SendInviteState> = _sendInviteState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun setSelectedMembers(members: List<ProfileModel>) {
        _selectedMembers.value = members
    }

    fun clearSelectedMembers() {
        _selectedMembers.value = emptyList()
    }

    fun removeSelectedMember(memberId: String) {
        _selectedMembers.value = _selectedMembers.value.filter { it.id != memberId }
    }

    fun sendInvitations(teamId: String, teamName: String) {
        viewModelScope.launch {
            try {
                _sendInviteState.value = SendInviteState.Loading

                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _sendInviteState.value = SendInviteState.Error("User tidak terautentikasi")
                    return@launch
                }

                // Get current user data
                val currentUserDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val senderName = currentUserDoc.getString("name") ?: currentUser.displayName ?: "Unknown"
                val senderEmail = currentUserDoc.getString("email") ?: currentUser.email ?: "Unknown"

                val selectedMembersList = _selectedMembers.value
                var successCount = 0
                var failCount = 0

                // Send invitation to each selected member
                for (member in selectedMembersList) {
                    try {
                        val inviteId = firestore.collection("invitations").document().id

                        val inviteData = mapOf(
                            "id" to inviteId,
                            "senderId" to currentUser.uid,
                            "senderName" to senderName,
                            "senderEmail" to senderEmail,
                            "recipientId" to member.id,
                            "recipientName" to member.name,
                            "recipientEmail" to member.email,
                            "teamId" to teamId,
                            "teamName" to teamName,
                            "status" to "WAITING",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )

                        firestore.collection("invitations")
                            .document(inviteId)
                            .set(inviteData)
                            .await()

                        successCount++
                    } catch (e: Exception) {
                        failCount++
                        println("Failed to send invitation to ${member.name}: ${e.message}")
                    }
                }

                if (successCount > 0) {
                    _sendInviteState.value = SendInviteState.Success(
                        "Berhasil mengirim $successCount undangan" +
                                if (failCount > 0) ", $failCount gagal dikirim" else ""
                    )
                    clearSelectedMembers()
                } else {
                    _sendInviteState.value = SendInviteState.Error("Gagal mengirim semua undangan")
                }

            } catch (e: Exception) {
                _sendInviteState.value = SendInviteState.Error(e.message ?: "Terjadi kesalahan saat mengirim undangan")
            }
        }
    }

    fun resetSendInviteState() {
        _sendInviteState.value = SendInviteState.Idle
    }

    sealed class SendInviteState {
        object Idle : SendInviteState()
        object Loading : SendInviteState()
        data class Success(val message: String) : SendInviteState()
        data class Error(val message: String) : SendInviteState()
    }
}