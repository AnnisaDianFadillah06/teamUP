package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.CabangLombaModel
import com.example.teamup.data.repositories.CabangLombaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CabangLombaViewModel(
    private val repository: CabangLombaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CabangLombaUiState())
    val uiState: StateFlow<CabangLombaUiState> = _uiState.asStateFlow()

    // Load all cabang lomba at once rather than per competition
    fun getAllCabangLomba() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getAllCabangLomba().collect { cabangList ->
                    _uiState.update {
                        it.copy(
                            allCabangList = cabangList,
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

    fun getCabangLombaByCompetitionId(competitionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getCabangLombaByCompetitionId(competitionId).collect { cabangList ->
                    _uiState.update {
                        it.copy(
                            cabangList = cabangList,
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

    fun addCabangLomba(competitionId: String, namaCabang: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cabangLomba = CabangLombaModel(
                    competitionId = competitionId,
                    namaCabang = namaCabang
                )
                repository.addCabangLomba(cabangLomba)
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

    fun addMultipleCabangLomba(competitionId: String, cabangNames: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cabangLombaList = cabangNames.map { namaCabang ->
                    CabangLombaModel(
                        competitionId = competitionId,
                        namaCabang = namaCabang
                    )
                }
                repository.addMultipleCabangLomba(cabangLombaList)
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

    data class CabangLombaUiState(
        val cabangList: List<CabangLombaModel> = emptyList(),
        val allCabangList: List<CabangLombaModel> = emptyList(), // Store all cabang lomba for filtering
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false
    )
}

class CabangLombaViewModelFactory(
    private val repository: CabangLombaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CabangLombaViewModel::class.java)) {
            return CabangLombaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}