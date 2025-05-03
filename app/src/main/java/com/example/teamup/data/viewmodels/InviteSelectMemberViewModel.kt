package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.R
import com.example.teamup.data.model.ProfileModel
import com.example.teamup.presentation.screen.FilterOption
import com.example.teamup.presentation.screen.FilterType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InviteSelectMemberViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilters = MutableStateFlow<List<FilterOption>>(emptyList())
    val selectedFilters: StateFlow<List<FilterOption>> = _selectedFilters.asStateFlow()

    private val _members = MutableStateFlow<List<ProfileModel>>(
        listOf(
            ProfileModel("1", "Annisa Dian", "annisadian@gmail.com", R.drawable.captain_icon, "Universitas Indonesia", "Informatika", listOf("UI/UX", "Mobile")),
            ProfileModel("2", "Annisa Dian", "annisa.dian@gmail.com", R.drawable.captain_icon, "Universitas Indonesia", "Elektro", listOf("Mobile", "Backend")),
            ProfileModel("3", "Annisa Dian", "dian.annisa@gmail.com", R.drawable.captain_icon, "Universitas Gadjah Mada", "Informatika", listOf("Frontend", "UI/UX")),
            ProfileModel("4", "Annisa Dian", "annisa.d@gmail.com", R.drawable.captain_icon, "Institut Teknologi Bandung", "Mesin", listOf("Backend", "Database")),
            ProfileModel("5", "Annisa Dian", "ad.annisa@gmail.com", R.drawable.captain_icon, "Universitas Brawijaya", "Elektro", listOf("Mobile", "Database")),
            ProfileModel("6", "Annisa Dian", "annisa.dian01@gmail.com", R.drawable.captain_icon, "Universitas Indonesia", "Informatika", listOf("UI/UX", "Frontend")),
        )
    )
    val members: StateFlow<List<ProfileModel>> = _members.asStateFlow()

    private val _filteredMembers = MutableStateFlow<List<ProfileModel>>(emptyList())
    val filteredMembers: StateFlow<List<ProfileModel>> = _filteredMembers.asStateFlow()

    // Filter options
    val universityFilters = listOf(
        FilterOption("ui", "Universitas Indonesia", FilterType.UNIVERSITY),
        FilterOption("ugm", "Universitas Gadjah Mada", FilterType.UNIVERSITY),
        FilterOption("itb", "Institut Teknologi Bandung", FilterType.UNIVERSITY),
        FilterOption("ub", "Universitas Brawijaya", FilterType.UNIVERSITY),
        FilterOption("unair", "Universitas Airlangga", FilterType.UNIVERSITY)
    )

    val majorFilters = listOf(
        FilterOption("informatika", "Informatika", FilterType.MAJOR),
        FilterOption("elektro", "Elektro", FilterType.MAJOR),
        FilterOption("mesin", "Mesin", FilterType.MAJOR)
    )

    val skillFilters = listOf(
        FilterOption("mobile", "Mobile", FilterType.SKILL),
        FilterOption("uiux", "UI/UX", FilterType.SKILL),
        FilterOption("frontend", "Frontend", FilterType.SKILL),
        FilterOption("backend", "Backend", FilterType.SKILL),
        FilterOption("database", "Database", FilterType.SKILL)
    )

    init {
        viewModelScope.launch {
            combine(_members, _selectedFilters, _searchQuery) { members, filters, query ->
                getFilteredMembers(members, filters, query)
            }.collect {
                _filteredMembers.value = it
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addFilter(filter: FilterOption) {
        if (!_selectedFilters.value.contains(filter)) {
            _selectedFilters.value = _selectedFilters.value + filter
        }
    }

    fun removeFilter(filter: FilterOption) {
        _selectedFilters.value = _selectedFilters.value.filter { it.id != filter.id }
    }

    fun clearAllFilters() {
        _selectedFilters.value = emptyList()
    }

    fun toggleMemberSelection(memberId: String) {
        _members.value = _members.value.map { member ->
            if (member.id == memberId) {
                member.copy(isSelected = !member.isSelected)
            } else {
                member
            }
        }
    }

    fun getSelectedMembers(): List<ProfileModel> {
        return _members.value.filter { it.isSelected }
    }

    private fun getFilteredMembers(
        members: List<ProfileModel>,
        filters: List<FilterOption>,
        query: String
    ): List<ProfileModel> {
        val searchLower = query.lowercase()

        return members.filter { member ->
            val matchesSearch = member.name.lowercase().contains(searchLower) ||
                    member.email.lowercase().contains(searchLower)

            val universityFilters = filters.filter { it.type == FilterType.UNIVERSITY }
            val majorFilters = filters.filter { it.type == FilterType.MAJOR }
            val skillFilters = filters.filter { it.type == FilterType.SKILL }

            val matchesUniversity = universityFilters.isEmpty() ||
                    universityFilters.any { it.name == member.university }

            val matchesMajor = majorFilters.isEmpty() ||
                    majorFilters.any { it.name == member.major }

            val matchesSkill = skillFilters.isEmpty() ||
                    skillFilters.any { it.name in member.skills }

            matchesSearch && matchesUniversity && matchesMajor && matchesSkill
        }
    }
}
