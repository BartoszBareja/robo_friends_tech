package com.example.robofriendstech.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DeviceEntity::class, ConnectionEventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class RoboFriendDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun connectionEventDao(): ConnectionEventDao

    companion object {
        @Volatile
        private var instance: RoboFriendDatabase? = null

        fun getInstance(context: Context): RoboFriendDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RoboFriendDatabase::class.java,
                    "robofriend.db",
                ).build().also { instance = it }
            }
    }
}
