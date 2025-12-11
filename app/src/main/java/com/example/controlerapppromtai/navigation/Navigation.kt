package com.example.controlerapppromtai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Devices : Screen("devices", "Devices", Icons.Default.Devices)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Login : Screen("login", "Login", Icons.Default.Person)
}

/**
 * List of bottom navigation items
 */
val bottomNavigationItems = listOf(
    Screen.Home,
    Screen.Devices,
    Screen.Settings,
    Screen.Profile
)
