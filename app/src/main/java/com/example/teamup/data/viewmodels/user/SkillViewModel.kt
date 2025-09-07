// ===== 4. data/viewmodels/user/SkillViewModel.kt - PERBAIKAN =====
package com.example.teamup.data.viewmodels.user

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.user.Skill
import com.example.teamup.data.repositories.user.SkillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SkillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SkillRepository()

    companion object {
        private const val TAG = "SkillViewModel"
    }

    private val _skills = MutableStateFlow<List<Skill>>(emptyList())
    val skills: StateFlow<List<Skill>> = _skills

    private val _currentSkill = MutableStateFlow<Skill?>(null)
    val currentSkill: StateFlow<Skill?> = _currentSkill

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // For skill suggestions/search
    private val _skillSuggestions = MutableStateFlow<List<String>>(emptyList())
    val skillSuggestions: StateFlow<List<String>> = _skillSuggestions

    fun loadSkills(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getUserSkills(userId)
                    .catch { e ->
                        _errorMessage.value = "Gagal memuat data skill: ${e.message}"
                        Log.e(TAG, "Error loading skills", e)
                        _skills.value = emptyList()
                    }
                    .collect { skillList ->
                        _skills.value = skillList.sortedBy { it.name }
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Alias untuk konsistensi dengan naming convention lain
    fun loadUserSkills(userId: String) = loadSkills(userId)

    fun loadSkill(userId: String, skillId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val skill = repository.getSkill(userId, skillId)
                _currentSkill.value = skill
                if (skill == null) {
                    _errorMessage.value = "Skill tidak ditemukan"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data skill: ${e.message}"
                Log.e(TAG, "Error loading skill", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSkill(
        userId: String,
        name: String,
        level: String,
        fromExperienceId: String? = null,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val skill = Skill(
                    id = "", // Will be generated in remote data source
                    userId = userId,
                    name = name,
                    level = level,
                    fromExperienceId = fromExperienceId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val success = repository.addSkill(userId, skill)
                if (success) {
                    loadSkills(userId) // Reload to get updated data
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menambahkan skill"
                    callback(false)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambahkan skill: ${e.message}"
                Log.e(TAG, "Error adding skill", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSkill(
        userId: String,
        skillId: String,
        name: String,
        level: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentSkill = _currentSkill.value
                if (currentSkill != null) {
                    val updatedSkill = currentSkill.copy(
                        name = name,
                        level = level,
                        updatedAt = System.currentTimeMillis()
                    )

                    val success = repository.updateSkill(userId, updatedSkill)
                    if (success) {
                        loadSkills(userId) // Reload to get updated data
                        _currentSkill.value = updatedSkill
                        callback(true)
                    } else {
                        _errorMessage.value = "Gagal mengupdate skill"
                        callback(false)
                    }
                } else {
                    _errorMessage.value = "Skill tidak ditemukan"
                    callback(false)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengupdate skill: ${e.message}"
                Log.e(TAG, "Error updating skill", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSkill(
        userId: String,
        skillId: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.deleteSkill(userId, skillId)
                if (success) {
                    loadSkills(userId) // Reload to get updated data
                    callback(true)
                } else {
                    _errorMessage.value = "Gagal menghapus skill"
                    callback(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus skill: ${e.message}"
                Log.e(TAG, "Error deleting skill", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSkillsFromExperience(
        userId: String,
        skills: List<String>,
        experienceId: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentSkills = _skills.value
                val skillsToAdd = mutableListOf<Skill>()

                for (skillName in skills) {
                    // Check if skill already exists
                    val existingSkill = currentSkills.find {
                        it.name.equals(skillName, ignoreCase = true)
                    }

                    if (existingSkill == null) {
                        val skill = Skill(
                            id = "", // Will be generated
                            userId = userId,
                            name = skillName,
                            level = "Beginner", // Default level
                            fromExperienceId = experienceId,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        skillsToAdd.add(skill)
                    }
                }

                if (skillsToAdd.isNotEmpty()) {
                    val success = repository.addMultipleSkills(userId, skillsToAdd)
                    if (success) {
                        loadSkills(userId) // Reload to get updated data
                        callback(true)
                    } else {
                        _errorMessage.value = "Gagal menambahkan beberapa skills"
                        callback(false)
                    }
                } else {
                    // All skills already exist
                    callback(true)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambahkan skills dari pengalaman: ${e.message}"
                Log.e(TAG, "Error adding skills from experience", e)
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchSkillSuggestions(query: String) {
        // Common skills for suggestions
        val commonSkills = listOf(
            "Leadership", "Communication", "Problem Solving", "Teamwork", "Project Management",
            "Data Analysis", "Programming", "Marketing", "Sales", "Customer Service",
            "JavaScript", "Python", "Java", "Kotlin", "React", "Node.js", "SQL",
            "Adobe Photoshop", "Adobe Illustrator", "Figma", "UI/UX Design",
            "Microsoft Excel", "Microsoft PowerPoint", "Google Analytics",
            "Social Media Marketing", "Content Writing", "SEO", "Digital Marketing"
        )

        if (query.length >= 2) {
            _skillSuggestions.value = commonSkills.filter {
                it.contains(query, ignoreCase = true)
            }.take(10)
        } else {
            _skillSuggestions.value = emptyList()
        }
    }

    fun getSkillsByLevel(userId: String, level: String, callback: (List<Skill>) -> Unit) {
        viewModelScope.launch {
            try {
                val filteredSkills = repository.getSkillsByLevel(userId, level)
                callback(filteredSkills)
            } catch (e: Exception) {
                Log.e(TAG, "Error filtering skills by level", e)
                callback(emptyList())
            }
        }
    }

    fun clearCurrentSkill() {
        _currentSkill.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuggestions() {
        _skillSuggestions.value = emptyList()
    }
}