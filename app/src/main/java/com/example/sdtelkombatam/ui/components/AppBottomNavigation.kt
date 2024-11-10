package com.example.sdtelkombatam.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sdtelkombatam.Screen
import com.example.sdtelkombatam.navigateSingleTop

@Composable
fun AppBottomNavigation(
    navController: NavController
) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                if (currentRoute != Screen.Home.route) {
                    navController.navigateSingleTop(Screen.Home.route)
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.People, contentDescription = "Absensi") },
            label = { Text("Absensi") },
            selected = currentRoute == Screen.Attendance.route,
            onClick = {
                if (currentRoute != Screen.Attendance.route) {
                    navController.navigateSingleTop(Screen.Attendance.route)
                }
            }
        )
    }
}