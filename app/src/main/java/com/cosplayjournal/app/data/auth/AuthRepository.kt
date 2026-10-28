package com.cosplayjournal.app.data.auth

import com.cosplayjournal.app.data.remote.AvatarBody
import com.cosplayjournal.app.data.remote.CosplayJournalApi
import com.cosplayjournal.app.data.remote.LoginBody
import com.cosplayjournal.app.data.remote.RegisterBody
import com.cosplayjournal.app.data.remote.RemoteOwnProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Registro, inicio de sesión y perfil propio. El token se guarda cifrado en el dispositivo. */
class AuthRepository(
    private val api: CosplayJournalApi,
    private val tokenStore: TokenStore
) {

    val isLoggedIn: Boolean get() = tokenStore.token() != null

    suspend fun register(nickname: String, email: String, password: String) = withContext(Dispatchers.IO) {
        val response = api.register(RegisterBody(nickname, email, password))
        if (!response.isSuccessful) throw IOException(messageFor(response.code()))
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        val token = api.login(LoginBody(email, password))
        tokenStore.save(token.accessToken)
    }

    fun logout() = tokenStore.clear()

    suspend fun myProfile(): RemoteOwnProfile = withContext(Dispatchers.IO) { api.myProfile() }

    suspend fun changeAvatar(avatarUrl: String?): RemoteOwnProfile = withContext(Dispatchers.IO) {
        api.changeAvatar(AvatarBody(avatarUrl?.takeIf { it.isNotBlank() }))
    }

    private fun messageFor(code: Int): String = when (code) {
        400 -> "Revisa los datos: el nickname debe tener entre 3 y 20 letras, números o guion bajo"
        409 -> "Ya existe una cuenta con ese email o nickname"
        else -> "No se pudo completar la operación (código $code)"
    }
}
