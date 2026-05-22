package com.healthcare.jeevabindu.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Donor(
    @DocumentId
    val id: String = "",
    val phoneNumber: String = "",
    val fullName: String = "",
    val bloodGroup: BloodGroup = BloodGroup.O_POSITIVE,
    val age: Int = 0,
    val location: String = "",
    val lastDonationDate: Date? = null,
    val totalDonations: Int = 0,
    val fcmToken: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Date = Date()
) {
    // Calculate if donor is eligible (90 days since last donation)
    fun isEligibleToDonate(): Boolean {
        if (lastDonationDate == null) return true
        
        val daysSinceLastDonation = (Date().time - lastDonationDate.time) / (1000 * 60 * 60 * 24)
        return daysSinceLastDonation >= 90
    }
    
    // Get next eligible date
    fun getNextEligibleDate(): Date? {
        if (lastDonationDate == null) return null
        
        val calendar = java.util.Calendar.getInstance()
        calendar.time = lastDonationDate
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 90)
        return calendar.time
    }
}

enum class BloodGroup(val displayName: String) {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");
    
    companion object {
        fun fromDisplayName(name: String): BloodGroup {
            return values().find { it.displayName == name } ?: O_POSITIVE
        }
    }
}
