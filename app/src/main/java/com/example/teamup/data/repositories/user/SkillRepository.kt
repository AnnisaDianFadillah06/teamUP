// ===== 2. data/repositories/user/SkillRepository.kt =====
package com.example.teamup.data.repositories.user

import android.util.Log
import com.example.teamup.data.model.user.Skill
import com.example.teamup.data.sources.remote.user.SkillRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SkillRepository {
    private val remoteDataSource = SkillRemoteDataSource()

    companion object {
        private const val TAG = "SkillRepository"
    }

    /**
     * Add a new skill for a user
     */
    suspend fun addSkill(userId: String, skill: Skill): Boolean {
        return try {
            val result = remoteDataSource.saveSkill(userId, skill)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error adding skill for user: $userId", e)
            false
        }
    }

    /**
     * Update an existing skill
     */
    suspend fun updateSkill(userId: String, skill: Skill): Boolean {
        return try {
            if (skill.id.isEmpty()) {
                Log.e(TAG, "Skill ID cannot be empty")
                return false
            }

            val result = remoteDataSource.updateSkill(userId, skill)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error updating skill: ${skill.id}", e)
            false
        }
    }

    /**
     * Delete a skill
     */
    suspend fun deleteSkill(userId: String, skillId: String): Boolean {
        return try {
            val result = remoteDataSource.deleteSkill(userId, skillId)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting skill: $skillId", e)
            false
        }
    }

    /**
     * Get all skills for a user as Flow
     */
    suspend fun getUserSkills(userId: String): Flow<List<Skill>> = flow {
        try {
            val skills = remoteDataSource.getSkills(userId)
            emit(skills)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user skills: $userId", e)
            emit(emptyList())
        }
    }

    /**
     * Get a specific skill by ID
     */
    suspend fun getSkill(userId: String, skillId: String): Skill? {
        return try {
            remoteDataSource.getSkillById(userId, skillId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting skill: $skillId", e)
            null
        }
    }

    /**
     * Get skills by level
     */
    suspend fun getSkillsByLevel(userId: String, level: String): List<Skill> {
        return try {
            val allSkills = remoteDataSource.getSkills(userId)
            allSkills.filter { it.level == level }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting skills by level: $level", e)
            emptyList()
        }
    }

    /**
     * Get skills by source (experience)
     */
    suspend fun getSkillsBySource(userId: String, fromExperienceId: String): List<Skill> {
        return try {
            remoteDataSource.getSkillsBySource(userId, fromExperienceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting skills by source: $fromExperienceId", e)
            emptyList()
        }
    }

    /**
     * Delete skills by source (experience)
     */
    suspend fun deleteSkillsBySource(userId: String, fromExperienceId: String): Boolean {
        return try {
            val result = remoteDataSource.deleteSkillsBySource(userId, fromExperienceId)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting skills by source: $fromExperienceId", e)
            false
        }
    }

//    /**
//     * Search skills by name
//     */
//    suspend fun searchSkills(userId: String, query: String): List<Skill> {
//        return try {
//            remoteDataSource.searchSkills(userId, query)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error searching skills", e)
//            emptyList()
//        }
//    }

    /**
     * Add multiple skills at once
     */
    suspend fun addMultipleSkills(userId: String, skills: List<Skill>): Boolean {
        return try {
            var allSuccess = true
            skills.forEach { skill ->
                val result = remoteDataSource.saveSkill(userId, skill)
                if (!result.isSuccess) {
                    allSuccess = false
                }
            }
            allSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error adding multiple skills for user: $userId", e)
            false
        }
    }
}
