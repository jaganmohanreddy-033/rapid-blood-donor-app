package com.healthcare.jeevabindu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.healthcare.jeevabindu.model.Donor
import com.healthcare.jeevabindu.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentDonor: Donor,
    authViewModel: AuthViewModel,
    onNavigateToEmergencyAlerts: () -> Unit,
    onNavigateToDonorDirectory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPostEmergency: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jeeva-Bindu") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToPostEmergency,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Post Emergency") },
                containerColor = MaterialTheme.colorScheme.error
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
            // Welcome Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome, ${currentDonor.fullName}!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Blood Group: ${currentDonor.bloodGroup.displayName}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Location: ${currentDonor.location}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentDonor.isEligibleToDonate()) 
                        MaterialTheme.colorScheme.secondaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (currentDonor.isEligibleToDonate()) 
                                "✓ Ready to Donate" 
                            else 
                                "⏳ Not Eligible",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!currentDonor.isEligibleToDonate()) {
                            val nextDate = currentDonor.getNextEligibleDate()
                            if (nextDate != null) {
                                Text(
                                    text = "Eligible on: ${java.text.SimpleDateFormat("dd MMM yyyy").format(nextDate)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Text(
                        text = "Donations: ${currentDonor.totalDonations}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Navigation Cards
            Card(
                onClick = onNavigateToEmergencyAlerts,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Emergency Alerts",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "View urgent blood requests",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }

            Card(
                onClick = onNavigateToDonorDirectory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Donor Directory",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Find available donors",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }

            Card(
                onClick = onNavigateToProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "My Profile",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "View and update your details",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}
