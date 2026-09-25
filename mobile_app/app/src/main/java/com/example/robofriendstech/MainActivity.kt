package com.example.robofriendstech

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.robofriendstech.ui.DevicesScreen
import com.example.robofriendstech.ui.NotificationsScreen
import com.example.robofriendstech.ui.ScanScreen
import com.example.robofriendstech.ui.theme.RoboFriendsTechTheme
import android.Manifest
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askNotificationPermission()
        registerWithFirebaseMessaging()
        setContent {
            RoboFriendsTechTheme {
                RoboFriendApp()
            }
        }
    }

    private fun registerWithFirebaseMessaging() {
        // Guarded: a Firebase/Play-Services hiccup here should never take the whole
        // activity down before it gets a chance to draw its UI.
        try {
            FirebaseMessaging.getInstance().register()
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful()) {
                        // Registration failed. Consider retrying the registration with exponential backoff.
                        Log.w(TAG, "Failed to register with Firebase Cloud Messaging", task.exception)
                    }
                    // Success! The Firebase Installation ID can be used to target messages to this app
                    // instance and will be delivered asynchronously to your `onRegistered()` callback.
                }
        } catch (e: Exception) {
            Log.w(TAG, "Could not start Firebase Cloud Messaging registration", e)
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
