//authrepository.kt
package com.example.teamup.data.repositories

import android.util.Log
import androidx.activity.ComponentActivity
import com.example.teamup.data.viewmodels.RegistrationData
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.functions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // 1. Register email/password
    suspend fun register(email: String, password: String): AuthResult =
        auth.createUserWithEmailAndPassword(email, password).await()

    // 2. Kirim email verifikasi
    // 2. Kirim email verifikasi
    suspend fun sendEmailVerification() {
        val user = auth.currentUser ?: throw Exception("User not logged in")

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://teamupapp-b6cc1.web.app/emailverified.html")
            .setHandleCodeInApp(true) // Change to TRUE to handle in app
            .setAndroidPackageName("com.example.teamup", true, null)
            .setIOSBundleId("com.example.teamup")
            .build()

        try {
            user.sendEmailVerification(actionCodeSettings).await()
            Log.d("Email", "Email verifikasi dikirim dengan custom link.")
        } catch (e: Exception) {
            Log.e("Email", "Gagal kirim verifikasi", e)
        }

    }




    // 3. Kirim OTP SMS via Firebase PhoneAuth
    fun sendPhoneOtp(
        phoneNumber: String,
        activity: ComponentActivity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        try {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(120L, TimeUnit.SECONDS) // Tingkatkan timeout menjadi 120 detik
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error sending OTP: ${e.message}")
            throw e
        }
    }

    // 4. Verifikasi OTP dan sign‑in
    suspend fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String
    ): AuthResult {
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        return auth.signInWithCredential(credential).await()
    }

    // 5. Simpan profil ke Firestore
    suspend fun saveUserProfile(data: RegistrationData) {
        val uid = auth.currentUser?.uid
            ?: throw Exception("User not logged in")
        val userMap = mapOf(
            "fullName" to data.fullName,
            "username" to data.username,
            "email" to data.email,
            "phone" to data.phone
        )
        firestore.collection("users").document(uid).set(userMap).await()
    }

    // Tambahkan penanganan error dan timeout yang lebih panjang
    suspend fun isEmailTaken(email: String): Boolean {
        return try {
            val methods = auth.fetchSignInMethodsForEmail(email).await().signInMethods
            methods != null && methods.isNotEmpty()
        } catch (e: Exception) {
            // Log error dan return false untuk mencegah blocking
            Log.e("AuthRepository", "Error checking email: ${e.message}")
            false
        }
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val snapshot = firestore
                .collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            // Log error dan return false untuk mencegah blocking
            Log.e("AuthRepository", "Error checking username: ${e.message}")
            false
        }
    }

    suspend fun isPhoneTaken(phone: String): Boolean {
        return try {
            val snapshot = firestore
                .collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            // Log error dan return false untuk mencegah blocking
            Log.e("AuthRepository", "Error checking phone: ${e.message}")
            false
        }
    }

    // 9. Kirim reset password email
    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    // 10. Update password
    suspend fun updatePassword(newPassword: String) {
        auth.currentUser
            ?.updatePassword(newPassword)
            ?.await()
            ?: throw Exception("User not logged in")
    }
}
