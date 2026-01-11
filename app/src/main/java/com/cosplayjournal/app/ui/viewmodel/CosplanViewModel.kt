package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.entity.Cosplan
import com.cosplayjournal.app.data.repository.CosplayRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CosplanViewModel(private val repository: CosplayRepository) : ViewModel() {

    val allCosplans: StateFlow<List<Cosplan>> = repository.allCosplans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insert(cosplan: Cosplan) = viewModelScope.launch {
        repository.insertCosplan(cosplan)
    }

    fun update(cosplan: Cosplan) = viewModelScope.launch {
        repository.updateCosplan(cosplan)
    }

    fun delete(cosplan: Cosplan) = viewModelScope.launch {
        repository.deleteCosplan(cosplan)
    }
}

class CosplanViewModelFactory(private val repository: CosplayRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CosplanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CosplanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
