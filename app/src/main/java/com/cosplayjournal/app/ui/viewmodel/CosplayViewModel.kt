package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.entity.*
import com.cosplayjournal.app.data.repository.CosplayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class CosplayViewModel(private val repository: CosplayRepository) : ViewModel() {

    private val _currentCosplanId = MutableStateFlow<Long>(-1L)
    
    val allCosplays: StateFlow<List<Cosplay>> = repository.allCosplays.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cosplaysForPlan: StateFlow<List<Cosplay>> = _currentCosplanId.flatMapLatest { id ->
        if (id == -1L) repository.allCosplays else repository.getCosplaysForPlan(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setCosplanId(id: Long) {
        _currentCosplanId.value = id
    }

    suspend fun getCosplayById(id: Long): Cosplay? {
        return repository.getCosplayById(id)
    }

    fun insertCosplay(cosplay: Cosplay) = viewModelScope.launch {
        repository.insertCosplay(cosplay)
    }
    
    suspend fun insertCosplayAndGetId(cosplay: Cosplay): Long = withContext(Dispatchers.IO) {
        repository.insertCosplay(cosplay)
    }

    fun updateCosplay(cosplay: Cosplay) = viewModelScope.launch {
        repository.updateCosplay(cosplay)
    }

    // Handmade Parts
    fun getHandmadeParts(cosplayId: Long) = repository.getHandmadeParts(cosplayId)
    
    suspend fun getHandmadePartById(id: Long): HandmadePart? = repository.getHandmadePartById(id)

    fun insertHandmadePart(part: HandmadePart) = viewModelScope.launch {
        repository.insertHandmadePart(part)
    }
    
    suspend fun insertHandmadePartAndGetId(part: HandmadePart): Long = withContext(Dispatchers.IO) {
        repository.insertHandmadePart(part)
    }

    fun updateHandmadePart(part: HandmadePart) = viewModelScope.launch {
        repository.updateHandmadePart(part)
    }

    // Part Resources
    fun getResourcesForPart(partId: Long) = repository.getResourcesForPart(partId)
    
    fun insertPartResource(resource: PartResource) = viewModelScope.launch {
        repository.insertPartResource(resource)
    }

    // Purchased Items
    fun getPurchasedItems(cosplayId: Long) = repository.getPurchasedItems(cosplayId)
    
    suspend fun getPurchasedItemById(id: Long): PurchasedItem? = repository.getPurchasedItemById(id)

    fun insertPurchasedItem(item: PurchasedItem) = viewModelScope.launch {
        repository.insertPurchasedItem(item)
    }

    fun updatePurchasedItem(item: PurchasedItem) = viewModelScope.launch {
        repository.updatePurchasedItem(item)
    }

    // Photo Sessions
    fun getPhotoSessions(cosplayId: Long) = repository.getPhotoSessionsForCosplay(cosplayId)

    fun addPhotoSessionToCosplay(cosplayId: Long, session: PhotoSession) = viewModelScope.launch {
        val sessionId = repository.insertPhotoSession(session)
        repository.insertCosplayPhotoSessionCrossRef(CosplayPhotoSessionCrossRef(cosplayId, sessionId))
    }
}

class CosplayViewModelFactory(private val repository: CosplayRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CosplayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CosplayViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
