package com.example.teamup.data.viewmodels

import androidx.lifecycle.ViewModel
import com.example.teamup.data.model.ProfileModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedMemberViewModel : ViewModel() {

    private val _selectedMembers = MutableStateFlow<List<ProfileModel>>(emptyList())
    val selectedMembers: StateFlow<List<ProfileModel>> = _selectedMembers.asStateFlow()

    fun setSelectedMembers(members: List<ProfileModel>) {
        _selectedMembers.value = members
    }

    fun clearSelectedMembers() {
        _selectedMembers.value = emptyList()
    }

    fun removeSelectedMember(memberId: String) {
        _selectedMembers.value = _selectedMembers.value.filter { it.id != memberId }
    }
}