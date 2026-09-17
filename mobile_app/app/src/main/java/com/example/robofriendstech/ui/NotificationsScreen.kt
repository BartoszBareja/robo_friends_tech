package com.example.robofriendstech.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.robofriendstech.data.ConnectionEventEntity
import com.example.robofriendstech.data.RoboFriendDatabase

@Composable
fun NotificationsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val database = remember { RoboFriendDatabase.getInstance(context) }
    val events by database.connectionEventDao().observeAll().collectAsState(initial = emptyList())

    if (events.isEmpty()) {
        EmptyState(
            text = "Brak powiadomień. Połącz się z robotem, aby zobaczyć historię połączeń.",
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(events, key = { it.id }) { event ->
            NotificationRow(event)
        }
    }
}

@Composable
private fun NotificationRow(event: ConnectionEventEntity, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Połączono z ${event.robotName}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Adres IP telefonu: ${event.clientIp}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = event.connectedAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
