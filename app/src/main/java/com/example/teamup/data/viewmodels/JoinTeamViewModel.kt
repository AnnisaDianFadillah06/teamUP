package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.CompetitionModelDummy
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.repositories.TeamRepository
import com.example.teamup.data.repositories.CompetitionRepositoryDummy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JoinTeamViewModel(
    private val teamRepository: TeamRepository,
    private val competitionRepository: CompetitionRepositoryDummy
) : ViewModel() {
    private val _categories = MutableStateFlow<List<CompetitionModelDummy>>(emptyList())
    val categories: StateFlow<List<CompetitionModelDummy>> = _categories

    private val _popularTeams = MutableStateFlow<List<TeamModel>>(emptyList())
    val popularTeams: StateFlow<List<TeamModel>> = _popularTeams

    fun loadCategories() {
        viewModelScope.launch {
            competitionRepository.getCategories().collect { categoriesList ->
                _categories.value = categoriesList
            }
        }
    }

    fun loadPopularTeams() {
        viewModelScope.launch {
            competitionRepository.getPopularTeams().collect { teamsList ->
                _popularTeams.value = teamsList
            }
        }
    }

    // If you need to fetch teams from Firebase
    private val _allTeams = MutableStateFlow<List<TeamModel>>(emptyList())
    val allTeams: StateFlow<List<TeamModel>> = _allTeams

    fun loadAllTeamsFromFirebase() {
        viewModelScope.launch {
            try {
                val teams = teamRepository.getAllTeams()
                _allTeams.value = teams
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}