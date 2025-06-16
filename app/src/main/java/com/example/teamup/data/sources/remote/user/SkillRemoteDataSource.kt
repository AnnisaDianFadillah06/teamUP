// ===== 3. data/sources/remote/user/SkillRemoteDataSource.kt =====
package com.example.teamup.data.sources.remote.user

import android.util.Log
import com.example.teamup.data.model.user.Skill
import com.example.teamup.data.model.user.toSkill
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SkillRemoteDataSource {
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "SkillRemoteSource"
        private const val USERS_COLLECTION = "users"
        private const val SKILLS_COLLECTION = "skills"
    }

    suspend fun getSkills(userId: String): List<Skill> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.toSkill()?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing skill document: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting skills: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getSkillById(userId: String, skillId: String): Skill? {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_COLLECTION)
                .document(skillId)
                .get()
                .await()

            if (doc.exists()) {
                doc.data?.toSkill()?.copy(id = doc.id)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting skill by ID: ${e.message}", e)
            null
        }
    }

    suspend fun saveSkill(userId: String, skill: Skill): Result<String> {
        return try {
            val skillId = skill.id.ifEmpty { UUID.randomUUID().toString() }
            val skillWithId = skill.copy(
                id = skillId,
                userId = userId,
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_COLLECTION)
                .document(skillId)
                .set(skillWithId.toMap())
                .await()

            Result.success(skillId)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving skill: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateSkill(userId: String, skill: Skill): Result<Unit> {
        return try {
            val updates = skill.copy(
                userId = userId,
                updatedAt = System.currentTimeMillis()
            ).toMap()

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_COLLECTION)
                .document(skill.id)
                .set(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating skill: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSkill(userId: String, skillId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_COLLECTION)
                .document(skillId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting skill: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getSkillsBySource(userId: String, fromExperienceId: String): List<Skill> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_COLLECTION)
                .whereEqualTo("fromExperienceId", fromExperienceId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.toSkill()?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing skill document: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting skills by source: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun deleteSkillsBySource(userId: String, fromExperienceId: String): Result<Unit> {
        return try {
            val skillsToDelete = getSkillsBySource(userId, fromExperienceId)

            skillsToDelete.forEach { skill ->
                firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(SKILLS_COLLECTION)
                    .document(skill.id)
                    .delete()
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting skills by source: ${e.message}", e)
            Result.failure(e)
        }
    }
}
