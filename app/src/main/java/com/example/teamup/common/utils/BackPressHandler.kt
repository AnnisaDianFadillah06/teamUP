package com.example.teamup.common.utils

import android.app.Activity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.teamup.route.Routes
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BackPressHandler(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val lifecycleOwner = LocalLifecycleOwner.current

    // Menyimpan waktu terakhir tombol back ditekan
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    // State untuk menampilkan toast custom
    var showExitToast by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, backPressedDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            @OptIn(DelicateCoroutinesApi::class)
            override fun handleOnBackPressed() {
                val currentRoute = navController.currentBackStackEntry?.destination?.route

                // Jika user sudah di Dashboard atau screen utama, terapkan mekanisme double back press
                if (currentRoute == Routes.Dashboard.routes ||
                    currentRoute == Routes.Home.routes ||
                    currentRoute == Routes.Profile.routes ||
                    currentRoute == Routes.Competition.routes ||
                    currentRoute == Routes.Wishlist.routes ||
                    currentRoute == Routes.MyCourse.routes) {

                    val currentTime = System.currentTimeMillis()

                    // Jika jarak waktu antara 2 kali pencetan kurang dari 2 detik, keluar aplikasi
                    if (currentTime - lastBackPressTime < 2000) {
                        activity?.finish()
                    } else {
                        // Jika baru pertama kali tekan, tampilkan toast
                        lastBackPressTime = currentTime

                        // Show custom toast
                        showExitToast = true
                        GlobalScope.launch {
                            delay(2000)
                            showExitToast = false
                        }
                    }
                } else {
                    // Jika bukan di home screen, gunakan back navigation biasa
                    navController.popBackStack()
                }
            }
        }

        backPressedDispatcher?.addCallback(lifecycleOwner, callback)

        onDispose {
            callback.remove()
        }
    }

    // Custom toast UI
    if (showExitToast) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1A1A1A))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Tekan sekali lagi untuk keluar",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}