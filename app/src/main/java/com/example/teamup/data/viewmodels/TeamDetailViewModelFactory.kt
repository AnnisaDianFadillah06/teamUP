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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamDetailViewModel(private val teamRepository: TeamRepository) : ViewModel() {
    private val TAG = "TeamDetailViewModel"
    private val currentUser = FirebaseAuth.getInstance().currentUser

    // UI state for loading, errors
    private val _uiState = MutableStateFlow(TeamDetailUiState())
    val uiState: StateFlow<TeamDetailUiState> = _uiState.asStateFlow()

    // Team data
    private val _team = MutableStateFlow<TeamModel?>(null)
    val team: StateFlow<TeamModel?> = _team.asStateFlow()

    // Team members data - semua anggota termasuk captain
    private val _teamMembers = MutableStateFlow<List<UserProfileData>>(emptyList())
    val teamMembers: StateFlow<List<UserProfileData>> = _teamMembers.asStateFlow()

    // Captain data - untuk identifikasi admin
    private val _teamAdmin = MutableStateFlow<UserProfileData?>(null)
    val teamAdmin: StateFlow<UserProfileData?> = _teamAdmin.asStateFlow()

    // Upload team photo
    fun updateTeamPhoto(teamId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val success = teamRepository.updateTeamPhoto(teamId, imageUri)
                if (success) {
                    // Refresh team data
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

                // Get team data
                val teamData = teamRepository.getTeamById(teamId)
                Log.d(TAG, "Team data loaded: ${teamData?.name}, Members: ${teamData?.members}, CaptainId: '${teamData?.captainId}'")
                _team.value = teamData

                if (teamData != null) {
                    // Load team members data
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

            val memberIds = team.members.distinct() // Remove duplicates
            val memberProfiles = mutableListOf<UserProfileData>()

            // Determine captain logic
            var captainId: String? = null

            when {
                // Case 1: captainId is explicitly set and not empty
                team.captainId.isNotEmpty() -> {
                    captainId = team.captainId
                    Log.d(TAG, "Using explicit captainId: $captainId")
                }
                // Case 2: captainId is empty, use first member as captain
                memberIds.isNotEmpty() -> {
                    captainId = memberIds.first()
                    Log.d(TAG, "CaptainId empty, using first member as captain: $captainId")
                }
                else -> {
                    Log.w(TAG, "No captain found - no captainId and no members")
                }
            }

            // Fetch all members data first
            Log.d(TAG, "Loading ${memberIds.size} team members")
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

            // Set captain/admin data (find from loaded members)
            if (captainId != null) {
                Log.d(TAG, "Looking for captain in loaded members: $captainId")
                val captainProfile = memberProfiles.find { it.userId == captainId }
                if (captainProfile != null) {
                    Log.d(TAG, "Captain found in members: ${captainProfile.fullName}")
                    _teamAdmin.value = captainProfile
                } else {
                    Log.w(TAG, "Captain not found in member profiles, loading separately")
                    try {
                        val captainProfile = teamRepository.getUserProfile(captainId)
                        if (captainProfile != null) {
                            Log.d(TAG, "Captain loaded separately: ${captainProfile.fullName}")
                            _teamAdmin.value = captainProfile
                            // Add captain to members if not already there
                            if (memberProfiles.none { it.userId == captainId }) {
                                memberProfiles.add(0, captainProfile) // Add at beginning
                            }
                        } else {
                            Log.w(TAG, "Captain profile not found for ID: $captainId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading captain profile: ${e.message}", e)
                    }
                }
            }

            Log.d(TAG, "Final result - ${memberProfiles.size} member profiles loaded")
            Log.d(TAG, "Captain: ${_teamAdmin.value?.fullName ?: "None"}")
            memberProfiles.forEach { member ->
                Log.d(TAG, "Member: ${member.fullName} (${member.userId}) - Is Captain: ${member.userId == captainId}")
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
            // Check explicit captainId first
            team.captainId.isNotEmpty() -> team.captainId == currentUserId
            // If captainId is empty, check if current user is first member
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
}

data class TeamDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null
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