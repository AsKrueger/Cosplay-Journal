package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.entity.EventCosplanSelection
import com.cosplayjournal.app.data.entity.UserEventData
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.data.repository.CosplayRepository
import com.cosplayjournal.app.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(
    private val repository: EventRepository,
    private val cosplayRepository: CosplayRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    val userEventData: StateFlow<List<UserEventData>> = cosplayRepository.allUserEventData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _events.value = repository.getEvents()
        }
    }

    fun updateEventStatus(eventId: String, status: String) = viewModelScope.launch {
        val current = cosplayRepository.getUserEventData(eventId) ?: UserEventData(eventId)
        cosplayRepository.insertUserEventData(current.copy(status = status))
    }

    fun toggleFavorite(eventId: String) = viewModelScope.launch {
        val current = cosplayRepository.getUserEventData(eventId) ?: UserEventData(eventId)
        cosplayRepository.insertUserEventData(current.copy(isFavorite = !current.isFavorite))
    }

    fun getSelectionsForEvent(eventId: String) = cosplayRepository.getCosplanSelectionsForEvent(eventId)

    fun selectCosplanForEvent(eventId: String, cosplanId: Long, day: String) = viewModelScope.launch {
        cosplayRepository.insertCosplanSelection(EventCosplanSelection(eventId, cosplanId, day))
    }
}

class EventViewModelFactory(
    private val repository: EventRepository,
    private val cosplayRepository: CosplayRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(repository, cosplayRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
