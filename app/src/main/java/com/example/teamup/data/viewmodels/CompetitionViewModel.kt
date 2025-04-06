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
        cabangLomba: String,
        tanggalPelaksanaan: String,
        deskripsiLomba: String,
        imageUrl: String = "",
        fileUrl: String = "",  // Parameter baru untuk URL file
        jumlahTim: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val competition = CompetitionModel(
                    namaLomba = namaLomba,
                    cabangLomba = cabangLomba,
                    tanggalPelaksanaan = tanggalPelaksanaan,
                    deskripsiLomba = deskripsiLomba,
                    imageUrl = imageUrl,
                    fileUrl = fileUrl,  // Mengisi properti baru
                    jumlahTim = jumlahTim
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

    // Fungsi ini untuk mengatur pesan error manual
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

//    // Add this for Snackbar message handling
//    private val _snackbarMessage = MutableLiveData<String?>()
//    val snackbarMessage: LiveData<String?> = _snackbarMessage
//
//    fun showSnackbarMessage(message: String) {
//        _snackbarMessage.value = message
//    }
//
//    fun snackbarShown() {
//        _snackbarMessage.value = null
//    }

    // Tambahkan fungsi ini di CompetitionViewModel.kt
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