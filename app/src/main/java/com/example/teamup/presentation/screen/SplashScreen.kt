package com.example.teamup.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.teamup.common.theme.DodgerBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinish: () -> Unit) {
    // Gunakan RawRes untuk resource raw
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.example.teamup.R.raw.animasi_logo)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = 1,
        speed = 1.0f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Teks "Team Up" berwarna putih
            Text(
                text = "Team Up",
                color = DodgerBlue,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Memanggil callback ketika animasi selesai
    if (progress == 1.0f) {
        LaunchedEffect(Unit) {
            delay(300) // Delay kecil agar animasi terlihat selesai
            onAnimationFinish()
        }
    }
}