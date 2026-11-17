package com.cosplayjournal.app.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.AppException
import com.cosplayjournal.app.data.auth.AccesoException
import com.cosplayjournal.app.data.auth.AuthRepository
import com.cosplayjournal.app.data.auth.ErrorAcceso
import com.cosplayjournal.app.data.auth.ResultadoRegistro
import com.cosplayjournal.app.data.clasificar
import com.cosplayjournal.app.ui.common.texto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object Reglas {
    private val NICKNAME = Regex("^[A-Za-z0-9_.]{3,24}$")
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    fun nicknameValido(n: String) = NICKNAME.matches(n)
    fun emailValido(e: String) = EMAIL.matches(e.trim())
    fun passwordValida(p: String) = p.length >= 8
    fun codigoValido(c: String) = c.length in 6..10 && c.all(Char::isDigit)
}

enum class EstadoNickname { VACIO, INVALIDO, COMPROBANDO, LIBRE, OCUPADO }

/** Pasos de «He olvidado mi contraseña»: pedir el código y escribirlo con la nueva contraseña. */
enum class PasoRecuperar { EMAIL, CODIGO }

data class AuthUi(
    val registro: Boolean = false,
    val email: String = "",
    val password: String = "",
    val nickname: String = "",
    val estadoNickname: EstadoNickname = EstadoNickname.VACIO,
    val mayorDe14: Boolean = false,
    val aceptaPrivacidad: Boolean = false,
    val enviando: Boolean = false,
    @StringRes val error: Int? = null,
    val confirmarEmail: Boolean = false,
    val recuperar: PasoRecuperar? = null,
    val codigo: String = "",
) {
    val puedeRecuperar: Boolean
        get() = !enviando && Reglas.emailValido(email) && when (recuperar) {
            PasoRecuperar.CODIGO -> Reglas.codigoValido(codigo) && Reglas.passwordValida(password)
            else -> true
        }

    val puedeEnviar: Boolean
        get() = !enviando && Reglas.emailValido(email) && Reglas.passwordValida(password) &&
            (!registro || (estadoNickname == EstadoNickname.LIBRE && mayorDe14 && aceptaPrivacidad))
}

class AuthViewModel(private val auth: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUi())
    val ui: StateFlow<AuthUi> = _ui.asStateFlow()
    private var comprobacion: Job? = null

    fun cambiarModo() = _ui.update { AuthUi(registro = !it.registro, email = it.email) }
    fun irAEntrar() = _ui.update { AuthUi(email = it.email) }
    fun email(v: String) = _ui.update { it.copy(email = v.trim(), error = null) }
    fun password(v: String) = _ui.update { it.copy(password = v, error = null) }
    fun empezarRecuperacion() = _ui.update { AuthUi(email = it.email, recuperar = PasoRecuperar.EMAIL) }
    fun codigo(v: String) = _ui.update { it.copy(codigo = v.filter(Char::isDigit).take(10), error = null) }

    fun recuperar() {
        val s = _ui.value
        if (!s.puedeRecuperar) return
        _ui.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            try {
                if (s.recuperar == PasoRecuperar.CODIGO) {
                    // Al acabar queda la sesión iniciada y la app entra sola
                    auth.restablecerPassword(s.email, s.codigo, s.password)
                    _ui.update { it.copy(enviando = false) }
                } else {
                    auth.enviarCodigoRecuperacion(s.email)
                    _ui.update { it.copy(enviando = false, recuperar = PasoRecuperar.CODIGO, password = "") }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(enviando = false, error = textoError(e)) }
            }
        }
    }

    fun mayorDe14(v: Boolean) = _ui.update { it.copy(mayorDe14 = v) }
    fun aceptaPrivacidad(v: Boolean) = _ui.update { it.copy(aceptaPrivacidad = v) }

    fun nickname(v: String) {
        val n = v.trim()
        comprobacion?.cancel()
        val estado = when {
            n.isEmpty() -> EstadoNickname.VACIO
            !Reglas.nicknameValido(n) -> EstadoNickname.INVALIDO
            else -> EstadoNickname.COMPROBANDO
        }
        _ui.update { it.copy(nickname = n, estadoNickname = estado, error = null) }
        if (estado != EstadoNickname.COMPROBANDO) return
        comprobacion = viewModelScope.launch {
            delay(400) // espera a que deje de escribir
            val libre = runCatching { auth.nicknameDisponible(n) }
            _ui.update {
                if (it.nickname != n) it
                else libre.fold(
                    { ok -> it.copy(estadoNickname = if (ok) EstadoNickname.LIBRE else EstadoNickname.OCUPADO) },
                    { e -> it.copy(estadoNickname = EstadoNickname.VACIO, error = clasificar(e).texto()) },
                )
            }
        }
    }

    fun enviar() {
        val s = _ui.value
        if (!s.puedeEnviar) return
        _ui.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            try {
                if (s.registro) {
                    val r = auth.registrar(s.email, s.password, s.nickname)
                    _ui.update {
                        if (r == ResultadoRegistro.CONFIRMAR_EMAIL) AuthUi(email = s.email, confirmarEmail = true)
                        else it.copy(enviando = false)
                    }
                } else {
                    auth.entrar(s.email, s.password)
                    _ui.update { it.copy(enviando = false) }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(enviando = false, error = textoError(e)) }
            }
        }
    }

    @StringRes
    private fun textoError(e: Exception): Int {
        val acceso = e as? AccesoException ?: (e as? AppException)?.cause as? AccesoException
        return when (acceso?.tipo) {
            ErrorAcceso.CREDENCIALES -> R.string.auth_error_credenciales
            ErrorAcceso.NO_CONFIRMADO -> R.string.auth_error_no_confirmado
            ErrorAcceso.EMAIL_EXISTE -> R.string.auth_error_existe
            ErrorAcceso.PASSWORD_DEBIL -> R.string.auth_error_password_debil
            ErrorAcceso.CODIGO_INCORRECTO -> R.string.auth_error_codigo
            null -> clasificar(e).texto()
        }
    }
}
