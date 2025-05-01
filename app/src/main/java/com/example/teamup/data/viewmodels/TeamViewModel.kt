package com.example.teamup.data.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.R
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.repositories.TeamRepository
import com.example.teamup.data.sources.remote.GoogleDriveHelper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeamViewModel(
    private val repository: TeamRepository,
    private val driveHelper: GoogleDriveHelper
) : ViewModel() {

    private val _teams = MutableStateFlow<List<TeamModel>>(emptyList())
    val teams: StateFlow<List<TeamModel>> = _teams

    private val _userTeams = MutableStateFlow<List<TeamModel>>(emptyList())
    val userTeams: StateFlow<List<TeamModel>> = _userTeams

    private val _selectedTeam = MutableStateFlow<TeamModel?>(null)
    val selectedTeam: StateFlow<TeamModel?> = _selectedTeam

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            try {
                repository.initialize() // Inisialisasi Google Drive Service
                getAllTeams()
                loadUserTeams() // Muat tim yang diikuti pengguna saat ini
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to initialize: ${e.message}") }
            }
        }
    }

    fun addTeam(
        name: String,
        description: String,
        category: String,
        avatarResId: Int = R.drawable.captain_icon,
        imageUri: Uri?,
        maxMembers: Int,
        isPrivate: Boolean,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val members = if (currentUserId != null) listOf(currentUserId) else emptyList()

                val team = TeamModel(
                    name = name,
                    description = description,
                    category = category,
                    avatarResId = avatarResId,
                    imageUrl = null,
                    driveFileId = null,
                    createdAt = Timestamp.now(),
                    members = members,
                    maxMembers = maxMembers,
                    isPrivate = isPrivate,
                    memberCount = members.size,
                    captainId = currentUserId ?: ""
                )

                val teamId = repository.addTeam(team, imageUri)
                if (teamId != null) {
                    // Reload teams
                    getAllTeams()
                    loadUserTeams()
                    onSuccess(teamId)
                } else {
                    onFailure(Exception("Failed to add team"))
                }
            } catch (e: Exception) {
                onFailure(e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getAllTeams() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val fetchedTeams = repository.getAllTeams()
                _teams.value = fetchedTeams
                _uiState.update { it.copy(teams = fetchedTeams) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error fetching teams: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getTeamById(teamId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val team = repository.getTeamById(teamId)
                _selectedTeam.value = team
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error fetching team: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadUserTeams() {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId != null) {
                try {
                    val userTeams = repository.getTeamsByUserId(currentUserId)
                    _userTeams.value = userTeams
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = "Error fetching your teams: ${e.message}") }
                }
            }
        }
    }

    fun joinTeam(teamId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId != null) {
                _uiState.update { it.copy(isLoading = true) }

                try {
                    val success = repository.joinTeam(teamId, currentUserId)
                    if (success) {
                        // Reload data
                        getAllTeams()
                        loadUserTeams()
                        onSuccess()
                    } else {
                        onFailure("Failed to join the team")
                    }
                } catch (e: Exception) {
                    onFailure("Error: ${e.message}")
                } finally {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                onFailure("You need to be logged in to join a team")
            }
        }
    }

    fun leaveTeam(teamId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId != null) {
                _uiState.update { it.copy(isLoading = true) }

                try {
                    val success = repository.leaveTeam(teamId, currentUserId)
                    if (success) {
                        // Reload data
                        getAllTeams()
                        loadUserTeams()
                        onSuccess()
                    } else {
                        onFailure("Failed to leave the team")
                    }
                } catch (e: Exception) {
                    onFailure("Error: ${e.message}")
                } finally {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                onFailure("You need to be logged in to leave a team")
            }
        }
    }

    data class TeamUiState(
        val isLoading: Boolean = false,
        val teams: List<TeamModel> = emptyList(),
        val errorMessage: String? = null
    )
}

class TeamViewModelFactory(
    private val repository: TeamRepository,
    private val driveHelper: GoogleDriveHelper
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamViewModel::class.java)) {
            return TeamViewModel(repository, driveHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}