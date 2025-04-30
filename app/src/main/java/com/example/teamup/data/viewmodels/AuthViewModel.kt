//authviewmodel.kt
package com.example.teamup.data.viewmodels

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

// Contoh state untuk UI
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String? = null) : AuthUiState()
    data class Error(val error: String) : AuthUiState()
}

data class RegistrationData(
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String,
    val password: String
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    // Untuk menyimpan data registrasi sementara
    var registrationData: RegistrationData? = null
        private set

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun setRegistrationData(fullName: String, username: String, email: String, phone: String, password: String) {
        registrationData = RegistrationData(fullName, username, email, phone, password)
    }

    // Delegasi ke repository
    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            repository.isUsernameTaken(username)
        } catch (e: Exception) {
            false // Asumsikan username belum dipakai jika terjadi error
        }
    }

    suspend fun isPhoneTaken(phone: String): Boolean {
        return try {
            repository.isPhoneTaken(phone)
        } catch (e: Exception) {
            false // Asumsikan phone belum dipakai jika terjadi error
        }
    }

    suspend fun isEmailTaken(email: String): Boolean {
        return try {
            repository.isEmailTaken(email)
        } catch (e: Exception) {
            false // Asumsikan email belum dipakai jika terjadi error
        }
    }

    fun isPasswordStrong(password: String): Boolean {
        val regex =
            Regex("^(?=.*[0-9])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}\$")
        return regex.matches(password)
    }

    // 1. Registrasi email + kirim verifikasi
    fun registerWithEmail(
        data: RegistrationData,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repository.register(data.email, data.password)
                repository.sendEmailVerification()
                _uiState.value = AuthUiState.Success("Email verifikasi terkirim")
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Registrasi gagal")
                onResult(false, e.message)
            }
        }
    }

    // Fungsi baru untuk mengirim ulang email verifikasi
    fun resendEmailVerification(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repository.sendEmailVerification()
                _uiState.value = AuthUiState.Success("Email verifikasi terkirim ulang")
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Gagal mengirim ulang email verifikasi")
                onResult(false, e.message)
            }
        }
    }

    // 2. Kirim OTP phone, dengan callback
    fun sendPhoneOtp(
        phone: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)       // Format: +628123...
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 3. Verifikasi OTP
    fun verifyPhoneOtp(
        verificationId: String,
        code: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repository.verifyPhoneOtp(verificationId, code)
                _uiState.value = AuthUiState.Success("Phone verifikasi sukses")
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "OTP salah")
                onResult(false, e.message)
            }
        }
    }

    // Fungsi yang telah ada untuk memeriksa status verifikasi user
    fun reloadCurrentUser(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().currentUser?.reload()?.await()
                val verified = FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
                onResult(verified)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // 4. Simpan profil (dipanggil setelah salah satu verifikasi berhasil)
    fun saveProfile(
        data: RegistrationData,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repository.saveUserProfile(data)
                _uiState.value = AuthUiState.Success("Registrasi lengkap")
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Simpan profil gagal")
                onResult(false, e.message)
            }
        }
    }

    fun sendPasswordResetEmail(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repository.sendPasswordResetEmail(email)
                _uiState.value = AuthUiState.Success("Email reset terkirim")
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Gagal kirim reset email")
                onResult(false, e.message)
            }
        }
    }

    fun updatePassword(
        newPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repository.updatePassword(newPassword)
                _uiState.value = AuthUiState.Success("Password berhasil diupdate")
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Gagal update password")
                onResult(false, e.message)
            }
        }
    }
}