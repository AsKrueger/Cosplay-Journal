package com.cosplayjournal.app.data.social

import com.cosplayjournal.app.BuildConfig
import com.cosplayjournal.app.data.llamada
import com.cosplayjournal.app.data.profile.Perfil
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.util.UUID

/** Perfil de un amigo tal como lo ve otro amigo. */
data class PerfilAmigo(
    val perfil: Perfil,
    val escaparate: List<FotoEscaparate>,
    val vaA: List<Long>,
)

interface SocialRepository {
    suspend fun buscar(texto: String): List<ResultadoBusqueda>
    suspend fun contactos(): List<Contacto>
    suspend fun enviarSolicitud(id: String): Relacion
    suspend fun enviarSolicitudPorCodigo(codigo: String): Relacion
    suspend fun responder(solicitante: String, aceptar: Boolean)
    suspend fun eliminar(id: String)
    suspend fun bloquear(id: String)
    suspend fun desbloquear(id: String)
    suspend fun bloqueados(): List<Bloqueado>
    suspend fun denunciar(id: String, motivo: String)
    suspend fun perfilAmigo(id: String, desde: LocalDate): PerfilAmigo
    suspend fun escaparate(userId: String): List<FotoEscaparate>
    suspend fun añadirAlEscaparate(fotoUri: String, eventoId: Long?, pie: String?)
    suspend fun quitarDelEscaparate(foto: FotoEscaparate)
    suspend fun cambiarAvatar(fotoUri: String): String
    suspend fun borrarMisArchivos()
}

/** URL de una imagen privada; la sesión se añade en el interceptor de Coil (AppContainer). */
fun urlPrivada(bucket: String, ruta: String?): String? =
    ruta?.let { "${BuildConfig.SUPABASE_URL}/storage/v1/object/authenticated/$bucket/$it" }

class SupabaseSocialRepository(
    private val client: SupabaseClient,
    private val compresor: CompresorImagenes,
    private val userId: () -> String?,
) : SocialRepository {

    private val yo get() = userId() ?: error("sin sesión")

    private suspend fun rpc(nombre: String, vararg params: Pair<String, String>) =
        client.postgrest.rpc(nombre, buildJsonObject { params.forEach { (k, v) -> put(k, v) } })

    override suspend fun buscar(texto: String) = llamada {
        rpc("search_profiles", "p_query" to texto).decodeList<ResultadoBusqueda>()
    }

    override suspend fun contactos(): List<Contacto> = llamada {
        val yo = yo
        val relaciones = client.from("friendships").select().decodeList<Amistad>()
        val otros = relaciones.map { if (it.solicitante == yo) it.destinatario else it.solicitante }
        if (otros.isEmpty()) return@llamada emptyList()
        val perfiles = client.from("profiles").select { filter { isIn("id", otros) } }.decodeList<Perfil>().associateBy { it.id }
        relaciones.mapNotNull { r ->
            val otro = if (r.solicitante == yo) r.destinatario else r.solicitante
            val p = perfiles[otro] ?: return@mapNotNull null
            val rel = when {
                r.status == "ACCEPTED" -> Relacion.FRIEND
                r.solicitante == yo -> Relacion.SENT
                else -> Relacion.RECEIVED
            }
            Contacto(p.id, p.nickname, p.avatarPath, rel)
        }.sortedBy { it.nickname.lowercase() }
    }

    override suspend fun enviarSolicitud(id: String) = llamada {
        Relacion.valueOf(if (rpc("send_friend_request", "p_target" to id).decodeAs<String>() == "ACCEPTED") "FRIEND" else "SENT")
    }

    override suspend fun enviarSolicitudPorCodigo(codigo: String) = llamada {
        Relacion.valueOf(if (rpc("send_friend_request_by_code", "p_code" to codigo).decodeAs<String>() == "ACCEPTED") "FRIEND" else "SENT")
    }

    override suspend fun responder(solicitante: String, aceptar: Boolean) = llamada {
        client.postgrest.rpc("respond_friend_request", buildJsonObject {
            put("p_requester", solicitante); put("p_accept", aceptar)
        })
        Unit
    }

    override suspend fun eliminar(id: String) = llamada { rpc("remove_friend", "p_other" to id); Unit }
    override suspend fun bloquear(id: String) = llamada { rpc("block_user", "p_target" to id); Unit }

    override suspend fun desbloquear(id: String) = llamada {
        client.from("blocks").delete { filter { eq("blocker_id", yo); eq("blocked_id", id) } }
        Unit
    }

    override suspend fun bloqueados() = llamada { client.postgrest.rpc("my_blocks").decodeList<Bloqueado>() }

    override suspend fun denunciar(id: String, motivo: String) = llamada {
        client.from("reports").insert(buildJsonObject {
            put("reporter_id", yo); put("reported_id", id); put("reason", motivo.take(1000))
        })
        Unit
    }

    override suspend fun perfilAmigo(id: String, desde: LocalDate) = llamada {
        val perfil = client.from("profiles").select { filter { eq("id", id) } }.decodeSingle<Perfil>()
        val vaA = client.from("attendance").select {
            filter { eq("user_id", id); eq("status", "GOING") }
        }.decodeList<AsistenciaAmigo>().map { it.eventoId }
        PerfilAmigo(perfil, escaparate(id), vaA)
    }

    override suspend fun escaparate(userId: String) = llamada {
        client.from("showcase_photos").select {
            filter { eq("user_id", userId) }
            order("position", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
        }.decodeList<FotoEscaparate>()
    }

    override suspend fun añadirAlEscaparate(fotoUri: String, eventoId: Long?, pie: String?) = llamada {
        val yo = yo
        val ruta = "$yo/${UUID.randomUUID()}.webp"
        client.storage.from("showcase").upload(ruta, compresor.comprimir(fotoUri)) { contentType = ContentType.parse("image/webp") }
        val posicion = escaparate(yo).maxOfOrNull { it.position + 1 } ?: 0
        try {
            client.from("showcase_photos").insert(NuevaFotoEscaparate(yo, ruta, pie?.take(140), eventoId, posicion))
        } catch (e: Exception) {
            // Escaparate lleno u otro error: no dejar el archivo huérfano
            runCatching { client.storage.from("showcase").delete(ruta) }
            throw e
        }
        Unit
    }

    override suspend fun quitarDelEscaparate(foto: FotoEscaparate) = llamada {
        client.from("showcase_photos").delete { filter { eq("id", foto.id) } }
        runCatching { client.storage.from("showcase").delete(foto.ruta) }
        Unit
    }

    override suspend fun cambiarAvatar(fotoUri: String) = llamada {
        val yo = yo
        val anterior = client.from("profiles").select { filter { eq("id", yo) } }.decodeSingle<Perfil>().avatarPath
        val ruta = "$yo/avatar-${System.currentTimeMillis()}.webp"
        client.storage.from("avatars").upload(ruta, compresor.comprimir(fotoUri, ladoMax = 512, maxBytes = 120_000)) {
            contentType = ContentType.parse("image/webp")
        }
        client.from("profiles").update(buildJsonObject { put("avatar_path", ruta) }) { filter { eq("id", yo) } }
        anterior?.let { runCatching { client.storage.from("avatars").delete(it) } }
        ruta
    }

    /** Antes de borrar la cuenta: quita avatar y escaparate del almacenamiento. */
    override suspend fun borrarMisArchivos() = llamada {
        val yo = yo
        for (bucket in listOf("avatars", "showcase")) {
            val rutas = client.storage.from(bucket).list(yo).map { "$yo/${it.name}" }
            if (rutas.isNotEmpty()) client.storage.from(bucket).delete(rutas)
        }
    }
}
