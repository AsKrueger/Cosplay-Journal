package com.cosplayjournal.app.ui.friends

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.ErrorApp
import com.cosplayjournal.app.data.clasificar
import com.cosplayjournal.app.data.social.Contacto
import com.cosplayjournal.app.data.social.Relacion
import com.cosplayjournal.app.data.social.ResultadoBusqueda
import com.cosplayjournal.app.data.social.SocialRepository
import com.cosplayjournal.app.ui.common.texto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AmigosUi(
    val cargando: Boolean = true,
    val contactos: List<Contacto> = emptyList(),
    val busqueda: String = "",
    val buscando: Boolean = false,
    val resultados: List<ResultadoBusqueda>? = null,
    @StringRes val mensaje: Int? = null,
) {
    val recibidas get() = contactos.filter { it.relacion == Relacion.RECEIVED }
    val enviadas get() = contactos.filter { it.relacion == Relacion.SENT }
    val amigos get() = contactos.filter { it.relacion == Relacion.FRIEND }
}

class FriendsViewModel(private val social: SocialRepository) : ViewModel() {
    private val _ui = MutableStateFlow(AmigosUi())
    val ui: StateFlow<AmigosUi> = _ui.asStateFlow()
    private var busquedaJob: Job? = null

    init {
        refrescar()
    }

    fun refrescar() = viewModelScope.launch {
        runCatching { social.contactos() }
            .onSuccess { c -> _ui.update { it.copy(cargando = false, contactos = c) } }
            .onFailure { e -> _ui.update { it.copy(cargando = false, mensaje = clasificar(e).texto()) } }
    }

    fun buscar(texto: String) {
        _ui.update { it.copy(busqueda = texto) }
        busquedaJob?.cancel()
        if (texto.trim().length < 3) {
            _ui.update { it.copy(resultados = null, buscando = false) }
            return
        }
        busquedaJob = viewModelScope.launch {
            delay(350)
            _ui.update { it.copy(buscando = true) }
            val r = runCatching { social.buscar(texto.trim()) }
            _ui.update { it.copy(buscando = false, resultados = r.getOrNull() ?: it.resultados,
                mensaje = r.exceptionOrNull()?.let { e -> clasificar(e).texto() }) }
        }
    }

    fun enviar(id: String) = accion {
        val rel = social.enviarSolicitud(id)
        marcarResultado(id, rel)
        if (rel == Relacion.FRIEND) R.string.ami_ya_sois_amigos else R.string.ami_solicitud_enviada
    }

    fun enviarPorCodigo(codigo: String) = accion {
        if (social.enviarSolicitudPorCodigo(codigo) == Relacion.FRIEND) R.string.ami_ya_sois_amigos
        else R.string.ami_solicitud_enviada
    }

    fun responder(id: String, aceptar: Boolean) = accion {
        social.responder(id, aceptar)
        if (aceptar) R.string.ami_ya_sois_amigos else null
    }

    fun cancelar(id: String) = accion {
        social.eliminar(id)
        marcarResultado(id, Relacion.NONE)
        null
    }

    fun mensajeVisto() = _ui.update { it.copy(mensaje = null) }

    private fun marcarResultado(id: String, rel: Relacion) = _ui.update { s ->
        s.copy(resultados = s.resultados?.map { if (it.id == id) it.copy(relation = rel) else it })
    }

    private fun accion(bloque: suspend () -> Int?) = viewModelScope.launch {
        val r = runCatching { bloque() }
        val msg = r.fold({ it }, { e ->
            val err = clasificar(e)
            if (err is ErrorApp.Servidor && err.codigo == "user_not_found") R.string.ami_no_encontrado else err.texto()
        })
        _ui.update { it.copy(mensaje = msg) }
        refrescar()
    }
}

