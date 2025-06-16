package com.example.teamup.data.sources.remote.user

import android.net.Uri
import android.util.Log
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.model.user.toUserProfileData
import com.example.teamup.data.model.user.Activity
import com.example.teamup.data.model.user.toActivity
import com.example.teamup.data.model.user.Experience
import com.example.teamup.data.model.user.toExperience
import com.example.teamup.data.model.user.Education
import com.example.teamup.data.model.user.toEducation
import com.example.teamup.data.model.user.Skill
import com.example.teamup.data.model.user.toSkill
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserRemoteDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "UserRemoteSource"
        private const val USERS_COLLECTION = "users"
        private const val ACTIVITIES_SUBCOLLECTION = "activities"
        private const val EXPERIENCES_SUBCOLLECTION = "experiences"
        private const val EDUCATIONS_SUBCOLLECTION = "educations"
        private const val SKILLS_SUBCOLLECTION = "skills"
        private const val STORAGE_PATH = "profile_images"
        private const val MEDIA_STORAGE_PATH = "user_media"
    }

    // ================= USER PROFILE METHODS =================

    suspend fun getUserById(userId: String): UserProfileData? {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                doc.data?.toUserProfileData()?.copy(userId = doc.id)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID: ${e.message}", e)
            null
        }
    }

    suspend fun createUser(user: UserProfileData): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(user.userId)
                .set(user.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: UserProfileData): Result<Unit> {
        return try {
            val updates = user.toMap().toMutableMap().apply {
                put("updatedAt", System.currentTimeMillis())
            }

            firestore.collection(USERS_COLLECTION)
                .document(user.userId)
                .set(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserFields(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates.toMutableMap().apply {
                put("updatedAt", System.currentTimeMillis())
            }

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(updatesWithTimestamp)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user fields: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Get user data first to delete associated profile picture
            val user = getUserById(userId)

            // Delete profile picture if exists
            user?.profilePictureUrl?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    try {
                        storage.getReferenceFromUrl(imageUrl).delete().await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete profile picture: $imageUrl", e)
                    }
                }
            }

            // Delete user document
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): String? {
        return try {
            val imageRef = storage.reference
                .child("$STORAGE_PATH/$userId/${UUID.randomUUID()}.jpg")

            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload profile picture", e)
            null
        }
    }

    suspend fun deleteProfilePicture(imageUrl: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(imageUrl).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting profile picture: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun checkUsernameAvailability(username: String, currentUserId: String? = null): Boolean {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .await()

            val existingUsers = query.documents.filter { doc ->
                currentUserId == null || doc.id != currentUserId
            }

            existingUsers.isEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking username availability: ${e.message}", e)
            false
        }
    }

    suspend fun checkEmailExists(email: String, currentUserId: String? = null): Boolean {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", email)
                .get()
                .await()

            val existingUsers = query.documents.filter { doc ->
                currentUserId == null || doc.id != currentUserId
            }

            existingUsers.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking email existence: ${e.message}", e)
            false
        }
    }

    suspend fun getUserByUsername(username: String): UserProfileData? {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            val doc = query.documents.firstOrNull()
            doc?.data?.toUserProfileData()?.copy(userId = doc.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by username: ${e.message}", e)
            null
        }
    }

    suspend fun getUserByEmail(email: String): UserProfileData? {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            val doc = query.documents.firstOrNull()
            doc?.data?.toUserProfileData()?.copy(userId = doc.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by email: ${e.message}", e)
            null
        }
    }

    suspend fun searchUsers(query: String, limit: Int = 20): List<UserProfileData> {
        return try {
            if (query.isEmpty()) {
                // Return all active users if no query
                val allUsersQuery = firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("isActive", true)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()

                return allUsersQuery.documents.mapNotNull { doc ->
                    doc.data?.toUserProfileData()?.copy(userId = doc.id)
                }
            }

            // Search by full name and username
            val nameQuery = firestore.collection(USERS_COLLECTION)
                .whereGreaterThanOrEqualTo("fullName", query)
                .whereLessThanOrEqualTo("fullName", query + "\uf8ff")
                .whereEqualTo("isActive", true)
                .limit(limit.toLong())
                .get()
                .await()

            val usernameQuery = firestore.collection(USERS_COLLECTION)
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .whereEqualTo("isActive", true)
                .limit(limit.toLong())
                .get()
                .await()

            val allDocs = (nameQuery.documents + usernameQuery.documents)
                .distinctBy { it.id }
                .take(limit)

            allDocs.mapNotNull { doc ->
                doc.data?.toUserProfileData()?.copy(userId = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getUsersByRole(role: String, limit: Int = 50): List<UserProfileData> {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("role", role)
                .whereEqualTo("isActive", true)
                .limit(limit.toLong())
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                doc.data?.toUserProfileData()?.copy(userId = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting users by role: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getRecentUsers(limit: Int = 10): List<UserProfileData> {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                doc.data?.toUserProfileData()?.copy(userId = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent users: ${e.message}", e)
            emptyList()
        }
    }

    // ================= ACTIVITIES SUBCOLLECTION METHODS =================

    suspend fun getUserActivities(userId: String): List<Activity> {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                doc.data?.toActivity()?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user activities: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addUserActivity(userId: String, activity: Activity): Result<Unit> {
        return try {
            val docRef = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document()

            val activityWithId = activity.copy(id = docRef.id, userId = userId)

            docRef.set(activityWithId.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user activity: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserActivity(userId: String, activity: Activity): Result<Unit> {
        return try {
            val updates = activity.copy(
                userId = userId,
                updatedAt = System.currentTimeMillis()
            ).toMap()

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activity.id)
                .set(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user activity: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUserActivity(userId: String, activityId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user activity: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserActivities(userId: String): Boolean {
        return try {
            val activities = getUserActivities(userId)
            activities.forEach { activity ->
                deleteUserActivity(userId, activity.id)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all user activities: ${e.message}", e)
            false
        }
    }

    // ================= EXPERIENCES SUBCOLLECTION METHODS =================

    suspend fun getUserExperiences(userId: String): List<Experience> {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_SUBCOLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                doc.data?.toExperience()?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user experiences: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addUserExperience(userId: String, experience: Experience): Result<Unit> {
        return try {
            val docRef = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_SUBCOLLECTION)
                .document()

            val experienceWithId = experience.copy(id = docRef.id, userId = userId)

            docRef.set(experienceWithId.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user experience: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserExperience(userId: String, experience: Experience): Result<Unit> {
        return try {
            val updates = experience.copy(
                userId = userId,
                updatedAt = System.currentTimeMillis()
            ).toMap()

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_SUBCOLLECTION)
                .document(experience.id)
                .set(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user experience: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUserExperience(userId: String, experienceId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPERIENCES_SUBCOLLECTION)
                .document(experienceId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user experience: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserExperiences(userId: String): Boolean {
        return try {
            val experiences = getUserExperiences(userId)
            experiences.forEach { experience ->
                deleteUserExperience(userId, experience.id)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all user experiences: ${e.message}", e)
            false
        }
    }

    // ================= EDUCATIONS SUBCOLLECTION METHODS =================

    suspend fun getUserEducations(userId: String): List<Education> {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EDUCATIONS_SUBCOLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                doc.data?.toEducation()?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user educations: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addUserEducation(userId: String, education: Education): Result<Unit> {
        return try {
            val docRef = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EDUCATIONS_SUBCOLLECTION)
                .document()

            val educationWithId = education.copy(id = docRef.id, userId = userId)

            docRef.set(educationWithId.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user education: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserEducation(userId: String, education: Education): Result<Unit> {
        return try {
            val updates = education.copy(
                userId = userId,
                updatedAt = System.currentTimeMillis()
            ).toMap()

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EDUCATIONS_SUBCOLLECTION)
                .document(education.id)
                .set(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user education: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUserEducation(userId: String, educationId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EDUCATIONS_SUBCOLLECTION)
                .document(educationId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user education: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserEducations(userId: String): Boolean {
        return try {
            val educations = getUserEducations(userId)
            educations.forEach { education ->
                deleteUserEducation(userId, education.id)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all user educations: ${e.message}", e)
            false
        }
    }

    // ================= SKILLS SUBCOLLECTION METHODS =================

    suspend fun getUserSkills(userId: String): List<Skill> {
        return try {
            val query = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_SUBCOLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                doc.data?.toSkill()?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user skills: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addUserSkill(userId: String, skill: Skill): Result<Unit> {
        return try {
            val docRef = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_SUBCOLLECTION)
                .document()

            val skillWithId = skill.copy(id = docRef.id, userId = userId)

            docRef.set(skillWithId.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user skill: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserSkill(userId: String, skill: Skill): Result<Unit> {
        return try {
            val updates = skill.copy(
                userId = userId,
                updatedAt = System.currentTimeMillis()
            ).toMap()

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_SUBCOLLECTION)
                .document(skill.id)
                .set(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user skill: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUserSkill(userId: String, skillId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SKILLS_SUBCOLLECTION)
                .document(skillId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user skill: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserSkills(userId: String): Boolean {
        return try {
            val skills = getUserSkills(userId)
            skills.forEach { skill ->
                deleteUserSkill(userId, skill.id)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all user skills: ${e.message}", e)
            false
        }
    }

    // ================= MEDIA UPLOAD METHODS =================

    suspend fun uploadMediaFile(userId: String, mediaUri: Uri, fileName: String? = null): String? {
        return try {
            val finalFileName = fileName ?: "${UUID.randomUUID()}.jpg"
            val mediaRef = storage.reference
                .child("$MEDIA_STORAGE_PATH/$userId/$finalFileName")

            mediaRef.putFile(mediaUri).await()
            mediaRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload media file", e)
            null
        }
    }

    suspend fun deleteMediaFile(mediaUrl: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(mediaUrl).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting media file: ${e.message}", e)
            Result.failure(e)
        }
    }
}