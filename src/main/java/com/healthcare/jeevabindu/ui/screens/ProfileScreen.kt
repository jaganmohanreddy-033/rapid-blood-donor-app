package com.healthcare.jeevabindu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthcare.jeevabindu.model.Donor
import com.healthcare.jeevabindu.viewmodel.DonorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentDonor: Donor,
    donorViewModel: DonorViewModel,
    onBack: () -> Unit
) {
    var showMarkDonationDialog by remember { mutableStateOf(false) }
    val donor by donorViewModel.currentDonor.collectAsState()
    val displayDonor = donor ?: currentDonor

    LaunchedEffect(currentDonor.id) {
        donorViewModel.loadDonor(currentDonor.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = displayDonor.fullName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = displayDonor.phoneNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Blood Group Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Blood Group",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = displayDonor.bloodGroup.displayName,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Details Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileDetailRow(
                        icon = Icons.Default.Person,
                        label = "Age",
                        value = "${displayDonor.age} years"
                    )
                    Divider()
                    ProfileDetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        value = displayDonor.location
                    )
                    Divider()
                    ProfileDetailRow(
                        icon = Icons.Default.Favorite,
                        label = "Total Donations",
                        value = displayDonor.totalDonations.toString()
                    )
                }
            }

            // Eligibility Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (displayDonor.isEligibleToDonate()) 
                        MaterialTheme.colorScheme.secondaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (displayDonor.isEligibleToDonate()) 
                                Icons.Default.CheckCircle 
                            else 
                                Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (displayDonor.isEligibleToDonate()) 
                                MaterialTheme.colorScheme.secondary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (displayDonor.isEligibleToDonate()) 
                                    "Ready to Donate" 
                                else 
                                    "Not Eligible Yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (displayDonor.lastDonationDate != null) {
                                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                Text(
                                    text = "Last donation: ${dateFormat.format(displayDonor.lastDonationDate)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (!displayDonor.isEligibleToDonate()) {
                        val nextDate = displayDonor.getNextEligibleDate()
                        if (nextDate != null) {
                            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            Text(
                                text = "You can donate again on: ${dateFormat.format(nextDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "90-day waiting period for safe donation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Mark Donation Button
            Button(
                onClick = { showMarkDonationDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark Donation", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showMarkDonationDialog) {
        AlertDialog(
            onDismissRequest = { showMarkDonationDialog = false },
            title = { Text("Mark Donation") },
            text = { Text("Have you donated blood today? This will update your eligibility date.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        donorViewModel.markDonation(currentDonor.id) { success ->
                            if (success) {
                                showMarkDonationDialog = false
                            }
                        }
                    }
                ) {
                    Text("Yes, I Donated")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkDonationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
