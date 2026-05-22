package com.healthcare.jeevabindu.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.healthcare.jeevabindu.model.BloodGroup
import com.healthcare.jeevabindu.model.Donor
import com.healthcare.jeevabindu.model.EmergencyAlert
import com.healthcare.jeevabindu.repository.DonorRepository
import kotlinx.coroutines.tasks.await

class NotificationService {
    private val firestore = FirebaseFirestore.getInstance()
    private val donorRepository = DonorRepository()

    suspend fun sendEmergencyAlert(alert: EmergencyAlert) {
        try {
            // Get all eligible donors for the required blood group in the location
            val donors = donorRepository.getAvailableDonorsByBloodGroup(
                alert.requiredBloodGroup,
                alert.location
            )

            // In a real app, you would send FCM messages to all donors
            // For simulation, we'll just log it
            donors.forEach { donor ->
                if (donor.fcmToken.isNotEmpty()) {
                    // Send FCM notification
                    // This would typically be done from a backend server
                    sendFCMNotification(
                        token = donor.fcmToken,
                        title = "🚨 URGENT: Blood Needed",
                        body = "${alert.requiredBloodGroup.displayName} needed at ${alert.hospitalName}",
                        data = mapOf(
                            "alertId" to alert.id,
                            "bloodGroup" to alert.requiredBloodGroup.name,
                            "hospital" to alert.hospitalName
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun sendFCMNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        // In a production app, this would be done via Firebase Cloud Functions or a backend server
        // For this educational app, we're simulating the notification
        // The FCMService will handle incoming notifications
        
        // Note: Direct FCM sending from Android client is not recommended for production
        // This is just for demonstration purposes
        println("Simulated FCM Notification sent to $token: $title - $body")
    }

    suspend fun subscribeToBloodGroupTopic(bloodGroup: BloodGroup) {
        try {
            FirebaseMessaging.getInstance()
                .subscribeToTopic("blood_${bloodGroup.name}")
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun subscribeToLocationTopic(location: String) {
        try {
            val sanitizedLocation = location.replace(" ", "_").lowercase()
            FirebaseMessaging.getInstance()
                .subscribeToTopic("location_$sanitizedLocation")
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getCurrentFCMToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            null
        }
    }
}
