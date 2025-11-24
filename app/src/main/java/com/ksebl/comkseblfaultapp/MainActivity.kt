package com.ksebl.comkseblfaultapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.ksebl.comkseblfaultapp.navigation.AppNavigation
import com.ksebl.comkseblfaultapp.ui.theme.ComkseblfaultappTheme
import timber.log.Timber

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    
    // Permission request launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Timber.d("All permissions granted, starting service")
            // Try to start the service now that we have permissions
            com.ksebl.comkseblfaultapp.service.FaultNotificationService.start(this)
        } else {
            Timber.w("Some permissions denied: $permissions")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            
            // Request permissions if needed
            requestRequiredPermissions()
            
            setContent {
                ComkseblfaultappApp()
            }
        } catch (e: Exception) {
            // Log the crash and try to show a basic error screen
            android.util.Log.e("MainActivity", "Critical error in onCreate", e)
            try {
                setContent {
                    ErrorScreen(error = e.message ?: "Unknown error occurred")
                }
            } catch (e2: Exception) {
                // If even the error screen fails, finish the activity
                android.util.Log.e("MainActivity", "Failed to show error screen", e2)
                finish()
            }
        }
    }
    
    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Check foreground service data sync permission for Android 14+
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            Timber.d("Requesting permissions: $permissionsToRequest")
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Timber.d("All permissions already granted")
            // Try to start the service immediately
            try {
                com.ksebl.comkseblfaultapp.service.FaultNotificationService.start(this)
            } catch (e: Exception) {
                Timber.e(e, "Failed to start service even with permissions")
            }
        }
    }
}

@Composable
fun ErrorScreen(error: String) {
    ComkseblfaultappTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "App Error",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    androidx.compose.material3.Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    androidx.compose.material3.Text(
                        text = "Please restart the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ComkseblfaultappApp() {
    val navController = rememberNavController()
    val view = LocalView.current
    val isDarkTheme = isSystemInDarkTheme()
    val openFaults = try {
        (view.context as ComponentActivity).intent?.getBooleanExtra("open_faults_tab", false) ?: false
    } catch (e: Exception) {
        android.util.Log.e("ComkseblfaultappApp", "Error getting intent extras", e)
        false
    }

    // Update the system UI colors to match the app theme
    SideEffect {
        try {
            val window = (view.context as ComponentActivity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
        } catch (e: Exception) {
            android.util.Log.e("ComkseblfaultappApp", "Error setting up system UI", e)
        }
    }

    ComkseblfaultappTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { paddingValues ->
                AppNavigation(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
                if (openFaults) {
                    // Open staff faults tab when requested via intent
                    LaunchedEffect(openFaults) {
                        try {
                            navController.navigate("staff_dashboard/1")
                            (view.context as ComponentActivity).intent?.removeExtra("open_faults_tab")
                        } catch (e: Exception) {
                            android.util.Log.e("ComkseblfaultappApp", "Error navigating to staff dashboard", e)
                        }
                    }
                }
            }
        }
    }
}