package com.cosplayjournal.app.data.dao

import androidx.room.*
import com.cosplayjournal.app.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CosplayDao {
    // Cosplans
    @Query("SELECT * FROM cosplans")
    fun getAllCosplans(): Flow<List<Cosplan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosplan(cosplan: Cosplan): Long

    @Update
    suspend fun updateCosplan(cosplan: Cosplan)

    @Delete
    suspend fun deleteCosplan(cosplan: Cosplan)

    // Cosplays
    @Query("SELECT * FROM cosplays")
    fun getAllCosplays(): Flow<List<Cosplay>>

    @Query("SELECT * FROM cosplays WHERE cosplanId = :cosplanId")
    fun getCosplaysForCosplan(cosplanId: Long): Flow<List<Cosplay>>

    @Query("SELECT * FROM cosplays WHERE id = :id")
    suspend fun getCosplayById(id: Long): Cosplay?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosplay(cosplay: Cosplay): Long

    @Update
    suspend fun updateCosplay(cosplay: Cosplay)

    @Delete
    suspend fun deleteCosplay(cosplay: Cosplay)

    @Query("SELECT * FROM cosplays WHERE isFavorite = 1")
    fun getFavoriteCosplays(): Flow<List<Cosplay>>

    // Handmade Parts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHandmadePart(part: HandmadePart)

    @Update
    suspend fun updateHandmadePart(part: HandmadePart)

    @Delete
    suspend fun deleteHandmadePart(part: HandmadePart)

    @Query("SELECT * FROM handmade_parts WHERE cosplayId = :cosplayId")
    fun getHandmadeParts(cosplayId: Long): Flow<List<HandmadePart>>

    // Purchased Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchasedItem(item: PurchasedItem)

    @Update
    suspend fun updatePurchasedItem(item: PurchasedItem)

    @Delete
    suspend fun deletePurchasedItem(item: PurchasedItem)

    @Query("SELECT * FROM purchased_items WHERE cosplayId = :cosplayId")
    fun getPurchasedItems(cosplayId: Long): Flow<List<PurchasedItem>>

    // User Event Data
    @Query("SELECT * FROM user_event_data WHERE eventId = :eventId")
    suspend fun getUserEventData(eventId: String): UserEventData?

    @Query("SELECT * FROM user_event_data")
    fun getAllUserEventData(): Flow<List<UserEventData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserEventData(data: UserEventData)

    @Query("SELECT * FROM event_cosplan_selection WHERE eventId = :eventId")
    fun getCosplanSelectionsForEvent(eventId: String): Flow<List<EventCosplanSelection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosplanSelection(selection: EventCosplanSelection)

    @Query("DELETE FROM event_cosplan_selection WHERE eventId = :eventId AND cosplanId = :cosplanId AND day = :day")
    suspend fun deleteCosplanSelection(eventId: String, cosplanId: Long, day: String)
}
