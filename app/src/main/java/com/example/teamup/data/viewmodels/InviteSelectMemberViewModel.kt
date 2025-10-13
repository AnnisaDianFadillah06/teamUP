package com.example.teamup.data.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.ProfileModel
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.presentation.screen.FilterOption
import com.example.teamup.presentation.screen.FilterType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InviteSelectMemberViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilters = MutableStateFlow<List<FilterOption>>(emptyList())
    val selectedFilters: StateFlow<List<FilterOption>> = _selectedFilters.asStateFlow()

    private val _members = MutableStateFlow<List<ProfileModel>>(emptyList())
    val members: StateFlow<List<ProfileModel>> = _members.asStateFlow()

    private val _filteredMembers = MutableStateFlow<List<ProfileModel>>(emptyList())
    val filteredMembers: StateFlow<List<ProfileModel>> = _filteredMembers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Dynamic filter options that will be populated from Firestore data
    private val _universityFilters = MutableStateFlow<List<FilterOption>>(emptyList())
    val universityFilters: StateFlow<List<FilterOption>> = _universityFilters.asStateFlow()

    private val _majorFilters = MutableStateFlow<List<FilterOption>>(emptyList())
    val majorFilters: StateFlow<List<FilterOption>> = _majorFilters.asStateFlow()

    private val _skillFilters = MutableStateFlow<List<FilterOption>>(emptyList())
    val skillFilters: StateFlow<List<FilterOption>> = _skillFilters.asStateFlow()

    init {
        // Load data from Firestore when ViewModel is created
        loadMembersFromFirestore()

        // Set up filtering logic
        viewModelScope.launch {
            combine(_members, _selectedFilters, _searchQuery) { members, filters, query ->
                getFilteredMembers(members, filters, query)
            }.collect {
                _filteredMembers.value = it
            }
        }
    }

    private fun loadMembersFromFirestore() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("users")
                    .whereEqualTo("profileCompleted", true)
                    .get()
                    .await()

                val membersList = mutableListOf<ProfileModel>()
                val universities = mutableSetOf<String>()
                val majors = mutableSetOf<String>()
                val skills = mutableSetOf<String>()

                for (document in snapshot.documents) {
                    try {
                        val userData = UserProfileData(
                            userId = document.id,
                            fullName = document.getString("fullName") ?: "",
                            username = document.getString("username") ?: "",
                            email = document.getString("email") ?: "",
                            phone = document.getString("phone") ?: "",
                            university = document.getString("university") ?: "",
                            major = document.getString("major") ?: "",
                            skills = document.get("skills") as? List<String> ?: emptyList(),
                            profilePictureUrl = document.getString("profilePictureUrl") ?: "",
                            profileCompleted = document.getBoolean("profileCompleted") ?: false
                        )

                        // Convert UserProfileData to ProfileModel
                        val profileModel = ProfileModel(
                            id = userData.userId,
                            name = userData.fullName,
                            email = userData.email,
                            imageResId = 0, // Default image resource, you might want to handle profile pictures differently
                            university = userData.university,
                            major = userData.major,
                            skills = userData.skills,
                            profilePictureUrl = userData.profilePictureUrl,
                            isSelected = false
                        )

                        membersList.add(profileModel)

                        // Collect unique values for filters
                        if (userData.university.isNotBlank()) {
                            universities.add(userData.university)
                        }
                        if (userData.major.isNotBlank()) {
                            majors.add(userData.major)
                        }
                        userData.skills.forEach { skill ->
                            if (skill.isNotBlank()) {
                                skills.add(skill)
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("InviteSelectMemberVM", "Error parsing document ${document.id}", e)
                    }
                }

                // Update members list
                _members.value = membersList

                // Update filter options based on actual data
                _universityFilters.value = universities.sorted().map { university ->
                    FilterOption(
                        id = university.lowercase().replace(" ", "_"),
                        name = university,
                        type = FilterType.UNIVERSITY
                    )
                }

                _majorFilters.value = majors.sorted().map { major ->
                    FilterOption(
                        id = major.lowercase().replace(" ", "_"),
                        name = major,
                        type = FilterType.MAJOR
                    )
                }

                _skillFilters.value = skills.sorted().map { skill ->
                    FilterOption(
                        id = skill.lowercase().replace(" ", "_"),
                        name = skill,
                        type = FilterType.SKILL
                    )
                }

                Log.d("InviteSelectMemberVM", "Loaded ${membersList.size} members from Firestore")

            } catch (e: Exception) {
                _errorMessage.value = "Error loading members: ${e.message}"
                Log.e("InviteSelectMemberVM", "Error loading members from Firestore", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshMembers() {
        loadMembersFromFirestore()
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

    fun clearError() {
        _errorMessage.value = null
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