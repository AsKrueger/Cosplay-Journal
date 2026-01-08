package com.cosplayjournal.app.data.repository

import com.cosplayjournal.app.data.dao.CosplayDao
import com.cosplayjournal.app.data.entity.*
import kotlinx.coroutines.flow.Flow

class CosplayRepository(private val cosplayDao: CosplayDao) {
    val allCosplans: Flow<List<Cosplan>> = cosplayDao.getAllCosplans()
    val favoriteCosplays: Flow<List<Cosplay>> = cosplayDao.getFavoriteCosplays()

    suspend fun insertCosplan(cosplan: Cosplan): Long = cosplayDao.insertCosplan(cosplan)
    suspend fun updateCosplan(cosplan: Cosplan) = cosplayDao.updateCosplan(cosplan)
    suspend fun deleteCosplan(cosplan: Cosplan) = cosplayDao.deleteCosplan(cosplan)

    fun getCosplaysForCosplan(cosplanId: Long): Flow<List<Cosplay>> = cosplayDao.getCosplaysForCosplan(cosplanId)
    suspend fun insertCosplay(cosplay: Cosplay): Long = cosplayDao.insertCosplay(cosplay)

    fun getHandmadeParts(cosplayId: Long): Flow<List<HandmadePart>> = cosplayDao.getHandmadeParts(cosplayId)
    suspend fun insertHandmadePart(part: HandmadePart) = cosplayDao.insertHandmadePart(part)

    fun getPurchasedItems(cosplayId: Long): Flow<List<PurchasedItem>> = cosplayDao.getPurchasedItems(cosplayId)
    suspend fun insertPurchasedItem(item: PurchasedItem) = cosplayDao.insertPurchasedItem(item)
}
