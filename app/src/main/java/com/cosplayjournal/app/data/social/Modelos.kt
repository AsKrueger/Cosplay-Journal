package com.cosplayjournal.app.data.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Relacion { NONE, SENT, RECEIVED, FRIEND }

@Serializable
data class ResultadoBusqueda(
    val id: String,
    val nickname: String,
    @SerialName("avatar_path") val avatarPath: String? = null,
    val relation: Relacion = Relacion.NONE,
)

@Serializable
data class Amistad(
    @SerialName("requester_id") val solicitante: String,
    @SerialName("addressee_id") val destinatario: String,
    val status: String,
)

/** Persona en la lista de amigos o de solicitudes. */
data class Contacto(val id: String, val nickname: String, val avatarPath: String?, val relacion: Relacion)

@Serializable
data class Bloqueado(
    @SerialName("blocked_id") val id: String,
    val nickname: String,
)

@Serializable
data class FotoEscaparate(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("image_path") val ruta: String,
    val caption: String? = null,
    @SerialName("event_id") val eventoId: Long? = null,
    val position: Int = 0,
)

@Serializable
data class NuevaFotoEscaparate(
    @SerialName("user_id") val userId: String,
    @SerialName("image_path") val ruta: String,
    val caption: String?,
    @SerialName("event_id") val eventoId: Long?,
    val position: Int,
)

@Serializable
data class AsistenciaAmigo(
    @SerialName("event_id") val eventoId: Long,
    val status: String,
)
