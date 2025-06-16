package com.example.teamup.common.utils


import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.*

/**
 * Handles app lifecycle events to manage user online/offline status
 * and periodic last active updates
 */
class AppLifecycleHandler : Application.ActivityLifecycleCallbacks, LifecycleEventObserver {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    private var lastActiveUpdateJob: Job? = null
    private var offlineJob: Job? = null
    private val TAG = "AppLifecycleHandler"
    private var context: Context? = null

    // Update interval for last active timestamp (5 minutes)
    private val LAST_ACTIVE_UPDATE_INTERVAL = 5 * 60 * 1000L // 5 minutes

    // Delay before setting user offline when app is backgrounded
    private val OFFLINE_DELAY = 60 * 1000L // 1 minute

    companion object {
        private var instance: AppLifecycleHandler? = null

        fun initialize(application: Application) {
            if (instance == null) {
                instance = AppLifecycleHandler().apply {
                    context = application.applicationContext
                }
                application.registerActivityLifecycleCallbacks(instance)
                ProcessLifecycleOwner.get().lifecycle.addObserver(instance!!)
                Log.d("AppLifecycleHandler", "AppLifecycleHandler initialized")
            }
        }

        fun getInstance(): AppLifecycleHandler? = instance
    }

    // Application.ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d(TAG, "Activity created: ${activity.localClassName}")
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App is in foreground
            Log.d(TAG, "App entered foreground")
            onAppForegrounded()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d(TAG, "Activity resumed: ${activity.localClassName}")
        // Update last active when activity is resumed
        SessionManager.updateLastActive()
        startPeriodicLastActiveUpdates()
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d(TAG, "Activity paused: ${activity.localClassName}")
        // Don't stop updates here as user might just be switching between activities
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App is in background
            Log.d(TAG, "App entered background")
            onAppBackgrounded()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d(TAG, "Activity save instance state: ${activity.localClassName}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d(TAG, "Activity destroyed: ${activity.localClassName}")
    }

    // LifecycleEventObserver
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                // Process is stopping
                Log.d(TAG, "Process lifecycle - ON_STOP")
                onAppBackgrounded()
            }
            Lifecycle.Event.ON_START -> {
                // Process is starting
                Log.d(TAG, "Process lifecycle - ON_START")
                onAppForegrounded()
            }
            Lifecycle.Event.ON_DESTROY -> {
                Log.d(TAG, "Process lifecycle - ON_DESTROY")
                cleanup()
            }
            else -> {
                Log.d(TAG, "Process lifecycle event: $event")
            }
        }
    }

    private fun onAppForegrounded() {
        Log.d(TAG, "App foregrounded - setting user online")

        // Cancel any pending offline job
        offlineJob?.cancel()
        offlineJob = null

        context?.let { ctx ->
            // Only update if user is logged in
            if (SessionManager.isLoggedIn(ctx)) {
                // Set user as online when app comes to foreground
                SessionManager.setLoggedIn(ctx, true) { success ->
                    if (success) {
                        Log.d(TAG, "User status updated to online")
                    } else {
                        Log.e(TAG, "Failed to update user status to online")
                    }
                }

                // Update last active timestamp
                SessionManager.updateLastActive()
            } else {
                Log.d(TAG, "User not logged in, skipping online status update")
            }
        }

        // Start periodic updates
        startPeriodicLastActiveUpdates()
    }

    private fun onAppBackgrounded() {
        Log.d(TAG, "App backgrounded - scheduling offline status")

        // Stop periodic updates
        stopPeriodicLastActiveUpdates()

        context?.let { ctx ->
            // Only update if user is logged in
            if (SessionManager.isLoggedIn(ctx)) {
                // Update last active timestamp immediately
                SessionManager.updateLastActive()

                // Schedule offline status update after delay
                offlineJob = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        delay(OFFLINE_DELAY)

                        // Double-check if app is still in background
                        if (activityReferences == 0) {
                            Log.d(TAG, "Setting user offline after delay")
                            SessionManager.updateUserOnlineStatus(false) { success ->
                                if (success) {
                                    Log.d(TAG, "User status updated to offline")
                                } else {
                                    Log.e(TAG, "Failed to update user status to offline")
                                }
                            }
                        } else {
                            Log.d(TAG, "App returned to foreground, cancelling offline status")
                        }
                    } catch (e: CancellationException) {
                        Log.d(TAG, "Offline job cancelled")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in offline job", e)
                    }
                }
            } else {
                Log.d(TAG, "User not logged in, skipping offline status update")
            }
        }
    }

    private fun startPeriodicLastActiveUpdates() {
        // Cancel existing job if any
        stopPeriodicLastActiveUpdates()

        context?.let { ctx ->
            if (SessionManager.isLoggedIn(ctx)) {
                lastActiveUpdateJob = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        while (isActive) {
                            delay(LAST_ACTIVE_UPDATE_INTERVAL)

                            // Only update if user is still logged in and app is in foreground
                            if (activityReferences > 0 && SessionManager.isLoggedIn(ctx)) {
                                SessionManager.updateLastActive()
                                Log.d(TAG, "Periodic last active update completed")
                            } else {
                                Log.d(TAG, "Stopping periodic updates - user not logged in or app in background")
                                break
                            }
                        }
                    } catch (e: CancellationException) {
                        Log.d(TAG, "Periodic last active updates cancelled")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in periodic last active updates", e)
                    }
                }

                Log.d(TAG, "Started periodic last active updates")
            } else {
                Log.d(TAG, "User not logged in, not starting periodic updates")
            }
        }
    }

    private fun stopPeriodicLastActiveUpdates() {
        lastActiveUpdateJob?.cancel()
        lastActiveUpdateJob = null
        Log.d(TAG, "Stopped periodic last active updates")
    }

    /**
     * Force update user online status
     * Call this when user manually performs an action
     */
    fun forceUpdateOnlineStatus() {
        context?.let { ctx ->
            if (SessionManager.isLoggedIn(ctx)) {
                SessionManager.updateLastActive()
                Log.d(TAG, "Force updated online status")
            }
        }
    }

    /**
     * Cleanup resources
     */
    private fun cleanup() {
        Log.d(TAG, "Cleaning up AppLifecycleHandler")
        stopPeriodicLastActiveUpdates()
        offlineJob?.cancel()
        offlineJob = null

        context?.let { ctx ->
            if (SessionManager.isLoggedIn(ctx)) {
                // Set user offline when app is destroyed
                CoroutineScope(Dispatchers.IO).launch {
                    SessionManager.updateUserOnlineStatus(false) { success ->
                        Log.d(TAG, "User set offline on app destruction: $success")
                    }
                }
            }
        }
    }

    /**
     * Check if app is currently in foreground
     */
    fun isAppInForeground(): Boolean {
        return activityReferences > 0
    }

    /**
     * Get current activity reference count
     */
    fun getActivityReferenceCount(): Int {
        return activityReferences
    }
}