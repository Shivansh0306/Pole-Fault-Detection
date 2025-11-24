package com.ksebl.comkseblfaultapp.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object StaffDashboard : Screen("staff_dashboard/{tabIndex}")
    object CitizenDashboard : Screen("citizen_dashboard")
    object ReportFault : Screen("report_fault")
    object FaultDetail : Screen("fault/{id}")
    object History : Screen("history")
}

