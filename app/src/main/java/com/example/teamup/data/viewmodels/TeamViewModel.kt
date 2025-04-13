package com.example.teamup.data.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.R
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.repositories.TeamRepository
import com.example.teamup.data.sources.remote.FirebaseStorageHelper
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class TeamViewModel(
    private val repository: TeamRepository,
    private val storageHelper: FirebaseStorageHelper = FirebaseStorageHelper()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState

    private val _teams = MutableStateFlow<List<TeamModel>>(emptyList())
    val teams: StateFlow<List<TeamModel>> = _teams

    init {
        getAllTeams()
    }

    // Updated addTeam function in TeamViewModel
    fun addTeam(
        name: String,
        description: String,
        category: String,
        avatarUri: String,
        maxMembers: Int = 5,
        isPrivate: Boolean = true
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Default avatar resource ID
                val defaultAvatarResId = R.drawable.captain_icon

                // If it's a URI, upload to Firebase Storage
                val imageUrl = if (avatarUri != "default_avatar") {
                    try {
                        // Parse the URI
                        val uri = Uri.parse(avatarUri)
                        // Upload and get download URL
                        storageHelper.uploadImage(uri)
                    } catch (e: Exception) {
                        // If upload fails, use default and continue
                        println("DEBUG: Failed to upload image: ${e.message}")
                        null
                    }
                } else null

                // Create team with all parameters
                val team = TeamModel(
                    name = name,
                    description = description,
                    category = category,
                    avatarResId = defaultAvatarResId,
                    imageUrl = imageUrl,
                    createdAt = Timestamp.now(),
                    maxMembers = maxMembers,
                    isPrivate = isPrivate,
                    memberCount = 1, // Starting with 1 member (creator)
                    isJoined = true, // Creator has joined by default
                    isFull = false // Not full initially
                )

                val teamId = repository.addTeam(team)
                if (teamId != null) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    getAllTeams() // Refresh the team list after adding a new team
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to create team"
                        )
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error in addTeam: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An unknown error occurred"
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    // Function to get all teams
    fun getAllTeams() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                println("DEBUG: Starting to fetch teams...")
                val teamsList = repository.getAllTeams()
                println("DEBUG: Teams loaded: ${teamsList.size}")
                teamsList.forEach {
                    println("DEBUG: Team: ${it.name}, ID: ${it.id}, Category: ${it.category}, ImageUrl: ${it.imageUrl}")
                }
                _teams.value = teamsList
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                println("DEBUG: Error loading teams: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load teams"
                    )
                }
            }
        }
    }
}

class TeamViewModelFactory(
    private val repository: TeamRepository,
    private val storageHelper: FirebaseStorageHelper = FirebaseStorageHelper()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamViewModel::class.java)) {
            return TeamViewModel(repository, storageHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}