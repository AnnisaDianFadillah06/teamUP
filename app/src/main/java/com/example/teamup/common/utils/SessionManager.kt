package com.example.teamup.common.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object SessionManager {
    private const val PREF_NAME = "teamup_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val TAG = "SessionManager"

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    /**
     * Enhanced method to check login status with better validation
     */
    fun isLoggedIn(context: Context): Boolean {
        return try {
            val currentUser = auth.currentUser
            val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val isManuallyLoggedIn = sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)

            // Debug logging
            Log.d(TAG, "Auth current user: ${currentUser?.uid}")
            Log.d(TAG, "Manual login flag: $isManuallyLoggedIn")

            // Only consider logged in if BOTH conditions are true
            val result = currentUser != null && isManuallyLoggedIn
            Log.d(TAG, "Final login status: $result")

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status", e)
            false
        }
    }

    /**
     * Mark user as logged in and update online status in Firestore
     */
    fun setLoggedIn(context: Context, isLoggedIn: Boolean, onComplete: ((Boolean) -> Unit)? = null) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()

        // Update online status in Firestore
        if (isLoggedIn) {
            updateUserOnlineStatus(true) { success ->
                // Ensure callback runs on Main thread
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete?.invoke(success)
                }
            }
        } else {
            updateUserOnlineStatus(false) { success ->
                // Ensure callback runs on Main thread
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete?.invoke(success)
                }
            }
        }
    }

    /**
     * Update user's online status in Firestore (made public for AppLifecycleHandler)
     */
    fun updateUserOnlineStatus(isOnline: Boolean, onComplete: ((Boolean) -> Unit)? = null) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            CoroutineScope(Dispatchers.Main).launch {
                onComplete?.invoke(false)
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDocRef = db.collection("users").document(currentUser.uid)

                val updates = hashMapOf<String, Any>(
                    "isOnline" to isOnline,
                    "lastActive" to FieldValue.serverTimestamp()
                )

                // If user is going online, also update login timestamp
                if (isOnline) {
                    updates["lastLogin"] = FieldValue.serverTimestamp()
                }

                userDocRef.update(updates).await()
                Log.d(TAG, "User online status updated successfully: $isOnline")

                // Switch to Main thread for callback
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error updating user online status", e)

                // Switch to Main thread for callback
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
            }
        }
    }

    /**
     * Update user's last active timestamp
     * Call this periodically while user is using the app
     */
    fun updateLastActive() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found for updating last active")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDocRef = db.collection("users").document(currentUser.uid)
                userDocRef.update("lastActive", FieldValue.serverTimestamp()).await()
                Log.d(TAG, "Last active timestamp updated")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating last active timestamp", e)
            }
        }
    }

    /**
     * Create or update user document in Firestore
     * Call this after successful registration or first login
     */
    fun createOrUpdateUserDocument(
        displayName: String? = null,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            CoroutineScope(Dispatchers.Main).launch {
                onComplete?.invoke(false)
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDocRef = db.collection("users").document(currentUser.uid)

                // Check if user document exists
                val userDoc = userDocRef.get().await()

                if (!userDoc.exists()) {
                    // Create new user document
                    val userData = hashMapOf(
                        "uid" to currentUser.uid,
                        "email" to currentUser.email,
                        "displayName" to (displayName ?: currentUser.displayName),
                        "photoURL" to currentUser.photoUrl?.toString(),
                        "isActive" to true,
                        "isOnline" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "lastLogin" to FieldValue.serverTimestamp(),
                        "lastActive" to FieldValue.serverTimestamp()
                    )

                    userDocRef.set(userData).await()
                    Log.d(TAG, "New user document created")
                } else {
                    // Update existing user document
                    val updates = hashMapOf<String, Any>(
                        "isOnline" to true,
                        "lastLogin" to FieldValue.serverTimestamp(),
                        "lastActive" to FieldValue.serverTimestamp()
                    )

                    // Update display name if provided
                    displayName?.let {
                        updates["displayName"] = it
                    }

                    userDocRef.update(updates).await()
                    Log.d(TAG, "Existing user document updated")
                }

                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error creating/updating user document", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
            }
        }
    }



    /**
     * Clear login session (for logout)
     * This completely clears all authentication data and sets user offline
     */
    fun clearSession(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        Log.d(TAG, "Starting session cleanup")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First set user as offline before clearing everything
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    try {
                        updateUserOnlineStatus(false) { success ->
                            Log.d(TAG, "User offline status updated: $success")
                        }
                        // Give a small delay to ensure the status update is processed
                        delay(500)
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not update offline status", e)
                    }
                }

                // Clear all shared preferences
                try {
                    val preferencesToClear = listOf(
                        "teamup_prefs",
                        "user_prefs",
                        "auth_prefs",
                        "biometric_prefs",
                        "user_session"
                    )

                    preferencesToClear.forEach { prefName ->
                        try {
                            val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                            val cleared = prefs.edit().clear().commit()
                            Log.d(TAG, "Preferences '$prefName' cleared: $cleared")
                        } catch (e: Exception) {
                            Log.w(TAG, "Could not clear preferences: $prefName", e)
                        }
                    }

                    // Double-check main preferences
                    val mainPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    mainPrefs.edit().clear().apply()

                    Log.d(TAG, "All preferences cleared successfully")

                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing preferences", e)
                }

                // Sign out from Firebase Auth - PENTING: Lakukan di Main thread
                withContext(Dispatchers.Main) {
                    try {
                        auth.signOut()
                        Log.d(TAG, "Firebase Auth signed out successfully")

                        // Verify sign out
                        val userAfterSignOut = auth.currentUser
                        if (userAfterSignOut == null) {
                            Log.d(TAG, "Sign out verified - no current user")
                        } else {
                            Log.w(TAG, "Sign out may not be complete - user still exists")
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error signing out from Firebase", e)
                    }

                    // Final callback
                    try {
                        onComplete?.invoke(true) // Changed: Pass boolean value instead of no parameter
                        Log.d(TAG, "Session cleanup completed successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in completion callback", e)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Critical error in clearSession", e)

                // Ensure callback is still called even on error
                withContext(Dispatchers.Main) {
                    try {
                        // Try basic auth signout as fallback
                        auth.signOut()
                    } catch (signOutError: Exception) {
                        Log.e(TAG, "Fallback signout failed", signOutError)
                    }

                    onComplete?.invoke(false) // Changed: Pass boolean value for error case
                }
            }
        }
    }

    /**
     * Get current user info including online status
     */
    fun getCurrentUserInfo(onResult: (UserInfo?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            CoroutineScope(Dispatchers.Main).launch {
                onResult(null)
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                if (userDoc.exists()) {
                    val userInfo = UserInfo(
                        uid = currentUser.uid,
                        email = currentUser.email ?: "",
                        displayName = userDoc.getString("displayName") ?: currentUser.displayName,
                        photoURL = userDoc.getString("photoURL"),
                        isActive = userDoc.getBoolean("isActive") ?: true,
                        isOnline = userDoc.getBoolean("isOnline") ?: false,
                        createdAt = userDoc.getTimestamp("createdAt"),
                        lastLogin = userDoc.getTimestamp("lastLogin"),
                        lastActive = userDoc.getTimestamp("lastActive")
                    )
                    withContext(Dispatchers.Main) {
                        onResult(userInfo)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user info", e)
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    /**
     * Get online users count
     */
    fun getOnlineUsersCount(onResult: (Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = db.collection("users")
                    .whereEqualTo("isOnline", true)
                    .get()
                    .await()

                withContext(Dispatchers.Main) {
                    onResult(snapshot.size())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting online users count", e)
                withContext(Dispatchers.Main) {
                    onResult(0)
                }
            }
        }
    }

    /**
     * Get recently active users (active within last 5 minutes)
     */
    fun getRecentlyActiveUsers(onResult: (List<UserInfo>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
                val timestamp = com.google.firebase.Timestamp(fiveMinutesAgo / 1000, 0)

                val snapshot = db.collection("users")
                    .whereGreaterThan("lastActive", timestamp)
                    .orderBy("lastActive", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()

                val users = snapshot.documents.mapNotNull { doc ->
                    try {
                        UserInfo(
                            uid = doc.getString("uid") ?: "",
                            email = doc.getString("email") ?: "",
                            displayName = doc.getString("displayName"),
                            photoURL = doc.getString("photoURL"),
                            isActive = doc.getBoolean("isActive") ?: true,
                            isOnline = doc.getBoolean("isOnline") ?: false,
                            createdAt = doc.getTimestamp("createdAt"),
                            lastLogin = doc.getTimestamp("lastLogin"),
                            lastActive = doc.getTimestamp("lastActive")
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user document", e)
                        null
                    }
                }

                withContext(Dispatchers.Main) {
                    onResult(users)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting recently active users", e)
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }
}

/**
 * Data class to represent user information
 */
data class UserInfo(
    val uid: String,
    val email: String,
    val displayName: String?,
    val photoURL: String?,
    val isActive: Boolean,
    val isOnline: Boolean,
    val createdAt: com.google.firebase.Timestamp?,
    val lastLogin: com.google.firebase.Timestamp?,
    val lastActive: com.google.firebase.Timestamp?
)