package com.cosplayjournal.app.ui.friends

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.clasificar
import com.cosplayjournal.app.data.events.EventoConEstado
import com.cosplayjournal.app.data.social.PerfilAmigo
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

data class PerfilAmigoUi(
    val cargando: Boolean = true,
    val perfil: PerfilAmigo? = null,
    /** Próximos eventos a los que va, con sus datos del calendario local. */
    val vaA: List<EventoConEstado> = emptyList(),
    @StringRes val error: Int? = null,
    @StringRes val mensaje: Int? = null,
    /** true cuando ya no es amigo (eliminado o bloqueado): la pantalla se cierra. */
    val cerrar: Boolean = false,
)

class FriendProfileViewModel(
    private val id: String,
    private val social: SocialRepository,
    eventos: Flow<List<EventoConEstado>>,
    private val hoy: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val estado = MutableStateFlow(PerfilAmigoUi())

    val ui: StateFlow<PerfilAmigoUi> = combine(estado, eventos) { s, evs ->
        val ids = s.perfil?.vaA?.toSet().orEmpty()
        s.copy(vaA = evs.filter { it.evento.id in ids && it.evento.fin >= hoy() }.sortedBy { it.evento.inicio })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerfilAmigoUi())

    init {
        cargar()
    }

    fun cargar() = viewModelScope.launch {
        estado.update { it.copy(cargando = true, error = null) }
        runCatching { social.perfilAmigo(id, hoy()) }
            .onSuccess { p -> estado.update { it.copy(cargando = false, perfil = p) } }
            .onFailure { e ->
                // Sin acceso al perfil (ya no sois amigos) la consulta no devuelve fila
                estado.update { it.copy(cargando = false, error = if (e.message?.contains("row") == true)
                    R.string.pa_no_disponible else clasificar(e).texto()) }
            }
    }

    fun eliminar() = cerrarTras { social.eliminar(id) }
    fun bloquear() = cerrarTras { social.bloquear(id) }

    fun denunciar(motivo: String) = viewModelScope.launch {
        val r = runCatching { social.denunciar(id, motivo) }
        estado.update { it.copy(mensaje = r.fold({ R.string.pa_denuncia_enviada }, { e -> clasificar(e).texto() })) }
    }

    fun mensajeVisto() = estado.update { it.copy(mensaje = null) }

    private fun cerrarTras(bloque: suspend () -> Unit) = viewModelScope.launch {
        runCatching { bloque() }
            .onSuccess { estado.update { it.copy(cerrar = true) } }
            .onFailure { e -> estado.update { it.copy(mensaje = clasificar(e).texto()) } }
    }
}
