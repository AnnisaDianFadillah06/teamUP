package com.example.teamup

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.teamup.common.theme.ESailTheme
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import java.security.Security

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

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
                    StartSail()
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
