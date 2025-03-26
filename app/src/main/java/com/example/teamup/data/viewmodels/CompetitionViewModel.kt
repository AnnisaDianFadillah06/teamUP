package com.example.teamup.data.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.teamup.data.model.CompetitionModel

class CompetitionViewModel : ViewModel() {
    private val _competitions = mutableStateOf<List<CompetitionModel>>(emptyList())
    val competitions: State<List<CompetitionModel>> = _competitions

    fun addCompetition(competition: CompetitionModel) {
        _competitions.value = _competitions.value + competition
    }

    fun removeCompetition(competition: CompetitionModel) {
        _competitions.value = _competitions.value.filter { it != competition }
    }

    fun clearCompetitions() {
        _competitions.value = emptyList()
    }
}