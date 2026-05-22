package com.healthcare.jeevabindu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.jeevabindu.model.BloodGroup
import com.healthcare.jeevabindu.model.Donor
import com.healthcare.jeevabindu.repository.DonorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DonorViewModel : ViewModel() {
    private val donorRepository = DonorRepository()

    private val _donors = MutableStateFlow<List<Donor>>(emptyList())
    val donors: StateFlow<List<Donor>> = _donors.asStateFlow()

    private val _filteredDonors = MutableStateFlow<List<Donor>>(emptyList())
    val filteredDonors: StateFlow<List<Donor>> = _filteredDonors.asStateFlow()

    private val _currentDonor = MutableStateFlow<Donor?>(null)
    val currentDonor: StateFlow<Donor?> = _currentDonor.asStateFlow()

    fun loadAllDonors() {
        viewModelScope.launch {
            val donorList = donorRepository.getAllDonors()
            _donors.value = donorList
            _filteredDonors.value = donorList
        }
    }

    fun filterDonors(bloodGroup: BloodGroup?, location: String?) {
        val filtered = _donors.value.filter { donor ->
            val matchesBloodGroup = bloodGroup == null || donor.bloodGroup == bloodGroup
            val matchesLocation = location.isNullOrBlank() || 
                donor.location.contains(location, ignoreCase = true)
            val isEligible = donor.isEligibleToDonate()
            
            matchesBloodGroup && matchesLocation && isEligible
        }
        _filteredDonors.value = filtered
    }

    fun loadDonor(donorId: String) {
        viewModelScope.launch {
            val donor = donorRepository.getDonorById(donorId)
            _currentDonor.value = donor
        }
    }

    fun markDonation(donorId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = donorRepository.markDonation(donorId)
            result.onSuccess {
                // Reload donor data
                loadDonor(donorId)
                onComplete(true)
            }.onFailure {
                onComplete(false)
            }
        }
    }

    fun updateAvailability(donorId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            donorRepository.updateDonor(donorId, mapOf("isAvailable" to isAvailable))
            loadDonor(donorId)
        }
    }
}
