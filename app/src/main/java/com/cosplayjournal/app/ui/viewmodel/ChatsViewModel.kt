package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.chat.ChatRepository
import com.cosplayjournal.app.data.remote.RemoteConversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ChatsUiState(
    val conversations: List<RemoteConversation> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null
)

/** Lista de chats y grupos, con alta de chat privado y de grupo. */
class ChatsViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _state = MutableStateFlow(ChatsUiState())
    val state: StateFlow<ChatsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        perform(successMessage = null) {
            _state.value = _state.value.copy(conversations = repository.conversations())
        }
    }

    fun openPrivate(nickname: String, onOpened: (String) -> Unit) {
        if (nickname.isBlank()) return
        perform(successMessage = null) {
            val created = repository.openPrivate(nickname)
            onOpened(created.id)
            _state.value = _state.value.copy(conversations = repository.conversations())
        }
    }

    fun createGroup(name: String, nicknames: String, onOpened: (String) -> Unit) {
        val members = nicknames.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (name.isBlank() || members.isEmpty()) return
        perform(successMessage = null) {
            val created = repository.createGroup(name, members)
            onOpened(created.id)
            _state.value = _state.value.copy(conversations = repository.conversations())
        }
    }

    private fun perform(successMessage: String?, action: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            try {
                action()
                _state.value = _state.value.copy(busy = false, message = successMessage)
            } catch (e: HttpException) {
                _state.value = _state.value.copy(busy = false, message = messageFor(e.code()))
            } catch (e: IOException) {
                _state.value = _state.value.copy(busy = false, message = "Sin conexión con el servidor")
            }
        }
    }

    private fun messageFor(code: Int): String = when (code) {
        400 -> "Solo puedes chatear con tus amigos, y los nicknames deben existir"
        401 -> "Inicia sesión para ver tus chats"
        404 -> "No existe ese nickname"
        else -> "No se pudo completar la operación (código " + code + ")"
    }
}

class ChatsViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
