package com.example.teamup.common.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val activity: FragmentActivity) {

    /**
     * Memeriksa apakah autentikasi biometrik tersedia dan dapat digunakan
     * @return true jika biometrik dapat digunakan
     */
    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(activity)

        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false
        }
    }

    /**
     * Memeriksa jika perangkat tidak memiliki sidik jari terdaftar
     * @return true jika tidak ada sidik jari terdaftar
     */
    fun hasNoFingerprintsEnrolled(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    /**
     * Memeriksa jika perangkat tidak memiliki sensor biometrik
     * @return true jika tidak ada sensor biometrik
     */
    fun hasNoBiometricSensor(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    }
}