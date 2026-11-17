package com.cosplayjournal.app.data.gallery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class FotoGaleria(val foto: Foto, val etiquetas: FotoEtiquetas)

/** Dónde se guarda la carpeta elegida (DataStore en la app, memoria en los tests). */
interface CarpetaGuardada {
    val carpeta: Flow<String?>
    suspend fun guardar(uri: String?)
}

class GalleryRepository(
    private val dao: FotoDao,
    private val lector: LectorCarpeta,
    private val guardada: CarpetaGuardada,
) {
    private val _fotos = MutableStateFlow<List<Foto>>(emptyList())
    private val _escaneando = MutableStateFlow(false)
    val escaneando: StateFlow<Boolean> = _escaneando.asStateFlow()

    val carpeta: Flow<String?> = guardada.carpeta
    val cosplays: Flow<List<String>> = dao.cosplays()

    val fotos: Flow<List<FotoGaleria>> = combine(_fotos, dao.todas()) { fotos, etiquetas ->
        val porUri = etiquetas.associateBy { it.uri }
        fotos.map { FotoGaleria(it, porUri[it.uri] ?: FotoEtiquetas(it.uri)) }
    }

    suspend fun vincular(carpeta: String) {
        guardada.carpeta.first()?.let { anterior -> if (anterior != carpeta) lector.soltarPermiso(anterior) }
        lector.conservarPermiso(carpeta)
        guardada.guardar(carpeta)
        escanear()
    }

    /** Vuelve a leer la carpeta (al abrir la galería y al deslizar). */
    suspend fun escanear() {
        val c = guardada.carpeta.first() ?: run { _fotos.value = emptyList(); return }
        _escaneando.value = true
        try {
            _fotos.value = runCatching { lector.imagenes(c) }.getOrElse {
                // Se perdió el permiso o la carpeta ya no existe: hay que volver a elegirla
                guardada.guardar(null)
                emptyList()
            }
        } finally {
            _escaneando.value = false
        }
    }

    /** Aplica a varias fotos la misma etiqueta. Un campo null se deja como estaba. */
    suspend fun etiquetar(
        uris: Collection<String>,
        eventoId: Long? = null,
        cosplay: String? = null,
        grupal: Boolean? = null,
        quitarEvento: Boolean = false,
        quitarCosplay: Boolean = false,
    ) {
        val actuales = dao.todas().first().associateBy { it.uri }
        dao.guardar(uris.map { uri ->
            val a = actuales[uri] ?: FotoEtiquetas(uri)
            a.copy(
                eventoId = if (quitarEvento) null else eventoId ?: a.eventoId,
                cosplay = if (quitarCosplay) null else cosplay?.trim()?.takeIf { it.isNotEmpty() } ?: a.cosplay,
                grupal = grupal ?: a.grupal,
            )
        })
    }

    suspend fun borrarLocal() {
        guardada.carpeta.first()?.let { lector.soltarPermiso(it) }
        guardada.guardar(null)
        dao.borrarTodo()
        _fotos.value = emptyList()
    }
}
