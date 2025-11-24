package com.ksebl.comkseblfaultapp.ui.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

// Backwards-compatible wrapper: delegate to StaffDashboardScreen.
// Kept so any references to DashboardScreen still work temporarily.
@Composable
fun DashboardScreen(navController: NavController, initialTabIndex: Int = 0) {
    StaffDashboardScreen(navController = navController, initialTabIndex = initialTabIndex)
}