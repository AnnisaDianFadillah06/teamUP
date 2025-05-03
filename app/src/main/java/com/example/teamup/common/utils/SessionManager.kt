package com.example.teamup.common.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object SessionManager {
    private const val PREF_NAME = "teamup_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    /**
     * Check if user is already logged in
     * We check both Firebase Auth and our manual flag
     */
    fun isLoggedIn(context: Context): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Only consider logged in if BOTH Firebase has a user AND our flag is true
        return currentUser != null && sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Mark user as logged in
     */
    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    /**
     * Clear login session (for logout)
     * This completely clears all authentication data
     */
    fun clearSession(context: Context) {
        // 1. Clear shared preferences completely
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        // 2. Also clear biometric-related prefs
        val prefs = context.getSharedPreferences("teamup_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // 3. Sign out from Firebase Auth
        FirebaseAuth.getInstance().signOut()
    }
}