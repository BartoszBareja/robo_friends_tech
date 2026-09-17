package com.example.robofriendstech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.robofriendstech.ui.DevicesScreen
import com.example.robofriendstech.ui.NotificationsScreen
import com.example.robofriendstech.ui.ScanScreen
import com.example.robofriendstech.ui.theme.RoboFriendsTechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoboFriendsTechTheme {
                RoboFriendApp()
            }
        }
    }
}

private sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Scan : Screen("scan", "Skanuj", Icons.Filled.QrCodeScanner)
    object Devices : Screen("devices", "Urządzenia", Icons.AutoMirrored.Filled.List)
    object Notifications : Screen("notifications", "Powiadomienia", Icons.Filled.Notifications)
}

private val bottomNavItems = listOf(Screen.Scan, Screen.Devices, Screen.Notifications)

@Composable
fun RoboFriendApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scan.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Scan.route) { ScanScreen() }
            composable(Screen.Devices.route) { DevicesScreen() }
            composable(Screen.Notifications.route) { NotificationsScreen() }
        }
    }
}
