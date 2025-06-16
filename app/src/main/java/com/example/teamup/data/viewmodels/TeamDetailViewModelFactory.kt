package com.example.teamup.data.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.model.UserProfileData
import com.example.teamup.data.repositories.TeamRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TeamDetailViewModel(private val teamRepository: TeamRepository) : ViewModel() {
    private val TAG = "TeamDetailViewModel"
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val firestore = FirebaseFirestore.getInstance()

    // UI state for loading, errors, and join request
    private val _uiState = MutableStateFlow(TeamDetailUiState())
    val uiState: StateFlow<TeamDetailUiState> = _uiState.asStateFlow()

    // Team data
    private val _team = MutableStateFlow<TeamModel?>(null)
    val team: StateFlow<TeamModel?> = _team.asStateFlow()

    // Team members data
    private val _teamMembers = MutableStateFlow<List<UserProfileData>>(emptyList())
    val teamMembers: StateFlow<List<UserProfileData>> = _teamMembers.asStateFlow()

    // Captain data
    private val _teamAdmin = MutableStateFlow<UserProfileData?>(null)
    val teamAdmin: StateFlow<UserProfileData?> = _teamAdmin.asStateFlow()

    // Request to join team
    fun requestToJoinTeam(teamId: String, teamName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val userId = currentUser?.uid ?: throw Exception("User not authenticated")
                val userProfile = teamRepository.getUserProfile(userId)
                    ?: throw Exception("User profile not found")

                val team = teamRepository.getTeamById(teamId)
                    ?: throw Exception("Team not found")

                // Check if user is already a member
                if (team.members.contains(userId)) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "You are already a member of this team"
                    )
                    return@launch
                }

                // Check if team is full
                if (team.isFull || team.memberCount >= team.maxMembers) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Team is full"
                    )
                    return@launch
                }

                // Create invitation
                val inviteId = firestore.collection("invitations").document().id
                val captainId = team.captainId.ifEmpty { team.members.firstOrNull() }
                    ?: throw Exception("No captain found for team")

                val captainProfile = teamRepository.getUserProfile(captainId)
                    ?: throw Exception("Captain profile not found")

                val inviteData = mapOf(
                    "id" to inviteId,
                    "senderId" to userId,
                    "senderName" to userProfile.fullName,
                    "senderEmail" to userProfile.email,
                    "recipientId" to captainId,
                    "recipientName" to captainProfile.fullName,
                    "recipientEmail" to captainProfile.email,
                    "teamId" to teamId,
                    "teamName" to teamName,
                    "status" to "WAITING",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("invitations").document(inviteId)
                    .set(inviteData)
                    .await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    joinRequestSuccess = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sending join request: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to send join request"
                )
            }
        }
    }

    // Upload team photo
    fun updateTeamPhoto(teamId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val success = teamRepository.updateTeamPhoto(teamId, imageUri)
                if (success) {
                    loadTeamData(teamId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update team photo"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating team photo: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    // Load team and member data
    fun loadTeamData(teamId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                Log.d(TAG, "Loading team data for ID: $teamId")

                val teamData = teamRepository.getTeamById(teamId)
                Log.d(TAG, "Team data loaded: ${teamData?.name}, Members: ${teamData?.members}, CaptainId: '${teamData?.captainId}'")
                _team.value = teamData

                if (teamData != null) {
                    loadTeamMembers(teamData)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Team not found"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading team data: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadTeamMembers(team: TeamModel) {
        try {
            Log.d(TAG, "Loading team members. Team: ${team.name}, Members: ${team.members}")
            val memberIds = team.members.distinct()
            val memberProfiles = mutableListOf<UserProfileData>()
            var captainId: String? = null

            when {
                team.captainId.isNotEmpty() -> captainId = team.captainId
                memberIds.isNotEmpty() -> captainId = memberIds.first()
                else -> Log.w(TAG, "No captain found - no captainId and no members")
            }

            for ((index, userId) in memberIds.withIndex()) {
                Log.d(TAG, "Loading member ${index + 1}/${memberIds.size}: $userId")
                try {
                    val userProfile = teamRepository.getUserProfile(userId)
                    if (userProfile != null) {
                        Log.d(TAG, "Member loaded: ${userProfile.fullName}")
                        memberProfiles.add(userProfile)
                    } else {
                        Log.w(TAG, "User profile not found for ID: $userId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading user profile for $userId: ${e.message}", e)
                }
            }

            if (captainId != null) {
                Log.d(TAG, "Looking for captain in loaded members: $captainId")
                val captainProfile = memberProfiles.find { it.userId == captainId }
                if (captainProfile != null) {
                    _teamAdmin.value = captainProfile
                } else {
                    Log.w(TAG, "Captain not found in member profiles, loading separately")
                    try {
                        val captainProfile = teamRepository.getUserProfile(captainId)
                        if (captainProfile != null) {
                            _teamAdmin.value = captainProfile
                            if (memberProfiles.none { it.userId == captainId }) {
                                memberProfiles.add(0, captainProfile)
                            }
                        } else {
                            Log.w(TAG, "Captain profile not found for ID: $captainId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading captain profile: ${e.message}", e)
                    }
                }
            }

            _teamMembers.value = memberProfiles
            _uiState.value = _uiState.value.copy(isLoading = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading team members: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Error loading team members: ${e.message}"
            )
        }
    }

    fun isCurrentUserAdmin(): Boolean {
        val currentUserId = currentUser?.uid ?: return false
        val team = _team.value ?: return false
        return when {
            team.captainId.isNotEmpty() -> team.captainId == currentUserId
            team.members.isNotEmpty() -> team.members.first() == currentUserId
            else -> false
        }
    }

    fun isCurrentUserMember(): Boolean {
        val currentUserId = currentUser?.uid ?: return false
        return _team.value?.members?.contains(currentUserId) == true
    }

    fun isCurrentUserInTeam(): Boolean {
        return isCurrentUserAdmin() || isCurrentUserMember()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearJoinRequestSuccess() {
        _uiState.value = _uiState.value.copy(joinRequestSuccess = false)
    }
}

data class TeamDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinRequestSuccess: Boolean = false // New state for join request success
)

class TeamDetailViewModelFactory(private val teamRepository: TeamRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamDetailViewModel::class.java)) {
            return TeamDetailViewModel(teamRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}