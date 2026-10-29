package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.friends.FriendsRepository
import com.cosplayjournal.app.data.remote.RemoteFriendRequest
import com.cosplayjournal.app.data.remote.RemotePublicProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class FriendsUiState(
    val friends: List<RemotePublicProfile> = emptyList(),
    val requests: List<RemoteFriendRequest> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null
)

/** Amigos y solicitudes recibidas. */
class FriendsViewModel(private val repository: FriendsRepository) : ViewModel() {

    private val _state = MutableStateFlow(FriendsUiState())
    val state: StateFlow<FriendsUiState> = _state.asStateFlow()

    init {
        perform(successMessage = null) { load() }
    }

    fun refresh() {
        perform(successMessage = null) { load() }
    }

    fun sendRequest(nickname: String) {
        if (nickname.isBlank()) return
        perform(successMessage = "Solicitud enviada a " + nickname.trim()) {
            repository.sendRequest(nickname)
            load()
        }
    }

    fun accept(requestId: String) {
        perform(successMessage = "Ahora sois amigos") {
            repository.accept(requestId)
            load()
        }
    }

    fun reject(requestId: String) {
        perform(successMessage = null) {
            repository.reject(requestId)
            load()
        }
    }

    fun remove(nickname: String) {
        perform(successMessage = "Amistad eliminada") {
            repository.remove(nickname)
            load()
        }
    }

    private suspend fun load() {
        val friends = repository.friends()
        val requests = repository.incomingRequests()
        _state.value = _state.value.copy(friends = friends, requests = requests)
    }

    /** Ejecuta una acción de red y muestra el resultado o el error. */
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
        400 -> "Esa solicitud no es válida: ya existe una relación o es contigo mismo"
        401 -> "Inicia sesión para gestionar tus amigos"
        404 -> "No existe ese nickname o esa solicitud"
        else -> "No se pudo completar la operación (código " + code + ")"
    }
}

class FriendsViewModelFactory(private val repository: FriendsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
