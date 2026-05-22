package com.healthcare.jeevabindu.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class EmergencyAlert(
    @DocumentId
    val id: String = "",
    val hospitalName: String = "",
    val requiredBloodGroup: BloodGroup = BloodGroup.O_POSITIVE,
    val contactNumber: String = "",
    val location: String = "",
    val postedBy: String = "", // Donor ID
    val postedByName: String = "",
    val createdAt: Date = Date(),
    val isActive: Boolean = true,
    val respondingDonors: List<RespondingDonor> = emptyList()
)

data class RespondingDonor(
    val donorId: String = "",
    val donorName: String = "",
    val donorPhone: String = "",
    val respondedAt: Date = Date()
)
