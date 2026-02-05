package com.cosplayjournal.app.data.repository

import com.cosplayjournal.app.data.dao.CosplayDao
import com.cosplayjournal.app.data.entity.*
import kotlinx.coroutines.flow.Flow

class CosplayRepository(private val cosplayDao: CosplayDao) {
    // Cosplans
    val allCosplans: Flow<List<Cosplan>> = cosplayDao.getAllCosplans()
    suspend fun insertCosplan(cosplan: Cosplan): Long = cosplayDao.insertCosplan(cosplan)
    suspend fun updateCosplan(cosplan: Cosplan) = cosplayDao.updateCosplan(cosplan)
    suspend fun deleteCosplan(cosplan: Cosplan) = cosplayDao.deleteCosplan(cosplan)

    // Cosplays
    val allCosplays: Flow<List<Cosplay>> = cosplayDao.getAllCosplays()
    fun getCosplaysForPlan(cosplanId: Long): Flow<List<Cosplay>> = cosplayDao.getCosplaysForCosplan(cosplanId)
    suspend fun getCosplayById(id: Long): Cosplay? = cosplayDao.getCosplayById(id)
    suspend fun insertCosplay(cosplay: Cosplay): Long = cosplayDao.insertCosplay(cosplay)
    suspend fun updateCosplay(cosplay: Cosplay) = cosplayDao.updateCosplay(cosplay)
    suspend fun deleteCosplay(cosplay: Cosplay) = cosplayDao.deleteCosplay(cosplay)
    val favoriteCosplays: Flow<List<Cosplay>> = cosplayDao.getFavoriteCosplays()

    // Handmade Parts
    fun getHandmadeParts(cosplayId: Long): Flow<List<HandmadePart>> = cosplayDao.getHandmadeParts(cosplayId)
    suspend fun insertHandmadePart(part: HandmadePart): Long = cosplayDao.insertHandmadePart(part)
    suspend fun updateHandmadePart(part: HandmadePart) = cosplayDao.updateHandmadePart(part)
    suspend fun deleteHandmadePart(part: HandmadePart) = cosplayDao.deleteHandmadePart(part)

    // Part Resources
    fun getResourcesForPart(partId: Long): Flow<List<PartResource>> = cosplayDao.getResourcesForPart(partId)
    suspend fun insertPartResource(resource: PartResource) = cosplayDao.insertPartResource(resource)
    suspend fun updatePartResource(resource: PartResource) = cosplayDao.updatePartResource(resource)
    suspend fun deletePartResource(resource: PartResource) = cosplayDao.deletePartResource(resource)

    // Purchased Items
    fun getPurchasedItems(cosplayId: Long): Flow<List<PurchasedItem>> = cosplayDao.getPurchasedItems(cosplayId)
    suspend fun insertPurchasedItem(item: PurchasedItem) = cosplayDao.insertPurchasedItem(item)
    suspend fun updatePurchasedItem(item: PurchasedItem) = cosplayDao.updatePurchasedItem(item)
    suspend fun deletePurchasedItem(item: PurchasedItem) = cosplayDao.deletePurchasedItem(item)

    // Photo Sessions
    fun getPhotoSessionsForCosplay(cosplayId: Long): Flow<List<PhotoSession>> = cosplayDao.getPhotoSessionsForCosplay(cosplayId)
    suspend fun insertPhotoSession(session: PhotoSession): Long = cosplayDao.insertPhotoSession(session)
    suspend fun insertCosplayPhotoSessionCrossRef(crossRef: CosplayPhotoSessionCrossRef) = cosplayDao.insertCosplayPhotoSessionCrossRef(crossRef)

    // User Event Data
    val allUserEventData: Flow<List<UserEventData>> = cosplayDao.getAllUserEventData()
    suspend fun getUserEventData(eventId: String): UserEventData? = cosplayDao.getUserEventData(eventId)
    suspend fun insertUserEventData(data: UserEventData) = cosplayDao.insertUserEventData(data)

    fun getCosplanSelectionsForEvent(eventId: String): Flow<List<EventCosplanSelection>> = 
        cosplayDao.getCosplanSelectionsForEvent(eventId)
    
    suspend fun insertCosplanSelection(selection: EventCosplanSelection) = 
        cosplayDao.insertCosplanSelection(selection)
    
    suspend fun deleteCosplanSelection(eventId: String, cosplanId: Long, day: String) = 
        cosplayDao.deleteCosplanSelection(eventId, cosplanId, day)
}
