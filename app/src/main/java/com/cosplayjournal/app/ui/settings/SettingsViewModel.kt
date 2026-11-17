package com.cosplayjournal.app.ui.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.auth.AuthRepository
import com.cosplayjournal.app.data.clasificar
import com.cosplayjournal.app.data.feedback.FeedbackRepository
import com.cosplayjournal.app.data.settings.SettingsRepository
import com.cosplayjournal.app.ui.common.texto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AjustesUi(
    val imagenesSinConexion: Boolean = true,
    val bytesCache: Long? = null,
    val ocupado: Boolean = false,
    @StringRes val mensaje: Int? = null,
    val comentarioEnviado: Boolean = false,
)

/** Operaciones locales que dependen de Android; se inyectan para poder probar el ViewModel. */
interface AlmacenLocal {
    suspend fun tamañoCache(): Long
    suspend fun borrarCache()
    suspend fun borrarDatos()
}

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val auth: AuthRepository,
    private val comentarios: FeedbackRepository,
    private val almacen: AlmacenLocal,
    private val version: String,
    /** Borra avatar y escaparate del servidor antes de eliminar la cuenta. */
    private val borrarArchivos: suspend () -> Unit = {},
) : ViewModel() {

    private val local = MutableStateFlow(AjustesUi())

    val ui: StateFlow<AjustesUi> = combine(settings.ajustes, local) { a, l ->
        l.copy(imagenesSinConexion = a.imagenesSinConexion)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AjustesUi())

    init {
        recalcularCache()
    }

    private fun recalcularCache() = viewModelScope.launch {
        local.update { it.copy(bytesCache = almacen.tamañoCache()) }
    }

    fun imagenesSinConexion(activo: Boolean) = viewModelScope.launch {
        settings.setImagenesSinConexion(activo)
        if (!activo) almacen.borrarCache()
        recalcularCache()
    }

    fun borrarCache() = tarea(R.string.ajustes_cache_borrada) { almacen.borrarCache() }

    fun borrarDatos() = tarea(R.string.ajustes_datos_borrados) { almacen.borrarDatos() }

    fun enviarComentario(texto: String, idioma: String) {
        if (texto.isBlank()) return
        tarea(R.string.ajustes_comentarios_gracias) {
            comentarios.enviar(auth.userIdActual(), texto, version, idioma)
            local.update { it.copy(comentarioEnviado = true) }
        }
    }

    fun comentarioVisto() = local.update { it.copy(comentarioEnviado = false) }

    fun cerrarSesion() = tarea(null) {
        auth.salir()
        almacen.borrarDatos()
    }

    fun borrarCuenta() = tarea(null) {
        runCatching { borrarArchivos() }
        auth.borrarCuenta()
        almacen.borrarDatos()
    }

    fun mensajeMostrado() = local.update { it.copy(mensaje = null) }

    private fun tarea(@StringRes exito: Int?, bloque: suspend () -> Unit) {
        if (local.value.ocupado) return
        local.update { it.copy(ocupado = true) }
        viewModelScope.launch {
            val r = runCatching { bloque() }
            local.update {
                it.copy(ocupado = false, mensaje = r.fold({ exito }, { e -> clasificar(e).texto() }))
            }
            recalcularCache()
        }
    }
}
