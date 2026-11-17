package com.cosplayjournal.app.data

import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import java.io.IOException

/** Errores que la interfaz sabe explicar al usuario. */
sealed interface ErrorApp {
    data object SinConexion : ErrorApp
    data class Servidor(val codigo: String?) : ErrorApp
    data object Desconocido : ErrorApp
}

class AppException(val error: ErrorApp, cause: Throwable? = null) : Exception(error.toString(), cause)

/** Ejecuta una llamada remota y traduce las excepciones a [AppException]. */
suspend fun <T> llamada(bloque: suspend () -> T): T = try {
    bloque()
} catch (e: CancellationException) {
    throw e
} catch (e: AppException) {
    throw e
} catch (e: Exception) {
    throw AppException(clasificar(e), e)
}

fun clasificar(e: Throwable): ErrorApp = when {
    e is AppException -> e.error
    e is IOException || e is HttpRequestException || e is HttpRequestTimeoutException ||
        e.cause is IOException -> ErrorApp.SinConexion
    // Las funciones SQL lanzan códigos como 'not_friends' o 'showcase_full'
    e.message != null -> ErrorApp.Servidor(codigoDe(e.message!!))
    else -> ErrorApp.Desconocido
}

private val CODIGOS = listOf(
    "not_friends", "user_not_found", "request_not_found", "showcase_full", "conversation_not_found",
    "cannot_friend_self", "cannot_block_self", "not_authenticated",
)

private fun codigoDe(mensaje: String): String? = CODIGOS.firstOrNull { mensaje.contains(it) }
