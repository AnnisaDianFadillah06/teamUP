package com.example.teamup

import android.app.Application
import com.example.teamup.di.Injection
import com.google.firebase.FirebaseApp

class TeamUpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Injection with application context
        Injection.initialize(this)

        // Pre-load notifications to get the count
        val notificationViewModel = Injection.provideNotificationViewModel()
        notificationViewModel.loadNotifications()
    }
}