package com.healthcare.jeevabindu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.healthcare.jeevabindu.ui.screens.*
import com.healthcare.jeevabindu.ui.theme.JeevaBinduTheme
import com.healthcare.jeevabindu.viewmodel.AuthState
import com.healthcare.jeevabindu.viewmodel.AuthViewModel
import com.healthcare.jeevabindu.viewmodel.DonorViewModel
import com.healthcare.jeevabindu.viewmodel.EmergencyViewModel

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            JeevaBinduTheme {
                JeevaBinduApp()
            }
        }
    }
}

@Composable
fun JeevaBinduApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val donorViewModel: DonorViewModel = viewModel()
    val emergencyViewModel: EmergencyViewModel = viewModel()
    
    val authState by authViewModel.authState.collectAsState()
    val currentDonor by authViewModel.currentDonor.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            when (val state = authState) {
                is AuthState.Initial,
                is AuthState.Loading,
                is AuthState.OtpSent,
                is AuthState.Error -> {
                    AuthScreen(
                        authViewModel = authViewModel,
                        onAuthenticated = {
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    )
                }
                is AuthState.NeedsRegistration -> {
                    RegistrationScreen(
                        phoneNumber = state.phoneNumber,
                        authViewModel = authViewModel,
                        onBack = { authViewModel.logout() },
                        onRegistered = {
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    )
                }
                is AuthState.Authenticated -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                }
            }
        }

        composable("home") {
            currentDonor?.let { donor ->
                HomeScreen(
                    currentDonor = donor,
                    authViewModel = authViewModel,
                    onNavigateToEmergencyAlerts = { navController.navigate("emergency_alerts") },
                    onNavigateToDonorDirectory = { navController.navigate("donor_directory") },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToPostEmergency = { navController.navigate("post_emergency") }
                )
            }
        }

        composable("emergency_alerts") {
            currentDonor?.let { donor ->
                EmergencyAlertsScreen(
                    currentDonor = donor,
                    emergencyViewModel = emergencyViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("donor_directory") {
            DonorDirectoryScreen(
                donorViewModel = donorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            currentDonor?.let { donor ->
                ProfileScreen(
                    currentDonor = donor,
                    donorViewModel = donorViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("post_emergency") {
            currentDonor?.let { donor ->
                PostEmergencyScreen(
                    currentDonor = donor,
                    emergencyViewModel = emergencyViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    // Handle logout
    LaunchedEffect(authState) {
        if (authState is AuthState.Initial) {
            navController.navigate("auth") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
