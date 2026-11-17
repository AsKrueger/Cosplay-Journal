package com.cosplayjournal.app.data.events

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

/** Lo que el usuario dice de un evento. Se guarda igual en Supabase (`attendance.status`). */
enum class Asistencia { GOING, NOT_GOING, WENT, MISSED }

@Entity(tableName = "eventos", indices = [Index("inicio")])
data class EventoEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val inicio: LocalDate,
    val fin: LocalDate,
    val lugar: String?,
    val direccion: String?,
    val ciudad: String?,
    val provincia: String?,
    val precio: String?,
    val cartel: String?,
    val web: String?,
)

/** Estado propio de un evento. `pendiente` = cambiado sin conexión, falta subirlo. */
@Entity(tableName = "mi_estado")
data class MiEstadoEntity(
    @PrimaryKey @ColumnInfo(name = "evento_id") val eventoId: Long,
    val asistencia: Asistencia? = null,
    val favorito: Boolean = false,
    val pendiente: Boolean = false,
)

data class EventoConEstado(
    @Embedded val evento: EventoEntity,
    @Relation(parentColumn = "id", entityColumn = "evento_id") val estados: List<MiEstadoEntity>,
) {
    val estado: MiEstadoEntity get() = estados.firstOrNull() ?: MiEstadoEntity(evento.id)
}

// ------------------------------------------------------------------ Supabase

@Serializable
data class EventoRemoto(
    val id: Long,
    val name: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val venue: String? = null,
    val address: String? = null,
    val city: String? = null,
    @SerialName("province_code") val provinceCode: String? = null,
    val price: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val website: String? = null,
) {
    fun aEntidad() = EventoEntity(
        id, name, LocalDate.parse(startDate), LocalDate.parse(endDate), venue, address, city,
        provinceCode?.trim(), price, imageUrl, website,
    )
}

@Serializable
data class AsistenciaRemota(
    @SerialName("user_id") val userId: String,
    @SerialName("event_id") val eventId: Long,
    val status: Asistencia,
)

@Serializable
data class FavoritoRemoto(
    @SerialName("user_id") val userId: String,
    @SerialName("event_id") val eventId: Long,
)
