package com.ksebl.comkseblfaultapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ksebl.comkseblfaultapp.ui.screen.CitizenDashboardScreen
import com.ksebl.comkseblfaultapp.ui.screen.StaffDashboardScreen
import com.ksebl.comkseblfaultapp.ui.screen.LoginScreen
import com.ksebl.comkseblfaultapp.ui.screen.ReportFaultScreen
import com.ksebl.comkseblfaultapp.ui.screen.SplashScreen
import com.ksebl.comkseblfaultapp.ui.screen.FaultDetailScreen
import com.ksebl.comkseblfaultapp.ui.screen.HistoryScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
//        composable(Screen.Splash.route) {
//            SplashScreen(navController = navController)
//        }
         
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.StaffDashboard.route) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getString("tabIndex")?.toIntOrNull() ?: 0
            StaffDashboardScreen(navController = navController, initialTabIndex = tabIndex)
        }

        composable(Screen.CitizenDashboard.route) {
            CitizenDashboardScreen(navController = navController)
        }
        
        composable(Screen.ReportFault.route) {
            ReportFaultScreen(navController = navController)
        }

        composable(Screen.FaultDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            FaultDetailScreen(navController = navController, faultId = id)
        }

        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
    }
}

