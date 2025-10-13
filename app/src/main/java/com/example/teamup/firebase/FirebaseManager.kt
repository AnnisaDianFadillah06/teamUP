package com.example.teamup.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseManager {
    val firestore: FirebaseFirestore by lazy { Firebase.firestore }

    // Konfigurasi collection path untuk memudahkan akses
    object Collections {
        const val USERS = "users"
        const val TEAMS = "teams"
        const val SKILLS = "skills"
    }
}