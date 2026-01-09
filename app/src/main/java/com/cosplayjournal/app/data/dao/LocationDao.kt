package com.cosplayjournal.app.data.dao

import androidx.room.*
import com.cosplayjournal.app.data.entity.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations")
    fun getAllLocations(): Flow<List<Location>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)

    @Update
    suspend fun updateLocation(location: Location)

    @Delete
    suspend fun deleteLocation(location: Location)
}
