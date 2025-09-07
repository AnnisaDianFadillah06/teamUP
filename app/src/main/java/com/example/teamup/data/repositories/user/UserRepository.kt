// data/repositories/UserRepository.kt
package com.example.teamup.data.repositories.user

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.model.user.Activity
import com.example.teamup.data.model.user.Experience
import com.example.teamup.data.model.user.Education
import com.example.teamup.data.model.user.Skill
import com.example.teamup.data.sources.remote.user.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class UserRepository {
    private val remoteDataSource = UserRemoteDataSource()

    companion object {
        private const val TAG = "UserRepository"
    }

    /**
     * Get user by ID
     */
    suspend fun getUser(userId: String): UserProfileData? {
        return try {
            remoteDataSource.getUserById(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user: $userId", e)
            null
        }
    }

    /**
     * Create user with optional profile image
     */
    suspend fun createUser(user: UserProfileData, imageUri: Uri?): Boolean {
        return try {
            // First create the user document
            val createResult = remoteDataSource.createUser(user)
            if (createResult.isFailure) {
                Log.e(TAG, "Failed to create user document", createResult.exceptionOrNull())
                return false
            }

            // Upload profile picture if provided
            if (imageUri != null) {
                val imageUrl = remoteDataSource.uploadProfilePicture(user.userId, imageUri)
                if (imageUrl != null) {
                    // Update user with profile picture URL
                    val updatedUser = user.copy(profilePictureUrl = imageUrl)
                    val updateResult = remoteDataSource.updateUser(updatedUser)
                    if (updateResult.isFailure) {
                        Log.e(TAG, "Failed to update user with profile picture", updateResult.exceptionOrNull())
                        return false
                    }
                }
            }

            Log.d(TAG, "User created successfully: ${user.userId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user: ${user.userId}", e)
            false
        }
    }

    /**
     * Update user with optional profile image
     */
    suspend fun updateUser(user: UserProfileData, imageUri: Uri?): Boolean {
        return try {
            var updatedUser = user

            // Upload new profile picture if provided
            if (imageUri != null) {
                val imageUrl = remoteDataSource.uploadProfilePicture(user.userId, imageUri)
                if (imageUrl != null) {
                    updatedUser = user.copy(profilePictureUrl = imageUrl)
                } else {
                    Log.e(TAG, "Failed to upload profile picture for user: ${user.userId}")
                    return false
                }
            }

            // Update user document
            val result = remoteDataSource.updateUser(updatedUser)
            if (result.isSuccess) {
                Log.d(TAG, "User updated successfully: ${user.userId}")
                true
            } else {
                Log.e(TAG, "Failed to update user: ${user.userId}", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${user.userId}", e)
            false
        }
    }

    /**
     * Delete user
     */
    suspend fun deleteUser(userId: String): Boolean {
        return try {
            val result = remoteDataSource.deleteUser(userId)
            if (result.isSuccess) {
                Log.d(TAG, "User deleted successfully: $userId")
                true
            } else {
                Log.e(TAG, "Failed to delete user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user: $userId", e)
            false
        }
    }

    /**
     * Search users by query
     */
    fun searchUsers(query: String): Flow<List<UserProfileData>> = flow {
        try {
            val users = remoteDataSource.searchUsers(query)
            emit(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users with query: $query", e)
            emit(emptyList())
        }
    }

    /**
     * Get active users
     */
    fun getActiveUsers(): Flow<List<UserProfileData>> = flow {
        try {
            val users = remoteDataSource.searchUsers("")
                .filter { it.isActive }
            emit(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active users", e)
            emit(emptyList())
        }
    }

    /**
     * Get users by role
     */
    fun getUsersByRole(role: String): Flow<List<UserProfileData>> = flow {
        try {
            val users = remoteDataSource.searchUsers("")
                .filter { it.role == role && it.isActive }
            emit(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting users by role: $role", e)
            emit(emptyList())
        }
    }

    /**
     * Get completed profiles
     */
    fun getCompletedProfiles(): Flow<List<UserProfileData>> = flow {
        try {
            val users = remoteDataSource.searchUsers("")
                .filter { it.profileCompleted && it.isActive }
            emit(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting completed profiles", e)
            emit(emptyList())
        }
    }

    /**
     * Check if username is available
     */
    suspend fun isUsernameAvailable(username: String, excludeUserId: String = ""): Boolean {
        return try {
            remoteDataSource.checkUsernameAvailability(username, excludeUserId.ifEmpty { null })
        } catch (e: Exception) {
            Log.e(TAG, "Error checking username availability: $username", e)
            false
        }
    }

    /**
     * Check if email exists
     */
    suspend fun checkEmailExists(email: String, excludeUserId: String = ""): Boolean {
        return try {
            remoteDataSource.checkEmailExists(email, excludeUserId.ifEmpty { null })
        } catch (e: Exception) {
            Log.e(TAG, "Error checking email existence: $email", e)
            false
        }
    }

    /**
     * Get user by username
     */
    suspend fun getUserByUsername(username: String): UserProfileData? {
        return try {
            remoteDataSource.getUserByUsername(username)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by username: $username", e)
            null
        }
    }

    /**
     * Get user by email
     */
    suspend fun getUserByEmail(email: String): UserProfileData? {
        return try {
            remoteDataSource.getUserByEmail(email)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by email: $email", e)
            null
        }
    }

    // ================= SUBCOLLECTION METHODS =================

    /**
     * Get user's activities
     */
    suspend fun getUserActivities(userId: String): List<Activity> {
        return try {
            remoteDataSource.getUserActivities(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user activities: $userId", e)
            emptyList()
        }
    }

    /**
     * Get user's experiences
     */
    suspend fun getUserExperiences(userId: String): List<Experience> {
        return try {
            remoteDataSource.getUserExperiences(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user experiences: $userId", e)
            emptyList()
        }
    }

    /**
     * Get user's educations
     */
    suspend fun getUserEducations(userId: String): List<Education> {
        return try {
            remoteDataSource.getUserEducations(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user educations: $userId", e)
            emptyList()
        }
    }

    /**
     * Get user's skills
     */
    suspend fun getUserSkills(userId: String): List<Skill> {
        return try {
            remoteDataSource.getUserSkills(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user skills: $userId", e)
            emptyList()
        }
    }

    /**
     * Add user activity
     */
    suspend fun addUserActivity(userId: String, activity: Activity): Boolean {
        return try {
            val result = remoteDataSource.addUserActivity(userId, activity)
            if (result.isSuccess) {
                Log.d(TAG, "Activity added successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to add activity for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding activity for user: $userId", e)
            false
        }
    }

    /**
     * Update user activity
     */
    suspend fun updateUserActivity(userId: String, activity: Activity): Boolean {
        return try {
            val result = remoteDataSource.updateUserActivity(userId, activity)
            if (result.isSuccess) {
                Log.d(TAG, "Activity updated successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to update activity for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating activity for user: $userId", e)
            false
        }
    }

    /**
     * Delete user activity
     */
    suspend fun deleteUserActivity(userId: String, activityId: String): Boolean {
        return try {
            val result = remoteDataSource.deleteUserActivity(userId, activityId)
            if (result.isSuccess) {
                Log.d(TAG, "Activity deleted successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to delete activity for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting activity for user: $userId", e)
            false
        }
    }

    /**
     * Add user experience
     */
    suspend fun addUserExperience(userId: String, experience: Experience): Boolean {
        return try {
            val result = remoteDataSource.addUserExperience(userId, experience)
            if (result.isSuccess) {
                Log.d(TAG, "Experience added successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to add experience for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding experience for user: $userId", e)
            false
        }
    }

    /**
     * Update user experience
     */
    suspend fun updateUserExperience(userId: String, experience: Experience): Boolean {
        return try {
            val result = remoteDataSource.updateUserExperience(userId, experience)
            if (result.isSuccess) {
                Log.d(TAG, "Experience updated successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to update experience for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating experience for user: $userId", e)
            false
        }
    }

    /**
     * Delete user experience
     */
    suspend fun deleteUserExperience(userId: String, experienceId: String): Boolean {
        return try {
            val result = remoteDataSource.deleteUserExperience(userId, experienceId)
            if (result.isSuccess) {
                Log.d(TAG, "Experience deleted successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to delete experience for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting experience for user: $userId", e)
            false
        }
    }

    /**
     * Add user education
     */
    suspend fun addUserEducation(userId: String, education: Education): Boolean {
        return try {
            val result = remoteDataSource.addUserEducation(userId, education)
            if (result.isSuccess) {
                Log.d(TAG, "Education added successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to add education for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding education for user: $userId", e)
            false
        }
    }

    /**
     * Update user education
     */
    suspend fun updateUserEducation(userId: String, education: Education): Boolean {
        return try {
            val result = remoteDataSource.updateUserEducation(userId, education)
            if (result.isSuccess) {
                Log.d(TAG, "Education updated successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to update education for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating education for user: $userId", e)
            false
        }
    }

    /**
     * Delete user education
     */
    suspend fun deleteUserEducation(userId: String, educationId: String): Boolean {
        return try {
            val result = remoteDataSource.deleteUserEducation(userId, educationId)
            if (result.isSuccess) {
                Log.d(TAG, "Education deleted successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to delete education for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting education for user: $userId", e)
            false
        }
    }

    /**
     * Add user skill
     */
    suspend fun addUserSkill(userId: String, skill: Skill): Boolean {
        return try {
            val result = remoteDataSource.addUserSkill(userId, skill)
            if (result.isSuccess) {
                Log.d(TAG, "Skill added successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to add skill for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding skill for user: $userId", e)
            false
        }
    }

    /**
     * Update user skill
     */
    suspend fun updateUserSkill(userId: String, skill: Skill): Boolean {
        return try {
            val result = remoteDataSource.updateUserSkill(userId, skill)
            if (result.isSuccess) {
                Log.d(TAG, "Skill updated successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to update skill for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating skill for user: $userId", e)
            false
        }
    }

    /**
     * Delete user skill
     */
    suspend fun deleteUserSkill(userId: String, skillId: String): Boolean {
        return try {
            val result = remoteDataSource.deleteUserSkill(userId, skillId)
            if (result.isSuccess) {
                Log.d(TAG, "Skill deleted successfully for user: $userId")
                true
            } else {
                Log.e(TAG, "Failed to delete skill for user: $userId", result.exceptionOrNull())
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting skill for user: $userId", e)
            false
        }
    }

    // ================= BATCH OPERATIONS =================

    /**
     * Get complete user profile with all subcollections
     */
    suspend fun getCompleteUserProfile(userId: String): CompleteUserProfile? {
        return try {
            val user = getUser(userId) ?: return null
            val activities = getUserActivities(userId)
            val experiences = getUserExperiences(userId)
            val educations = getUserEducations(userId)
            val skills = getUserSkills(userId)

            CompleteUserProfile(
                userData = user,
                activities = activities,
                experiences = experiences,
                educations = educations,
                skills = skills
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting complete user profile: $userId", e)
            null
        }
    }

    /**
     * Delete all user data including subcollections
     */
    suspend fun deleteCompleteUserProfile(userId: String): Boolean {
        return try {
            // Delete all subcollection data first
            val activitiesDeleted = remoteDataSource.deleteAllUserActivities(userId)
            val experiencesDeleted = remoteDataSource.deleteAllUserExperiences(userId)
            val educationsDeleted = remoteDataSource.deleteAllUserEducations(userId)
            val skillsDeleted = remoteDataSource.deleteAllUserSkills(userId)

            // Then delete the main user document
            val userDeleted = deleteUser(userId)

            val allDeleted = activitiesDeleted && experiencesDeleted &&
                    educationsDeleted && skillsDeleted && userDeleted

            if (allDeleted) {
                Log.d(TAG, "Complete user profile deleted successfully: $userId")
            } else {
                Log.w(TAG, "Some data may not have been deleted for user: $userId")
            }

            allDeleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting complete user profile: $userId", e)
            false
        }
    }
}

/**
 * Data class to hold complete user profile information
 */
data class CompleteUserProfile(
    val userData: UserProfileData,
    val activities: List<Activity>,
    val experiences: List<Experience>,
    val educations: List<Education>,
    val skills: List<Skill>
)