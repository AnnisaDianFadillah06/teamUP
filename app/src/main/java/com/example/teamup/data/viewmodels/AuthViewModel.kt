package com.example.teamup.data.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamup.data.model.RegistrationData
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

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    // Google Sign-In Properties
    private var googleSignInClient: GoogleSignInClient? = null
    private val auth = FirebaseAuth.getInstance()

    // Current user state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    // Untuk menyimpan data registrasi sementara
    var registrationData: RegistrationData? = null
        private set

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // ================ GOOGLE LOGIN METHODS WITH DEBUG ================

    // Ini kalo pake OAuth Client SHA-1 yang dicek di Google Cloud Console
    fun initGoogleSignIn(context: Context) {
        try {
            val webClientId = "1034394758478-mf4blrupgjdipk0ocdvr9ajkqm3n47k1.apps.googleusercontent.com"

            Log.d(TAG, "🔧 DEBUG: Initializing Google Sign-In")
            Log.d(TAG, "🔧 DEBUG: Using Web Client ID: $webClientId")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
            Log.d(TAG, "🔧 DEBUG: Google Sign-In client initialized successfully")

        } catch (e: Exception) {
            Log.e(TAG, "🔧 ERROR: Failed to initialize Google Sign-In", e)
        }
    }

    fun getGoogleSignInClient(): GoogleSignInClient? {
        Log.d(TAG, "🔧 DEBUG: Getting Google Sign-In client: ${googleSignInClient != null}")
        return googleSignInClient
    }

    fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔧 DEBUG: Starting Google Sign-In process")
                Log.d(TAG, "🔧 DEBUG: Account received: ${account != null}")

                _uiState.value = AuthUiState.Loading

                if (account?.idToken != null) {
                    Log.d(TAG, "🔧 DEBUG: Got idToken: ${account.idToken?.take(20)}...")
                    Log.d(TAG, "🔧 DEBUG: Account email: ${account.email}")
                    Log.d(TAG, "🔧 DEBUG: Account name: ${account.displayName}")

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    Log.d(TAG, "🔧 DEBUG: Created Firebase credential")

                    val result = auth.signInWithCredential(credential).await()
                    Log.d(TAG, "🔧 DEBUG: Firebase auth successful")

                    result.user?.let { user ->
                        Log.d(TAG, "🔧 DEBUG: Got Firebase user: ${user.email}")
                        Log.d(TAG, "🔧 DEBUG: User UID: ${user.uid}")
                        Log.d(TAG, "🔧 DEBUG: User verified: ${user.isEmailVerified}")

                        _currentUser.value = user
                        _uiState.value = AuthUiState.GoogleSignInSuccess(user)

                        // Save Google user profile
                        saveGoogleUserProfile(user)

                    } ?: run {
                        Log.e(TAG, "🔧 ERROR: Firebase user is null")
                        _uiState.value = AuthUiState.Error("Firebase authentication failed")
                    }

                } else {
                    Log.e(TAG, "🔧 ERROR: idToken is null")
                    Log.e(TAG, "🔧 ERROR: Account details - Email: ${account?.email}, ID: ${account?.id}")
                    _uiState.value = AuthUiState.Error("Google Sign-In failed - No ID token received")
                }

            } catch (e: Exception) {
                Log.e(TAG, "🔧 ERROR: Exception in handleGoogleSignInResult", e)
                Log.e(TAG, "🔧 ERROR: Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "🔧 ERROR: Exception message: ${e.message}")
                _uiState.value = AuthUiState.Error(e.message ?: "Google Sign-In failed")
            }
        }
    }

    private suspend fun saveGoogleUserProfile(user: FirebaseUser) {
        try {
            Log.d(TAG, "🔧 DEBUG: Saving Google user profile")
            val userData = RegistrationData(
                fullName = user.displayName ?: "",
                username = user.email?.substringBefore("@") ?: "",
                email = user.email ?: "",
                phone = user.phoneNumber ?: "",
                password = "" // No password for Google users
            )

            Log.d(TAG, "🔧 DEBUG: User data to save: $userData")
            repository.saveUserProfile(userData)
            Log.d(TAG, "🔧 DEBUG: Google user profile saved successfully")

        } catch (e: Exception) {
            Log.w(TAG, "🔧 WARNING: Failed to save Google user profile", e)
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
            try {
                Log.d(TAG, "🔧 DEBUG: Starting email login for: $email")
                _uiState.value = AuthUiState.Loading

                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    Log.d(TAG, "🔧 DEBUG: Email login successful for: ${user.email}")
                    _currentUser.value = user
                    _uiState.value = AuthUiState.LoginSuccess(user)
                    onResult(true, null)
                } ?: run {
                    Log.e(TAG, "🔧 ERROR: Email login failed - user is null")
                    onResult(false, "Login failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "🔧 ERROR: Email login exception", e)
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
                onResult(false, e.message)
            }
        }
    }

    // Enhanced Sign Out
    fun signOut() {
        Log.d(TAG, "🔧 DEBUG: Signing out user")
        auth.signOut()
        googleSignInClient?.signOut()
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
        Log.d(TAG, "🔧 DEBUG: Sign out completed")
    }

    // Check login status
    fun isUserLoggedIn(): Boolean {
        val isLoggedIn = auth.currentUser != null
        Log.d(TAG, "🔧 DEBUG: User logged in status: $isLoggedIn")
        return isLoggedIn
    }

    // Get current user info
    fun getCurrentUserInfo(): Triple<String?, String?, String?> {
        val user = auth.currentUser
        val info = Triple(user?.displayName, user?.email, user?.photoUrl?.toString())
        Log.d(TAG, "🔧 DEBUG: Current user info: $info")
        return info
    }

    // Clear error state
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            Log.d(TAG, "🔧 DEBUG: Clearing error state")
            _uiState.value = AuthUiState.Idle
        }
    }

    // ================ EXISTING METHODS - SEMUA TETAP SAMA ================

    fun setRegistrationData(fullName: String, username: String, email: String, phone: String, password: String) {
        registrationData = RegistrationData(
            fullName = fullName,
            username = username,
            email = email,
            phone = phone,
            password = password
        )
    }

    // Delegasi ke repository
    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            repository.isUsernameTaken(username)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isPhoneTaken(phone: String): Boolean {
        return try {
            repository.isPhoneTaken(phone)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isEmailTaken(email: String): Boolean {
        return try {
            repository.isEmailTaken(email)
        } catch (e: Exception) {
            false
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
            .setPhoneNumber(phone)
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

    // 4. Simpan profil
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