package com.example.robofriendstech.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.robofriendstech.data.ConnectionEventEntity
import com.example.robofriendstech.data.DeviceEntity
import com.example.robofriendstech.data.RoboFriendDatabase
import com.example.robofriendstech.network.ConnectionInfo
import com.example.robofriendstech.network.fetchConnectionInfo
import com.example.robofriendstech.network.parseScannedUrl
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import kotlinx.coroutines.launch

sealed interface ConnectionState {
    object Idle : ConnectionState
    object Connecting : ConnectionState
    data class Connected(val info: ConnectionInfo) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { RoboFriendDatabase.getInstance(context) }
    var state by remember { mutableStateOf<ConnectionState>(ConnectionState.Idle) }

    fun startScan() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = GmsBarcodeScanning.getClient(context, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (rawValue == null) {
                    state = ConnectionState.Error("Nie udało się odczytać kodu QR.")
                    return@addOnSuccessListener
                }
                state = ConnectionState.Connecting
                scope.launch {
                    state = try {
                        val target = parseScannedUrl(rawValue)
                        val info = fetchConnectionInfo(target)
                        database.deviceDao().upsert(
                            DeviceEntity(
                                id = target.id,
                                robotName = info.robotName,
                                host = target.host,
                                port = target.port,
                                lastClientIp = info.clientIp,
                                lastConnectedAt = info.connectedAt,
                            )
                        )
                        database.connectionEventDao().insert(
                            ConnectionEventEntity(
                                deviceId = target.id,
                                robotName = info.robotName,
                                clientIp = info.clientIp,
                                connectedAt = info.connectedAt,
                            )
                        )
                        ConnectionState.Connected(info)
                    } catch (e: Exception) {
                        ConnectionState.Error(e.message ?: "Nie udało się połączyć z RoboFriend.")
                    }
                }
            }
            .addOnFailureListener { e ->
                state = ConnectionState.Error(e.message ?: "Skanowanie nie powiodło się.")
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "RoboFriend", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Zeskanuj kod QR wyświetlony na ekranie robota, aby się połączyć.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (val current = state) {
            is ConnectionState.Idle -> {}
            is ConnectionState.Connecting -> CircularProgressIndicator()
            is ConnectionState.Connected -> ConnectionInfoCard(current.info)
            is ConnectionState.Error -> Text(
                text = current.message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { startScan() }) {
            Text(text = if (state is ConnectionState.Connected) "Skanuj ponownie" else "Skanuj kod QR")
        }
    }
}

@Composable
private fun ConnectionInfoCard(info: ConnectionInfo, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Połączono",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Robot", value = info.robotName)
            InfoRow(label = "Adres IP robota", value = info.jetsonIp)
            InfoRow(label = "Adres IP telefonu", value = info.clientIp)
            InfoRow(label = "Połączono o", value = info.connectedAt)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
