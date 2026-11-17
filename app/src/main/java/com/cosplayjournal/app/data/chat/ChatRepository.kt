package com.cosplayjournal.app.data.chat

import com.cosplayjournal.app.data.llamada
import com.cosplayjournal.app.data.profile.Perfil
import com.cosplayjournal.app.data.social.CompresorImagenes
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.util.UUID

@Serializable
data class ResumenChat(
    val id: String,
    val kind: String,
    val title: String? = null,
    @SerialName("event_id") val eventoId: Long? = null,
    @SerialName("other_user_id") val otroId: String? = null,
    @SerialName("other_nickname") val otroNickname: String? = null,
    @SerialName("other_avatar_path") val otroAvatar: String? = null,
    @SerialName("last_body") val ultimoTexto: String? = null,
    @SerialName("last_has_image") val ultimoEsFoto: Boolean? = null,
    @SerialName("last_at") val ultimaFecha: String? = null,
    val unread: Long = 0,
) {
    val esGrupo get() = kind == "GROUP"
    val nombre get() = if (esGrupo) title.orEmpty() else otroNickname.orEmpty()
    val instante: Instant? get() = ultimaFecha?.let { runCatching { Instant.parse(it) }.getOrNull() }
}

@Serializable
data class Mensaje(
    val id: Long,
    @SerialName("conversation_id") val chatId: String,
    @SerialName("sender_id") val autor: String? = null,
    val body: String? = null,
    @SerialName("image_path") val foto: String? = null,
    @SerialName("created_at") val fecha: String,
) {
    val instante: Instant get() = runCatching { Instant.parse(fecha) }.getOrDefault(Instant.EPOCH)
}

@Serializable
private data class NuevoMensaje(
    @SerialName("conversation_id") val chatId: String,
    @SerialName("sender_id") val autor: String,
    val body: String?,
    @SerialName("image_path") val foto: String?,
)

@Serializable
private data class Miembro(@SerialName("user_id") val userId: String, val role: String)

data class InfoChat(val miembros: List<Perfil>, val dueño: String?)

interface ChatRepository {
    suspend fun misChats(): List<ResumenChat>
    suspend fun abrirDirecto(amigoId: String): String
    suspend fun crearGrupo(titulo: String, miembros: List<String>, eventoId: Long?): String
    suspend fun mensajes(chatId: String, antesDe: String? = null, limite: Int = 50): List<Mensaje>
    suspend fun enviarTexto(chatId: String, texto: String)
    suspend fun enviarFoto(chatId: String, fotoUri: String)
    suspend fun marcarLeido(chatId: String)
    suspend fun info(chatId: String): InfoChat
    suspend fun añadirMiembro(chatId: String, userId: String)
    suspend fun salir(chatId: String)
    /** Mensajes nuevos en tiempo real mientras la pantalla del chat está abierta. */
    fun nuevos(chatId: String): Flow<Mensaje>
}

class SupabaseChatRepository(
    private val client: SupabaseClient,
    private val compresor: CompresorImagenes,
    private val userId: () -> String?,
) : ChatRepository {

    private val yo get() = userId() ?: error("sin sesión")

    override suspend fun misChats() = llamada { client.postgrest.rpc("my_conversations").decodeList<ResumenChat>() }

    override suspend fun abrirDirecto(amigoId: String) = llamada {
        client.postgrest.rpc("get_or_create_direct", buildJsonObject { put("p_friend", amigoId) }).decodeAs<String>()
    }

    override suspend fun crearGrupo(titulo: String, miembros: List<String>, eventoId: Long?) = llamada {
        client.postgrest.rpc("create_group", buildJsonObject {
            put("p_title", titulo.trim().take(60))
            put("p_members", JsonArray(miembros.map(::JsonPrimitive)))
            eventoId?.let { put("p_event", it) }
        }).decodeAs<String>()
    }

    override suspend fun mensajes(chatId: String, antesDe: String?, limite: Int) = llamada {
        client.from("messages").select {
            filter {
                eq("conversation_id", chatId)
                antesDe?.let { lt("created_at", it) }
            }
            order("created_at", Order.DESCENDING)
            limit(limite.toLong())
        }.decodeList<Mensaje>()
    }

    override suspend fun enviarTexto(chatId: String, texto: String) = llamada {
        client.from("messages").insert(NuevoMensaje(chatId, yo, texto.trim().take(2000), null))
        Unit
    }

    override suspend fun enviarFoto(chatId: String, fotoUri: String) = llamada {
        val ruta = "$chatId/${UUID.randomUUID()}.webp"
        client.storage.from("chat").upload(ruta, compresor.comprimir(fotoUri)) { contentType = ContentType.parse("image/webp") }
        client.from("messages").insert(NuevoMensaje(chatId, yo, null, ruta))
        Unit
    }

    override suspend fun marcarLeido(chatId: String) {
        runCatching { client.postgrest.rpc("mark_read", buildJsonObject { put("p_conversation", chatId) }) }
    }

    override suspend fun info(chatId: String) = llamada {
        val miembros = client.from("conversation_members").select { filter { eq("conversation_id", chatId) } }.decodeList<Miembro>()
        val perfiles = client.from("profiles").select { filter { isIn("id", miembros.map { it.userId }) } }.decodeList<Perfil>()
        InfoChat(perfiles.sortedBy { it.nickname.lowercase() }, miembros.firstOrNull { it.role == "OWNER" }?.userId)
    }

    override suspend fun añadirMiembro(chatId: String, userId: String) = llamada {
        client.postgrest.rpc("add_group_member", buildJsonObject { put("p_conversation", chatId); put("p_user", userId) })
        Unit
    }

    override suspend fun salir(chatId: String) = llamada {
        client.postgrest.rpc("leave_conversation", buildJsonObject { put("p_conversation", chatId) })
        Unit
    }

    override fun nuevos(chatId: String): Flow<Mensaje> = callbackFlow {
        val canal = client.channel("chat-$chatId")
        val cambios = canal.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter("conversation_id", FilterOperator.EQ, chatId)
        }
        val trabajo = launch { cambios.collect { trySend(it.decodeRecord<Mensaje>()) } }
        canal.subscribe()
        awaitClose {
            trabajo.cancel()
            CoroutineScope(Dispatchers.IO).launch { runCatching { client.realtime.removeChannel(canal) } }
        }
    }
}
