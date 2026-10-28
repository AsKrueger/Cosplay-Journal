package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.auth.AuthRepository
import com.cosplayjournal.app.data.remote.RemoteOwnProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ProfileUiState(
    val loggedIn: Boolean = false,
    val profile: RemoteOwnProfile? = null,
    val busy: Boolean = false,
    val message: String? = null
)

/** Perfil del usuario: sesión, nickname y foto. */
class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState(loggedIn = authRepository.isLoggedIn))
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        if (authRepository.isLoggedIn) {
            perform { _state.value = _state.value.copy(profile = authRepository.myProfile()) }
        }
    }

    fun login(email: String, password: String) {
        perform {
            authRepository.login(email.trim(), password)
            _state.value = _state.value.copy(loggedIn = true, profile = authRepository.myProfile())
        }
    }

    fun register(nickname: String, email: String, password: String) {
        perform {
            authRepository.register(nickname.trim(), email.trim(), password)
            authRepository.login(email.trim(), password)
            _state.value = _state.value.copy(loggedIn = true, profile = authRepository.myProfile())
        }
    }

    fun changeAvatar(url: String) {
        perform {
            _state.value = _state.value.copy(profile = authRepository.changeAvatar(url.trim()))
        }
    }

    fun logout() {
        authRepository.logout()
        _state.value = ProfileUiState(loggedIn = false)
    }

    private fun perform(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            try {
                block()
                _state.value = _state.value.copy(busy = false)
            } catch (e: HttpException) {
                if (e.code() == 401) authRepository.logout()
                _state.value = _state.value.copy(
                    busy = false,
                    loggedIn = authRepository.isLoggedIn,
                    message = messageFor(e.code())
                )
            } catch (e: IOException) {
                _state.value = _state.value.copy(busy = false, message = "Sin conexión con el servidor")
            }
        }
    }

    private fun messageFor(code: Int): String = when (code) {
        400, 401 -> "Email o contraseña incorrectos"
        409 -> "Ya existe una cuenta con ese email o nickname"
        else -> "No se pudo completar la operación (código $code)"
    }
}

class ProfileViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
