package com.example.teamup.common.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object SessionManager {
    private const val PREF_NAME = "teamup_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    /**
     * Check if user is already logged in
     */
    fun isLoggedIn(context: Context): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return currentUser != null || sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
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
     */
    fun clearSession(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().remove(KEY_IS_LOGGED_IN).apply()
        FirebaseAuth.getInstance().signOut()
    }
}