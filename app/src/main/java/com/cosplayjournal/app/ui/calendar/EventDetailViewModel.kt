package com.cosplayjournal.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.events.EventRepository
import com.cosplayjournal.app.data.events.EventoConEstado
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Programa o cancela el aviso de la víspera. Se inyecta para no depender de WorkManager en los tests. */
interface Avisos {
    fun programar(eventoId: Long, nombre: String, inicio: LocalDate)
    fun cancelar(eventoId: Long)
}

class EventDetailViewModel(
    private val id: Long,
    private val repo: EventRepository,
    private val avisos: Avisos,
) : ViewModel() {

    val evento: StateFlow<EventoConEstado?> =
        repo.evento(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Pulsar la opción ya marcada la desmarca. */
    fun asistencia(a: Asistencia) = viewModelScope.launch {
        val e = evento.value ?: return@launch
        val nueva = if (e.estado.asistencia == a) null else a
        repo.marcarAsistencia(id, nueva)
        if (nueva == Asistencia.GOING) avisos.programar(id, e.evento.nombre, e.evento.inicio) else avisos.cancelar(id)
    }

    fun favorito() = viewModelScope.launch { repo.cambiarFavorito(id) }
}
