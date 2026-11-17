package com.cosplayjournal.app.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.events.EventoConEstado
import com.cosplayjournal.app.data.gallery.FotoGaleria
import com.cosplayjournal.app.data.gallery.GalleryRepository
import com.cosplayjournal.app.domain.Grupo
import com.cosplayjournal.app.domain.Orden
import com.cosplayjournal.app.domain.agrupar
import com.cosplayjournal.app.domain.fechaDe
import com.cosplayjournal.app.domain.sugerirEvento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GaleriaUi(
    val cargando: Boolean = true,
    val carpeta: String? = null,
    val escaneando: Boolean = false,
    val orden: Orden = Orden.TODAS,
    val grupos: List<Grupo> = emptyList(),
    val total: Int = 0,
    val seleccion: Set<String> = emptySet(),
    val eventos: List<EventoConEstado> = emptyList(),
    val cosplays: List<String> = emptyList(),
) {
    val seleccionando get() = seleccion.isNotEmpty()
    /** Fotos en el orden en que se ven, para el visor. */
    val visibles: List<FotoGaleria> get() = grupos.flatMap { it.fotos }
}

/** Datos que propone la hoja de etiquetar a partir de las fotos elegidas. */
data class Propuesta(val evento: EventoConEstado?, val cosplay: String?, val grupal: Boolean)

class GalleryViewModel(
    private val repo: GalleryRepository,
    eventos: Flow<List<EventoConEstado>>,
) : ViewModel() {

    private val orden = MutableStateFlow(Orden.TODAS)
    private val seleccion = MutableStateFlow<Set<String>>(emptySet())

    val ui: StateFlow<GaleriaUi> = combine(
        combine(repo.fotos, repo.carpeta, repo.escaneando, ::Triple),
        eventos, repo.cosplays, orden, seleccion,
    ) { (fotos, carpeta, escaneando), evs, cosplays, o, sel ->
        val nombres = evs.associate { it.evento.id to it.evento.nombre }
        GaleriaUi(
            cargando = false,
            carpeta = carpeta,
            escaneando = escaneando,
            orden = o,
            grupos = agrupar(fotos, o) { nombres[it] },
            total = fotos.size,
            seleccion = sel,
            eventos = evs,
            cosplays = cosplays,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GaleriaUi())

    init {
        escanear()
    }

    fun escanear() = viewModelScope.launch { repo.escanear() }
    fun vincular(uri: String) = viewModelScope.launch { repo.vincular(uri) }
    fun orden(o: Orden) = orden.update { o }
    fun alternar(uri: String) = seleccion.update { if (uri in it) it - uri else it + uri }
    fun seleccionar(uris: Collection<String>) = seleccion.update { it + uris }
    fun limpiarSeleccion() = seleccion.update { emptySet() }

    /** Lo que ya tienen en común las fotos o, si no tienen evento, el que encaja por fecha. */
    fun propuesta(uris: Set<String>): Propuesta {
        val s = ui.value
        val fotos = s.visibles.filter { it.foto.uri in uris }
        val eventoComun = fotos.map { it.etiquetas.eventoId }.distinct().singleOrNull()
        val evento = eventoComun?.let { id -> s.eventos.firstOrNull { it.evento.id == id } }
            ?: fotos.firstOrNull()?.let { sugerirEvento(fechaDe(it), s.eventos) }
        return Propuesta(
            evento = evento,
            cosplay = fotos.map { it.etiquetas.cosplay }.distinct().singleOrNull(),
            grupal = fotos.isNotEmpty() && fotos.all { it.etiquetas.grupal },
        )
    }

    fun etiquetar(uris: Set<String>, eventoId: Long?, cosplay: String?, grupal: Boolean) = viewModelScope.launch {
        repo.etiquetar(
            uris, eventoId = eventoId, cosplay = cosplay, grupal = grupal,
            quitarEvento = eventoId == null, quitarCosplay = cosplay.isNullOrBlank(),
        )
        limpiarSeleccion()
    }
}
