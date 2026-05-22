package com.healthcare.jeevabindu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.healthcare.jeevabindu.model.BloodGroup
import com.healthcare.jeevabindu.model.Donor
import com.healthcare.jeevabindu.viewmodel.EmergencyViewModel
import com.healthcare.jeevabindu.viewmodel.PostAlertState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostEmergencyScreen(
    currentDonor: Donor,
    emergencyViewModel: EmergencyViewModel,
    onBack: () -> Unit
) {
    var hospitalName by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf(BloodGroup.O_POSITIVE) }
    var contactNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf(currentDonor.location) }
    var expanded by remember { mutableStateOf(false) }

    val postAlertState by emergencyViewModel.postAlertState.collectAsState()

    LaunchedEffect(postAlertState) {
        if (postAlertState is PostAlertState.Success) {
            kotlinx.coroutines.delay(1500)
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Emergency Alert") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.error,
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "⚠️ This will send urgent notifications to all eligible donors in the area",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = hospitalName,
                onValueChange = { hospitalName = it },
                label = { Text("Hospital Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedBloodGroup.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Required Blood Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    BloodGroup.values().forEach { bloodGroup ->
                        DropdownMenuItem(
                            text = { Text(bloodGroup.displayName) },
                            onClick = {
                                selectedBloodGroup = bloodGroup
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Contact Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (hospitalName.isNotBlank() && contactNumber.isNotBlank() && location.isNotBlank()) {
                        emergencyViewModel.postEmergencyAlert(
                            hospitalName = hospitalName,
                            requiredBloodGroup = selectedBloodGroup,
                            contactNumber = contactNumber,
                            location = location,
                            currentDonor = currentDonor
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = hospitalName.isNotBlank() && 
                         contactNumber.isNotBlank() && 
                         location.isNotBlank() &&
                         postAlertState !is PostAlertState.Loading
            ) {
                when (postAlertState) {
                    is PostAlertState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    is PostAlertState.Success -> {
                        Text("✓ Alert Posted!", style = MaterialTheme.typography.titleMedium)
                    }
                    else -> {
                        Text("Post Emergency Alert", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            if (postAlertState is PostAlertState.Error) {
                Text(
                    text = (postAlertState as PostAlertState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
