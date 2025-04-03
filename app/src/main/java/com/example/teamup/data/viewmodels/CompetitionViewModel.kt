package com.example.teamup.data.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.repositories.CompetitionRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class CompetitionViewModel(private val repository: CompetitionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetitionUiState())
    val uiState: StateFlow<CompetitionUiState> = _uiState.asStateFlow()

    init {
        loadCompetitions()
    }

    private fun loadCompetitions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val competitions = repository.getAllCompetitions()
                _uiState.update { it.copy(competitions = competitions, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load competitions: ${e.message}"
                    )
                }
            }
        }
    }

    fun addCompetition(
        namaLomba: String,
        cabangLomba: String,
        tanggalPelaksanaan: String,
        deskripsiLomba: String,
        imageUrl: String = "",
        fileUrl: String = "",
        jumlahTim: Int = 0
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val competition = CompetitionModel(
                    namaLomba = namaLomba,
                    cabangLomba = cabangLomba,
                    tanggalPelaksanaan = tanggalPelaksanaan,
                    deskripsiLomba = deskripsiLomba,
                    imageUrl = imageUrl,
                    fileUrl = fileUrl,
                    jumlahTim = jumlahTim,
                    createdAt = Timestamp.now()
                )

                val competitionId = repository.addCompetition(competition)
                if (competitionId != null) {
                    loadCompetitions() // Refresh the list first
                    _uiState.update { it.copy(isSuccess = true, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to add competition"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    data class CompetitionUiState(
        val competitions: List<CompetitionModel> = emptyList(),
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val errorMessage: String? = null
    )
}

// Example of what your factory might look like
class CompetitionViewModelFactory(
    private val repository: CompetitionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompetitionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompetitionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}