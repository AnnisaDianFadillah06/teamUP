package com.example.teamup.data.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.R
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.repositories.TeamRepository
import com.example.teamup.data.sources.remote.GoogleDriveHelper
import com.google.firebase.Timestamp
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

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.initialize() // Inisialisasi Google Drive Service
                getAllTeams()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to initialize: ${e.message}") }
            }
        }
    }

    fun addTeam(
        name: String,
        description: String,
        category: String,
        avatarResId: String,
        imageUri: Uri?,
        maxMembers: Int,
        isPrivate: Boolean,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val team = TeamModel(
                    name = name,
                    description = description,
                    category = category,
                    avatarResId = R.drawable.captain_icon, // bisa diganti sesuai avatarResId
                    imageUrl = null,
                    driveFileId = null,
                    createdAt = Timestamp.now(),
                    maxMembers = maxMembers,
                    isPrivate = isPrivate
                )
                val teamId = repository.addTeam(team, imageUri)
                if (teamId != null) {
                    onSuccess(teamId)
                } else {
                    onFailure(Exception("Failed to add team"))
                }
            } catch (e: Exception) {
                onFailure(e)
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