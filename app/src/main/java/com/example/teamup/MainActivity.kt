package com.example.teamup

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.example.teamup.common.theme.ESailTheme
import com.example.teamup.common.utils.AppLifecycleHandler
import com.example.teamup.common.utils.SessionManager
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.presentation.StartSail
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth

class MainActivity : FragmentActivity() {
    private val competitionViewModel: CompetitionViewModel by viewModels {
        CompetitionViewModelFactory(Injection.provideCompetitionRepository(), Injection.provideCabangLombaRepository())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ App Check pakai Debug Provider
        Log.d("AppCheck", "Initializing AppCheck Debug provider")
        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
            //klo udah mau rilis pake ini
            // PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Initialize app lifecycle handler
        AppLifecycleHandler.initialize(application)

        // Verify login status saat app start
        verifyInitialLoginStatus()

        try {
            ProviderInstaller.installIfNeeded(this)
            Log.d("MainActivity", "Security provider installed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to install security provider: ${e.message}")
        }

        // Mencegah masalah dengan FloatingActionMode di MIUI (Xiaomi)
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting window flags", e)
        }
        setContent {
            ESailTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StartSail(competitionViewModel = competitionViewModel)
                }
            }
        }

    }

    private fun verifyInitialLoginStatus() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val sharedPrefs = getSharedPreferences("teamup_prefs", Context.MODE_PRIVATE)
            val isManuallyLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)

            Log.d("MainActivity", "Initial login check - Auth: ${currentUser != null}, Manual: $isManuallyLoggedIn")

            // Jika state tidak konsisten, clear everything
            if ((currentUser == null && isManuallyLoggedIn) ||
                (currentUser != null && !isManuallyLoggedIn)) {
                Log.w("MainActivity", "Inconsistent login state on app start, clearing session")
                SessionManager.clearSession(this) {
                    Log.d("MainActivity", "Session cleared due to inconsistent state")
                }
            } else {
                Log.d("MainActivity", "Login state is consistent")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error verifying login status", e)
            // Clear session as fallback
            SessionManager.clearSession(this) {
                Log.d("MainActivity", "Session cleared due to verification error")
            }
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}