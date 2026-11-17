package com.cosplayjournal.app.data.auth

import com.cosplayjournal.app.data.AppException
import com.cosplayjournal.app.data.ErrorApp
import com.cosplayjournal.app.data.llamada
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Conectado(val userId: String) : EstadoSesion
}

enum class ResultadoRegistro { DENTRO, CONFIRMAR_EMAIL }

/** Errores de acceso que la pantalla explica con un texto concreto. */
enum class ErrorAcceso { CREDENCIALES, NO_CONFIRMADO, EMAIL_EXISTE, PASSWORD_DEBIL, CODIGO_INCORRECTO }

class AccesoException(val tipo: ErrorAcceso) : Exception(tipo.name)

interface AuthRepository {
    val estado: Flow<EstadoSesion>
    fun userIdActual(): String?
    suspend fun nicknameDisponible(nickname: String): Boolean
    suspend fun registrar(email: String, password: String, nickname: String): ResultadoRegistro
    suspend fun entrar(email: String, password: String)
    /** Envía un email con un código de 6 cifras para cambiar la contraseña. */
    suspend fun enviarCodigoRecuperacion(email: String)
    /** Comprueba el código, deja la sesión iniciada y guarda la nueva contraseña. */
    suspend fun restablecerPassword(email: String, codigo: String, nueva: String)
    suspend fun salir()
    suspend fun borrarCuenta()
}

class SupabaseAuthRepository(private val client: SupabaseClient) : AuthRepository {

    override val estado: Flow<EstadoSesion> = client.auth.sessionStatus.map { s ->
        when (s) {
            is SessionStatus.Initializing -> EstadoSesion.Cargando
            is SessionStatus.Authenticated -> s.session.user?.id?.let { EstadoSesion.Conectado(it) }
                ?: EstadoSesion.SinSesion
            // Sin red al renovar el token: se sigue dentro y se reintenta más tarde
            is SessionStatus.RefreshFailure -> userIdActual()?.let { EstadoSesion.Conectado(it) }
                ?: EstadoSesion.SinSesion
            is SessionStatus.NotAuthenticated -> EstadoSesion.SinSesion
        }
    }

    override fun userIdActual(): String? = client.auth.currentUserOrNull()?.id

    override suspend fun nicknameDisponible(nickname: String): Boolean = llamada {
        client.postgrest.rpc("nickname_available", buildJsonObject { put("p_nickname", nickname) })
            .decodeAs<Boolean>()
    }

    override suspend fun registrar(email: String, password: String, nickname: String): ResultadoRegistro =
        traducir {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject { put("nickname", nickname) }
            }
            if (client.auth.currentSessionOrNull() != null) ResultadoRegistro.DENTRO
            else ResultadoRegistro.CONFIRMAR_EMAIL
        }

    override suspend fun entrar(email: String, password: String) = traducir {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun enviarCodigoRecuperacion(email: String) = traducir {
        client.auth.resetPasswordForEmail(email)
    }

    override suspend fun restablecerPassword(email: String, codigo: String, nueva: String) = traducir {
        client.auth.verifyEmailOtp(type = OtpType.Email.RECOVERY, email = email, token = codigo)
        client.auth.updateUser { password = nueva }
        Unit
    }

    override suspend fun salir() {
        runCatching { client.auth.signOut() }
        client.auth.clearSession()
    }

    override suspend fun borrarCuenta() = llamada {
        client.postgrest.rpc("delete_my_account")
        salir()
    }

    private suspend fun <T> traducir(bloque: suspend () -> T): T = llamada {
        try {
            bloque()
        } catch (e: Exception) {
            val m = (e.message ?: "").lowercase()
            val tipo = when {
                "invalid login" in m || "invalid_credentials" in m -> ErrorAcceso.CREDENCIALES
                "not confirmed" in m || "email_not_confirmed" in m -> ErrorAcceso.NO_CONFIRMADO
                "already registered" in m || "user_already_exists" in m -> ErrorAcceso.EMAIL_EXISTE
                "password" in m && ("weak" in m || "should be" in m) -> ErrorAcceso.PASSWORD_DEBIL
                "otp_expired" in m || "expired or is invalid" in m -> ErrorAcceso.CODIGO_INCORRECTO
                else -> null
            }
            // Supabase limita los emails: "For security purposes, you can only request this after 42 seconds"
            if ("over_email_send_rate_limit" in m || "for security purposes" in m) {
                throw AppException(ErrorApp.Servidor("rate_limited"), e)
            }
            if (tipo != null) throw AppException(ErrorApp.Servidor(tipo.name), AccesoException(tipo)) else throw e
        }
    }
}
