package com.example.teamup

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
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.presentation.StartSail
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import dagger.hilt.android.AndroidEntryPoint

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
        )

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
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}