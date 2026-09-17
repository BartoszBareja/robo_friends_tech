package com.example.robofriendstech.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionEventDao {
    @Query("SELECT * FROM connection_events ORDER BY id DESC")
    fun observeAll(): Flow<List<ConnectionEventEntity>>

    @Insert
    suspend fun insert(event: ConnectionEventEntity)
}
