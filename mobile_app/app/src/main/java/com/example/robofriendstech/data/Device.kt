package com.example.robofriendstech.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val robotName: String,
    val host: String,
    val port: Int,
    val lastClientIp: String,
    val lastConnectedAt: String,
)
