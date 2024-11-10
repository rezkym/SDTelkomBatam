package com.example.sdtelkombatam

import androidx.navigation.NavController

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Attendance : Screen("attendance")

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                Home.route -> Home
                Attendance.route -> Attendance
                else -> Home
            }
        }
    }
}

// Extension functions untuk navigasi
fun NavController.navigateSingleTop(route: String) {
    this.navigate(route) {
        popUpTo(route) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.navigateWithPopUp(
    route: String,
    popUpRoute: String? = null,
    inclusive: Boolean = false,
    singleTop: Boolean = true
) {
    navigate(route) {
        popUpRoute?.let {
            popUpTo(it) { this.inclusive = inclusive }
        }
        launchSingleTop = singleTop
    }
}