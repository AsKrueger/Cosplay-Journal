package com.cosplayjournal.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cosplayjournal.app.data.entity.UserEventData
import kotlinx.coroutines.flow.Flow

@Dao
interface UserEventDao {
    @Query("SELECT * FROM user_event_data")
    fun getAll(): Flow<List<UserEventData>>

    @Query("SELECT * FROM user_event_data WHERE eventId = :eventId")
    suspend fun get(eventId: String): UserEventData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: UserEventData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<UserEventData>)

    @Query("DELETE FROM user_event_data")
    suspend fun clear()
}
