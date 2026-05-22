package com.healthcare.jeevabindu.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.healthcare.jeevabindu.model.BloodGroup
import com.healthcare.jeevabindu.model.Donor
import kotlinx.coroutines.tasks.await
import java.util.Date

class DonorRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val donorsCollection = firestore.collection("donors")

    suspend fun createDonor(donor: Donor): Result<String> {
        return try {
            val docRef = donorsCollection.add(donor).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDonorByPhone(phoneNumber: String): Donor? {
        return try {
            val snapshot = donorsCollection
                .whereEqualTo("phoneNumber", phoneNumber)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents[0].toObject(Donor::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDonorById(donorId: String): Donor? {
        return try {
            val snapshot = donorsCollection.document(donorId).get().await()
            snapshot.toObject(Donor::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateDonor(donorId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            donorsCollection.document(donorId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markDonation(donorId: String): Result<Unit> {
        return try {
            val donor = getDonorById(donorId)
            if (donor != null) {
                val updates = mapOf(
                    "lastDonationDate" to Date(),
                    "totalDonations" to (donor.totalDonations + 1),
                    "isAvailable" to false
                )
                updateDonor(donorId, updates)
            } else {
                Result.failure(Exception("Donor not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableDonorsByBloodGroup(
        bloodGroup: BloodGroup,
        location: String? = null
    ): List<Donor> {
        return try {
            var query: Query = donorsCollection
                .whereEqualTo("bloodGroup", bloodGroup)
                .whereEqualTo("isAvailable", true)
            
            if (location != null) {
                query = query.whereEqualTo("location", location)
            }
            
            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { it.toObject(Donor::class.java) }
                .filter { it.isEligibleToDonate() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllDonors(): List<Donor> {
        return try {
            val snapshot = donorsCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Donor::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateFCMToken(donorId: String, token: String): Result<Unit> {
        return updateDonor(donorId, mapOf("fcmToken" to token))
    }
}
