package com.cosplayjournal.app.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.ErrorApp
import com.cosplayjournal.app.data.clasificar
import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.events.EventoConEstado
import com.cosplayjournal.app.data.profile.Perfil
import com.cosplayjournal.app.data.profile.ProfileRepository
import com.cosplayjournal.app.data.social.FotoEscaparate
import com.cosplayjournal.app.data.social.SocialRepository
import com.cosplayjournal.app.ui.common.texto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MiPerfilUi(
    val perfil: Perfil? = null,
    val escaparate: List<FotoEscaparate> = emptyList(),
    val proximos: List<EventoConEstado> = emptyList(),
    val subiendo: Boolean = false,
    @StringRes val mensaje: Int? = null,
)

class ProfileViewModel(
    private val perfiles: ProfileRepository,
    private val social: SocialRepository,
    eventos: Flow<List<EventoConEstado>>,
    private val userId: () -> String?,
    private val hoy: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val estado = MutableStateFlow(MiPerfilUi())

    val ui: StateFlow<MiPerfilUi> = combine(estado, eventos) { s, evs ->
        s.copy(proximos = evs.filter { it.estado.asistencia == Asistencia.GOING && it.evento.fin >= hoy() }
            .sortedBy { it.evento.inicio })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MiPerfilUi())

    init {
        cargar()
    }

    fun cargar() = viewModelScope.launch {
        val id = userId() ?: return@launch
        runCatching { perfiles.miPerfil(id) }.onSuccess { p -> estado.update { it.copy(perfil = p) } }
        runCatching { social.escaparate(id) }.onSuccess { e -> estado.update { it.copy(escaparate = e) } }
    }

    fun cambiarAvatar(uri: String) = subir(null) {
        val ruta = social.cambiarAvatar(uri)
        estado.update { it.copy(perfil = it.perfil?.copy(avatarPath = ruta)) }
    }

    /** Desde el visor de la galería. */
    fun añadirAlEscaparate(uri: String, eventoId: Long?) = subir(R.string.esc_anadida) {
        social.añadirAlEscaparate(uri, eventoId, null)
        cargar()
    }

    fun quitar(foto: FotoEscaparate) = viewModelScope.launch {
        runCatching { social.quitarDelEscaparate(foto) }
            .onSuccess { estado.update { s -> s.copy(escaparate = s.escaparate - foto) } }
            .onFailure { e -> estado.update { it.copy(mensaje = clasificar(e).texto()) } }
    }

    fun mensajeVisto() = estado.update { it.copy(mensaje = null) }

    private fun subir(@StringRes exito: Int?, bloque: suspend () -> Unit) = viewModelScope.launch {
        estado.update { it.copy(subiendo = true) }
        val r = runCatching { bloque() }
        estado.update {
            it.copy(subiendo = false, mensaje = r.fold({ exito }, { e ->
                val err = clasificar(e)
                if (err is ErrorApp.Servidor && err.codigo == "showcase_full") R.string.esc_lleno else err.texto()
            }))
        }
    }
}
