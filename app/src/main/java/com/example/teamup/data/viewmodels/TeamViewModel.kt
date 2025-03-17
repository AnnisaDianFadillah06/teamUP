package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.repositories.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamUiState(
    val isLoading: Boolean = false,
    val teams: List<TeamModel> = emptyList(),
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class TeamViewModel(private val repository: TeamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        getAllTeams()
    }

    fun addTeam(name: String, description: String, category: String) {
        _uiState.update { it.copy(isLoading = true, isSuccess = false, errorMessage = null) }

        val team = TeamModel(
            name = name,
            description = description,
            category = category
        )

        viewModelScope.launch {
            try {
                val teamId = repository.addTeam(team)
                if (teamId != null) {
                    _uiState.update { it.copy(isSuccess = true) }
                    getAllTeams() // Refresh list setelah menambah
                } else {
                    _uiState.update { it.copy(errorMessage = "Gagal menambahkan tim") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getAllTeams() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val teams = repository.getAllTeams()
                _uiState.update { it.copy(teams = teams) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}

class TeamViewModelFactory(private val repository: TeamRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}