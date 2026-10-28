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

    private val _loadError = MutableStateFlow(false)
    /** true cuando la última carga desde el backend ha fallado. */
    val loadError: StateFlow<Boolean> = _loadError

    val userEventData: StateFlow<List<UserEventData>> = cosplayRepository.allUserEventData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadEvents()
    }

    /**
     * Muestra primero lo que hay en el backend y, después, pide una sincronización con ListadoManga
     * y vuelve a cargar. Si la sincronización falla, se conserva lo ya mostrado.
     */
    fun loadEvents() {
        viewModelScope.launch {
            try {
                _events.value = repository.getEvents()
                _loadError.value = false
            } catch (e: Exception) {
                // No se conservan datos anteriores: un catálogo desactualizado induce a error
                _events.value = emptyList()
                _loadError.value = true
                return@launch
            }
            try {
                repository.refresh()
                _events.value = repository.getEvents()
            } catch (e: Exception) {
                // La sincronización es opcional: el catálogo mostrado sigue siendo válido
            }
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
