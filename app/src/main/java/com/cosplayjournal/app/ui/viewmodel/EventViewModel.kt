package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.entity.UserEventData
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.data.repository.EventRepository
import com.cosplayjournal.app.data.repository.UserEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(
    private val repository: EventRepository,
    private val userEventRepository: UserEventRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _loadError = MutableStateFlow(false)
    /** true cuando la última carga desde el backend ha fallado. */
    val loadError: StateFlow<Boolean> = _loadError

    val userEventData: StateFlow<List<UserEventData>> = userEventRepository.all.stateIn(
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
            try {
                // Con sesión iniciada, el estado de asistencia del servidor manda sobre el local
                userEventRepository.syncFromServer()
            } catch (e: Exception) {
                // Sin sesión o sin conexión: se mantiene el estado local
            }
        }
    }

    fun updateEventStatus(eventId: String, status: String) = viewModelScope.launch {
        val current = userEventRepository.get(eventId) ?: UserEventData(eventId)
        userEventRepository.save(current.copy(status = status))
    }

}

class EventViewModelFactory(
    private val repository: EventRepository,
    private val userEventRepository: UserEventRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(repository, userEventRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
