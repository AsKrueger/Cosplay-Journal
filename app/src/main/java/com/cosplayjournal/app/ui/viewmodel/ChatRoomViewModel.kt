package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.chat.ChatRepository
import com.cosplayjournal.app.data.remote.RemoteMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ChatRoomUiState(
    val messages: List<RemoteMessage> = emptyList(),
    val message: String? = null
)

/**
 * Una conversación: carga los mensajes y consulta los nuevos cada pocos segundos mientras está abierta.
 */
class ChatRoomViewModel(
    private val repository: ChatRepository,
    private val conversationId: String
) : ViewModel() {

    private val _state = MutableStateFlow(ChatRoomUiState())
    val state: StateFlow<ChatRoomUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                fetchNew()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { sendWith { repository.sendText(conversationId, text) } }
    }

    fun sendPhoto(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch { sendWith { repository.sendPhoto(conversationId, url) } }
    }

    private suspend fun sendWith(send: suspend () -> RemoteMessage) {
        try {
            val sent = send()
            appendNew(listOf(sent))
        } catch (e: HttpException) {
            _state.value = _state.value.copy(message = messageFor(e.code()))
        } catch (e: IOException) {
            _state.value = _state.value.copy(message = "Sin conexión: el mensaje no se ha enviado")
        }
    }

    /** Pide los mensajes posteriores al último que ya tenemos. */
    private suspend fun fetchNew() {
        try {
            val since = _state.value.messages.lastOrNull()?.createdAt
            appendNew(repository.messages(conversationId, since))
        } catch (e: HttpException) {
            _state.value = _state.value.copy(message = messageFor(e.code()))
        } catch (e: IOException) {
            // Sin conexión: se reintenta en el siguiente ciclo
        }
    }

    private fun appendNew(incoming: List<RemoteMessage>) {
        val known = _state.value.messages.map { it.id }.toSet()
        val fresh = incoming.filter { it.id !in known }
        if (fresh.isNotEmpty()) {
            _state.value = _state.value.copy(messages = _state.value.messages + fresh)
        }
    }

    private fun messageFor(code: Int): String = when (code) {
        401 -> "Inicia sesión para ver esta conversación"
        404 -> "Esta conversación ya no existe o no eres miembro"
        else -> "No se pudo completar la operación (código " + code + ")"
    }

    private companion object {
        const val POLL_INTERVAL_MS = 4_000L
    }
}

class ChatRoomViewModelFactory(
    private val repository: ChatRepository,
    private val conversationId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatRoomViewModel(repository, conversationId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
