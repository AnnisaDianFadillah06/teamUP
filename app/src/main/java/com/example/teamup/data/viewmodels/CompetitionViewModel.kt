package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.repositories.CompetitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompetitionViewModel(
    private val repository: CompetitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetitionUiState())
    val uiState: StateFlow<CompetitionUiState> = _uiState.asStateFlow()

    init {
        getAllCompetitions()
    }

    private fun getAllCompetitions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getAllCompetitions().collect { competitions ->
                    _uiState.update {
                        it.copy(
                            competitions = competitions,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addCompetition(
        namaLomba: String,
        cabangLombaList: List<String>, // Changed to accept list
        tanggalPelaksanaan: String,
        deskripsiLomba: String,
        imageUrl: String = "",
        fileUrl: String = "",
        jumlahTim: Int = 0,
        status: String = "Published"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val competition = CompetitionModel(
                    namaLomba = namaLomba,
                    cabangLomba = cabangLombaList.firstOrNull() ?: "", // Keep first for backward compatibility
                    cabangLombaList = cabangLombaList,
                    tanggalPelaksanaan = tanggalPelaksanaan,
                    deskripsiLomba = deskripsiLomba,
                    imageUrl = imageUrl,
                    fileUrl = fileUrl,
                    jumlahTim = jumlahTim,
                    status = status
                )
                repository.addCompetition(competition)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    // Existing functions
    fun setError(errorMessage: String) {
        _uiState.update {
            it.copy(errorMessage = errorMessage)
        }
    }

    fun resetSuccess() {
        _uiState.update {
            it.copy(isSuccess = false)
        }
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }

    data class CompetitionUiState(
        val competitions: List<CompetitionModel> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false
    )
}

class CompetitionViewModelFactory(
    private val repository: CompetitionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompetitionViewModel::class.java)) {
            return CompetitionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}