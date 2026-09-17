package com.example.robofriendstech.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.robofriendstech.data.DeviceEntity
import com.example.robofriendstech.data.RoboFriendDatabase
import kotlinx.coroutines.launch

@Composable
fun DevicesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val database = remember { RoboFriendDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val devices by database.deviceDao().observeAll().collectAsState(initial = emptyList())

    if (devices.isEmpty()) {
        EmptyState(
            text = "Brak zapisanych urządzeń. Zeskanuj kod QR, aby dodać robota.",
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(devices, key = { it.id }) { device ->
            DeviceRow(
                device = device,
                onDelete = { scope.launch { database.deviceDao().delete(device) } },
            )
        }
    }
}

@Composable
private fun DeviceRow(device: DeviceEntity, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = device.robotName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${device.host}:${device.port}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Ostatnio połączono: ${device.lastConnectedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Usuń urządzenie")
            }
        }
    }
}
