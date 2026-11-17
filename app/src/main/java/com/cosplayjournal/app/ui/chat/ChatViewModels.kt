package com.cosplayjournal.app.ui.chat

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.chat.ChatRepository
import com.cosplayjournal.app.data.chat.InfoChat
import com.cosplayjournal.app.data.chat.Mensaje
import com.cosplayjournal.app.data.chat.ResumenChat
import com.cosplayjournal.app.data.clasificar
import com.cosplayjournal.app.data.social.Contacto
import com.cosplayjournal.app.data.social.Relacion
import com.cosplayjournal.app.data.social.SocialRepository
import com.cosplayjournal.app.ui.common.texto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ------------------------------------------------------------------ Lista de chats

data class ChatsUi(
    val cargando: Boolean = true,
    val chats: List<ResumenChat> = emptyList(),
    val amigos: List<Contacto> = emptyList(),
    @StringRes val error: Int? = null,
    /** Chat recién creado al que hay que navegar. */
    val abrir: String? = null,
)

class ChatsViewModel(private val chats: ChatRepository, private val social: SocialRepository) : ViewModel() {
    private val _ui = MutableStateFlow(ChatsUi())
    val ui: StateFlow<ChatsUi> = _ui.asStateFlow()

    fun refrescar() = viewModelScope.launch {
        val r = runCatching { chats.misChats() }
        _ui.update { it.copy(cargando = false, chats = r.getOrDefault(it.chats), error = r.exceptionOrNull()?.let { e -> clasificar(e).texto() }) }
    }

    fun cargarAmigos() = viewModelScope.launch {
        runCatching { social.contactos() }.onSuccess { c -> _ui.update { it.copy(amigos = c.filter { a -> a.relacion == Relacion.FRIEND }) } }
    }

    fun crearGrupo(titulo: String, miembros: List<String>, eventoId: Long?) = viewModelScope.launch {
        runCatching { chats.crearGrupo(titulo, miembros, eventoId) }
            .onSuccess { id -> _ui.update { it.copy(abrir = id) }; refrescar() }
            .onFailure { e -> _ui.update { it.copy(error = clasificar(e).texto()) } }
    }

    fun abierto() = _ui.update { it.copy(abrir = null, error = null) }
}

// ------------------------------------------------------------------ Conversación

data class ChatUi(
    val cargando: Boolean = true,
    /** Del más reciente al más antiguo (la lista se pinta invertida). */
    val mensajes: List<Mensaje> = emptyList(),
    val info: InfoChat? = null,
    val hayMas: Boolean = true,
    val enviando: Boolean = false,
    @StringRes val error: Int? = null,
    val salido: Boolean = false,
)

class ChatRoomViewModel(
    private val chatId: String,
    private val chats: ChatRepository,
    val yo: String?,
) : ViewModel() {
    private val _ui = MutableStateFlow(ChatUi())
    val ui: StateFlow<ChatUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { chats.mensajes(chatId) }
                .onSuccess { m -> _ui.update { it.copy(cargando = false, mensajes = m, hayMas = m.size >= 50) } }
                .onFailure { e -> _ui.update { it.copy(cargando = false, error = clasificar(e).texto()) } }
            chats.marcarLeido(chatId)
        }
        viewModelScope.launch { runCatching { chats.info(chatId) }.onSuccess { i -> _ui.update { it.copy(info = i) } } }
        viewModelScope.launch {
            runCatching {
                chats.nuevos(chatId).collect { m ->
                    añadir(listOf(m))
                    chats.marcarLeido(chatId)
                }
            }
        }
    }

    /** Une mensajes sin duplicar (el propio llega por la respuesta y por tiempo real). */
    private fun añadir(nuevos: List<Mensaje>) = _ui.update { s ->
        s.copy(mensajes = (nuevos + s.mensajes).distinctBy { it.id }.sortedByDescending { it.id })
    }

    fun cargarMas() {
        val s = _ui.value
        if (!s.hayMas || s.cargando) return
        val ultimo = s.mensajes.lastOrNull()?.fecha ?: return
        _ui.update { it.copy(cargando = true) }
        viewModelScope.launch {
            val r = runCatching { chats.mensajes(chatId, antesDe = ultimo) }
            _ui.update { st ->
                val antiguos = r.getOrDefault(emptyList())
                st.copy(cargando = false, hayMas = antiguos.size >= 50,
                    mensajes = (st.mensajes + antiguos).distinctBy { it.id }.sortedByDescending { it.id })
            }
        }
    }

    fun enviarTexto(texto: String) = enviar { chats.enviarTexto(chatId, texto) }
    fun enviarFoto(uri: String) = enviar { chats.enviarFoto(chatId, uri) }

    private fun enviar(bloque: suspend () -> Unit) {
        _ui.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            val r = runCatching { bloque() }
            // Por si el tiempo real no está disponible, se recargan los últimos mensajes
            if (r.isSuccess) runCatching { chats.mensajes(chatId, limite = 20) }.onSuccess { añadir(it) }
            _ui.update { it.copy(enviando = false, error = r.exceptionOrNull()?.let { e -> clasificar(e).texto() }) }
        }
    }

    fun añadirMiembro(userId: String) = viewModelScope.launch {
        runCatching { chats.añadirMiembro(chatId, userId); chats.info(chatId) }
            .onSuccess { i -> _ui.update { it.copy(info = i) } }
            .onFailure { e -> _ui.update { it.copy(error = clasificar(e).texto()) } }
    }

    fun salir() = viewModelScope.launch {
        runCatching { chats.salir(chatId) }.onSuccess { _ui.update { it.copy(salido = true) } }
    }

    fun errorVisto() = _ui.update { it.copy(error = null) }
}
