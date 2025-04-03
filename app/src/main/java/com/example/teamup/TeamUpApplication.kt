package com.example.teamup

import android.app.Application
import com.google.firebase.FirebaseApp

class TeamUpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
