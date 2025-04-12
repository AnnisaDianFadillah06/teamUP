package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.CabangLombaModel
import com.example.teamup.data.model.CompetitionActivityStatus
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.model.CompetitionVisibilityStatus
import com.example.teamup.data.repositories.CabangLombaRepository
import com.example.teamup.data.repositories.CompetitionRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CompetitionViewModel(
    private val repository: CompetitionRepository,
    private val cabangLombaRepository: CabangLombaRepository
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
        cabangLombaList: List<String>,
        tanggalPelaksanaan: String,
        deskripsiLomba: String,
        imageUrl: String = "",
        fileUrl: String = "",
        jumlahTim: Int = 0,
        visibilityStatus: String = CompetitionVisibilityStatus.PUBLISHED.value,
        activityStatus: String = CompetitionActivityStatus.ACTIVE.value,
        tanggalTutupPendaftaran: String? = null,
        autoCloseEnabled: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Parse tanggalTutupPendaftaran if provided
                val deadlineTimestamp = tanggalTutupPendaftaran?.let {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = dateFormat.parse(it)
                    date?.let { d -> Timestamp(d) }
                }

                // First, create the competition
                val competition = CompetitionModel(
                    namaLomba = namaLomba,
                    tanggalPelaksanaan = tanggalPelaksanaan,
                    deskripsiLomba = deskripsiLomba,
                    imageUrl = imageUrl,
                    fileUrl = fileUrl,
                    jumlahTim = jumlahTim,
                    visibilityStatus = visibilityStatus,
                    activityStatus = activityStatus,
                    tanggalTutupPendaftaran = deadlineTimestamp,
                    autoCloseEnabled = autoCloseEnabled
                )

                // Add the competition and get its ID
                val competitionId = repository.addCompetition(competition)

                // Then, add all cabang lomba entries linked to this competition
                val cabangEntries = cabangLombaList.filter { it.isNotBlank() }.map { cabangName ->
                    CabangLombaModel(
                        competitionId = competitionId,
                        namaCabang = cabangName
                    )
                }

                if (cabangEntries.isNotEmpty()) {
                    cabangLombaRepository.addMultipleCabangLomba(cabangEntries)
                }

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

    fun updateCompetitionStatus(
        competitionId: String,
        visibilityStatus: String? = null,
        activityStatus: String? = null,
        tanggalTutupPendaftaran: String? = null,
        autoCloseEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Parse tanggalTutupPendaftaran if provided
                val deadlineTimestamp = tanggalTutupPendaftaran?.let {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = dateFormat.parse(it)
                    date?.let { d -> Timestamp(d) }
                }

                repository.updateCompetitionStatus(
                    competitionId = competitionId,
                    visibilityStatus = visibilityStatus,
                    activityStatus = activityStatus,
                    tanggalTutupPendaftaran = deadlineTimestamp,
                    autoCloseEnabled = autoCloseEnabled
                )

                // Refresh competitions list
                getAllCompetitions()

                _uiState.update {
                    it.copy(isLoading = false, isSuccess = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message, isLoading = false)
                }
            }
        }
    }

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
    private val repository: CompetitionRepository,
    private val cabangLombaRepository: CabangLombaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompetitionViewModel::class.java)) {
            return CompetitionViewModel(repository, cabangLombaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}