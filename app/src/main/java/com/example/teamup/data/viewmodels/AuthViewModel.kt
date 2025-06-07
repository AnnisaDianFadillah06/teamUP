package com.example.teamup.data.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.repositories.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

// Enhanced AuthUiState - tambah Google Login states
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String? = null) : AuthUiState()
    data class Error(val error: String) : AuthUiState()
    // New Google states
    data class GoogleSignInSuccess(val user: FirebaseUser) : AuthUiState()
    data class LoginSuccess(val user: FirebaseUser) : AuthUiState()
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

    // Google Sign-In Properties
    private var googleSignInClient: GoogleSignInClient? = null
    private val auth = FirebaseAuth.getInstance()

    // Current user state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    // ================ EXISTING CODE - TETAP SAMA ================

    // Untuk menyimpan data registrasi sementara
    var registrationData: RegistrationData? = null
        private set

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // NEW GOOGLE LOGIN METHODS ================

    fun initGoogleSignIn(context: Context) {
        val webClientId = "1034394758478-e03r1jc6cqkfj60el39o4vh8pv01e37g.apps.googleusercontent.com"

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleSignInClient(): GoogleSignInClient? = googleSignInClient

    fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading

                if (account?.idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    val result = auth.signInWithCredential(credential).await()

                    result.user?.let { user ->
                        _currentUser.value = user
                        _uiState.value = AuthUiState.GoogleSignInSuccess(user)

                        // Save Google user profile
                        saveGoogleUserProfile(user)
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Google Sign-In failed")
                }

            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Google Sign-In failed")
            }
        }
    }

    private suspend fun saveGoogleUserProfile(user: FirebaseUser) {
        try {
            val userData = RegistrationData(
                fullName = user.displayName ?: "",
                username = user.email?.substringBefore("@") ?: "",
                email = user.email ?: "",
                phone = user.phoneNumber ?: "",
                password = "" // No password for Google users
            )
            repository.saveUserProfile(userData)
        } catch (e: Exception) {
            // Silent fail - user masih bisa login
        }
    }

    // ✅ Regular Email Login
    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.LoginSuccess(user)
                    onResult(true, null)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
                onResult(false, e.message)
            }
        }
    }

    // Enhanced Sign Out
    fun signOut() {
        auth.signOut()
        googleSignInClient?.signOut()
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }

    // Check login status
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Get current user info
    fun getCurrentUserInfo(): Triple<String?, String?, String?> {
        val user = auth.currentUser
        return Triple(user?.displayName, user?.email, user?.photoUrl?.toString())
    }

    // Clear error state
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    // ================ EXISTING METHODS - SEMUA TETAP SAMA ================

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
        val regex = Regex("^(?=.*[0-9])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}\$")
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