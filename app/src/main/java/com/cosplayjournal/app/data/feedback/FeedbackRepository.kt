package com.cosplayjournal.app.data.feedback

import com.cosplayjournal.app.data.llamada
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class NuevoComentario(
    @SerialName("user_id") val userId: String?,
    val message: String,
    @SerialName("app_version") val appVersion: String,
    val locale: String,
)

interface FeedbackRepository {
    suspend fun enviar(userId: String?, mensaje: String, version: String, idioma: String)
}

/** Guarda el comentario en la tabla privada `feedback`; solo el autor la lee desde Supabase. */
class SupabaseFeedbackRepository(private val client: SupabaseClient) : FeedbackRepository {
    override suspend fun enviar(userId: String?, mensaje: String, version: String, idioma: String) = llamada {
        client.from("feedback").insert(NuevoComentario(userId, mensaje.trim().take(4000), version, idioma))
        Unit
    }
}
