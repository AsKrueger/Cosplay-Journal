package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.data.entity.HandmadePart
import com.cosplayjournal.app.data.entity.PurchasedItem
import com.cosplayjournal.app.data.repository.CosplayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CosplayViewModel(private val repository: CosplayRepository) : ViewModel() {

    private val _currentCosplanId = MutableStateFlow<Long>(-1L)
    
    val cosplaysForPlan: StateFlow<List<Cosplay>> = _currentCosplanId.flatMapLatest { id ->
        repository.getCosplaysForCosplan(id)
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

    fun updateCosplay(cosplay: Cosplay) = viewModelScope.launch {
        repository.updateCosplay(cosplay)
    }

    // Handmade Parts
    fun getHandmadeParts(cosplayId: Long) = repository.getHandmadeParts(cosplayId)
    
    fun insertHandmadePart(part: HandmadePart) = viewModelScope.launch {
        repository.insertHandmadePart(part)
    }

    fun updateHandmadePart(part: HandmadePart) = viewModelScope.launch {
        repository.updateHandmadePart(part)
    }

    // Purchased Items
    fun getPurchasedItems(cosplayId: Long) = repository.getPurchasedItems(cosplayId)
    
    fun insertPurchasedItem(item: PurchasedItem) = viewModelScope.launch {
        repository.insertPurchasedItem(item)
    }

    fun updatePurchasedItem(item: PurchasedItem) = viewModelScope.launch {
        repository.updatePurchasedItem(item)
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
