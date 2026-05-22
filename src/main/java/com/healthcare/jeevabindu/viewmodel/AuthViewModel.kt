package com.healthcare.jeevabindu.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.healthcare.jeevabindu.model.BloodGroup
import com.healthcare.jeevabindu.model.Donor
import com.healthcare.jeevabindu.repository.DonorRepository
import com.healthcare.jeevabindu.service.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {
    private val donorRepository = DonorRepository()
    private val notificationService = NotificationService()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentDonor = MutableStateFlow<Donor?>(null)
    val currentDonor: StateFlow<Donor?> = _currentDonor.asStateFlow()

    // Holds the verification ID returned by Firebase after sending OTP
    private var storedVerificationId: String? = null
    private var storedPhoneNumber: String? = null

    // Step 1: Send OTP via Firebase Phone Auth
    fun verifyPhoneNumber(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading

        val formattedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        storedPhoneNumber = formattedNumber

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // Auto-verification (e.g. on emulator or instant verify)
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.Error(e.message ?: "Verification failed")
            }

            // OTP sent — move to OTP entry screen
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                _authState.value = AuthState.OtpSent(formattedNumber)
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Step 2: Verify the OTP entered by the user
    fun verifyOtp(otp: String) {
        val verificationId = storedVerificationId
        if (verificationId == null) {
            _authState.value = AuthState.Error("Session expired. Please try again.")
            return
        }
        _authState.value = AuthState.Loading
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential)
    }

    // Step 3: Sign in with credential, then check Firestore for existing donor
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithCredential(credential).await()
                val phoneNumber = storedPhoneNumber ?: firebaseAuth.currentUser?.phoneNumber ?: ""
                val existingDonor = donorRepository.getDonorByPhone(phoneNumber)

                if (existingDonor != null) {
                    _currentDonor.value = existingDonor
                    _authState.value = AuthState.Authenticated(existingDonor)
                    updateFCMToken(existingDonor.id)
                } else {
                    _authState.value = AuthState.NeedsRegistration(phoneNumber)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign-in failed. Check OTP and try again.")
            }
        }
    }

    fun registerDonor(
        phoneNumber: String,
        fullName: String,
        bloodGroup: BloodGroup,
        age: Int,
        location: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val fcmToken = notificationService.getCurrentFCMToken() ?: ""

            val donor = Donor(
                phoneNumber = phoneNumber,
                fullName = fullName,
                bloodGroup = bloodGroup,
                age = age,
                location = location,
                fcmToken = fcmToken
            )

            val result = donorRepository.createDonor(donor)

            result.onSuccess { donorId ->
                val createdDonor = donor.copy(id = donorId)
                _currentDonor.value = createdDonor
                _authState.value = AuthState.Authenticated(createdDonor)
                notificationService.subscribeToBloodGroupTopic(bloodGroup)
                notificationService.subscribeToLocationTopic(location)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Registration failed")
            }
        }
    }

    private fun updateFCMToken(donorId: String) {
        viewModelScope.launch {
            val token = notificationService.getCurrentFCMToken()
            if (token != null) {
                donorRepository.updateFCMToken(donorId, token)
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _currentDonor.value = null
        _authState.value = AuthState.Initial
        storedVerificationId = null
        storedPhoneNumber = null
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class OtpSent(val phoneNumber: String) : AuthState()
    data class NeedsRegistration(val phoneNumber: String) : AuthState()
    data class Authenticated(val donor: Donor) : AuthState()
    data class Error(val message: String) : AuthState()
}
