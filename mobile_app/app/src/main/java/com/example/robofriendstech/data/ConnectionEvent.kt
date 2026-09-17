package com.example.robofriendstech.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_events")
data class ConnectionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: String,
    val robotName: String,
    val clientIp: String,
    val connectedAt: String,
)
