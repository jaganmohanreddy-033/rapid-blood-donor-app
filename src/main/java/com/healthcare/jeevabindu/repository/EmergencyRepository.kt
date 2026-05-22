package com.healthcare.jeevabindu.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.healthcare.jeevabindu.model.EmergencyAlert
import com.healthcare.jeevabindu.model.RespondingDonor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class EmergencyRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val alertsCollection = firestore.collection("emergency_alerts")

    suspend fun createEmergencyAlert(alert: EmergencyAlert): Result<String> {
        return try {
            val docRef = alertsCollection.add(alert).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getActiveAlertsFlow(): Flow<List<EmergencyAlert>> = callbackFlow {
        val listener = alertsCollection
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val alerts = snapshot?.documents?.mapNotNull { 
                    it.toObject(EmergencyAlert::class.java) 
                } ?: emptyList()
                
                trySend(alerts)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun getActiveAlerts(): List<EmergencyAlert> {
        return try {
            val snapshot = alertsCollection
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.toObject(EmergencyAlert::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun respondToAlert(alertId: String, respondingDonor: RespondingDonor): Result<Unit> {
        return try {
            val alertDoc = alertsCollection.document(alertId).get().await()
            val alert = alertDoc.toObject(EmergencyAlert::class.java)
            
            if (alert != null) {
                val updatedResponders = alert.respondingDonors.toMutableList()
                
                // Check if donor already responded
                if (updatedResponders.none { it.donorId == respondingDonor.donorId }) {
                    updatedResponders.add(respondingDonor)
                    
                    alertsCollection.document(alertId)
                        .update("respondingDonors", updatedResponders)
                        .await()
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Alert not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deactivateAlert(alertId: String): Result<Unit> {
        return try {
            alertsCollection.document(alertId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
