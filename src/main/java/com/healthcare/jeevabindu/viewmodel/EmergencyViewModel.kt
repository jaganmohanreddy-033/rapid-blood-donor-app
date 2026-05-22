package com.healthcare.jeevabindu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.jeevabindu.model.BloodGroup
import com.healthcare.jeevabindu.model.Donor
import com.healthcare.jeevabindu.model.EmergencyAlert
import com.healthcare.jeevabindu.model.RespondingDonor
import com.healthcare.jeevabindu.repository.EmergencyRepository
import com.healthcare.jeevabindu.service.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class EmergencyViewModel : ViewModel() {
    private val emergencyRepository = EmergencyRepository()
    private val notificationService = NotificationService()

    private val _alerts = MutableStateFlow<List<EmergencyAlert>>(emptyList())
    val alerts: StateFlow<List<EmergencyAlert>> = _alerts.asStateFlow()

    private val _postAlertState = MutableStateFlow<PostAlertState>(PostAlertState.Initial)
    val postAlertState: StateFlow<PostAlertState> = _postAlertState.asStateFlow()

    init {
        observeAlerts()
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            emergencyRepository.getActiveAlertsFlow().collect { alertList ->
                _alerts.value = alertList
            }
        }
    }

    fun postEmergencyAlert(
        hospitalName: String,
        requiredBloodGroup: BloodGroup,
        contactNumber: String,
        location: String,
        currentDonor: Donor
    ) {
        viewModelScope.launch {
            _postAlertState.value = PostAlertState.Loading

            val alert = EmergencyAlert(
                hospitalName = hospitalName,
                requiredBloodGroup = requiredBloodGroup,
                contactNumber = contactNumber,
                location = location,
                postedBy = currentDonor.id,
                postedByName = currentDonor.fullName,
                createdAt = Date()
            )

            val result = emergencyRepository.createEmergencyAlert(alert)

            result.onSuccess { alertId ->
                val createdAlert = alert.copy(id = alertId)
                
                // Send notifications to eligible donors
                notificationService.sendEmergencyAlert(createdAlert)
                
                _postAlertState.value = PostAlertState.Success
                
                // Reset state after a delay
                kotlinx.coroutines.delay(2000)
                _postAlertState.value = PostAlertState.Initial
            }.onFailure { error ->
                _postAlertState.value = PostAlertState.Error(error.message ?: "Failed to post alert")
            }
        }
    }

    fun respondToAlert(alert: EmergencyAlert, currentDonor: Donor) {
        viewModelScope.launch {
            val respondingDonor = RespondingDonor(
                donorId = currentDonor.id,
                donorName = currentDonor.fullName,
                donorPhone = currentDonor.phoneNumber,
                respondedAt = Date()
            )

            emergencyRepository.respondToAlert(alert.id, respondingDonor)
        }
    }

    fun refreshAlerts() {
        viewModelScope.launch {
            val alertList = emergencyRepository.getActiveAlerts()
            _alerts.value = alertList
        }
    }
}

sealed class PostAlertState {
    object Initial : PostAlertState()
    object Loading : PostAlertState()
    object Success : PostAlertState()
    data class Error(val message: String) : PostAlertState()
}
