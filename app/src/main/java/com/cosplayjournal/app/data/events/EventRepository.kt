package com.cosplayjournal.app.data.events

import com.cosplayjournal.app.data.llamada
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Acceso a Supabase separado para poder probar el repositorio sin red. */
interface EventosRemotos {
    suspend fun eventos(desde: LocalDate, hasta: LocalDate): List<EventoRemoto>
    suspend fun misAsistencias(userId: String): List<AsistenciaRemota>
    suspend fun misFavoritos(userId: String): List<FavoritoRemoto>
    suspend fun guardarAsistencia(userId: String, eventoId: Long, asistencia: Asistencia?)
    suspend fun guardarFavorito(userId: String, eventoId: Long, favorito: Boolean)
}

class SupabaseEventosRemotos(private val client: SupabaseClient) : EventosRemotos {
    override suspend fun eventos(desde: LocalDate, hasta: LocalDate) =
        client.from("events").select {
            filter {
                gte("end_date", desde.toString())
                lte("start_date", hasta.toString())
            }
            order("start_date", Order.ASCENDING)
        }.decodeList<EventoRemoto>()

    override suspend fun misAsistencias(userId: String) =
        client.from("attendance").select { filter { eq("user_id", userId) } }.decodeList<AsistenciaRemota>()

    override suspend fun misFavoritos(userId: String) =
        client.from("favorites").select { filter { eq("user_id", userId) } }.decodeList<FavoritoRemoto>()

    override suspend fun guardarAsistencia(userId: String, eventoId: Long, asistencia: Asistencia?) {
        if (asistencia == null) {
            client.from("attendance").delete { filter { eq("user_id", userId); eq("event_id", eventoId) } }
        } else {
            client.from("attendance").upsert(AsistenciaRemota(userId, eventoId, asistencia))
        }
    }

    override suspend fun guardarFavorito(userId: String, eventoId: Long, favorito: Boolean) {
        if (favorito) client.from("favorites").upsert(FavoritoRemoto(userId, eventoId))
        else client.from("favorites").delete { filter { eq("user_id", userId); eq("event_id", eventoId) } }
    }
}

/**
 * Calendario con caché local. Lo que el usuario marca se guarda primero en el móvil y se sube
 * después; si no hay red queda pendiente y se reintenta en la siguiente actualización.
 */
class EventRepository(
    private val dao: EventDao,
    private val remoto: EventosRemotos,
    private val userId: () -> String?,
    private val hoy: () -> LocalDate = LocalDate::now,
) {
    val eventos: Flow<List<EventoConEstado>> = dao.todos()
    fun evento(id: Long): Flow<EventoConEstado?> = dao.uno(id)

    /** Descarga los eventos de un año atrás a un año adelante y el estado del usuario. */
    suspend fun actualizar() = llamada {
        subirPendientes()
        val d = hoy()
        val eventos = remoto.eventos(d.minusYears(1), d.plusYears(1)).map { it.aEntidad() }
        dao.guardarEventos(eventos)
        dao.borrarEventosSalvo(eventos.map { it.id })

        val uid = userId() ?: return@llamada
        val asistencias = remoto.misAsistencias(uid).associate { it.eventId to it.status }
        val favoritos = remoto.misFavoritos(uid).map { it.eventId }.toSet()
        val pendientes = dao.pendientes().map { it.eventoId }.toSet()
        dao.borrarEstadosSincronizados()
        dao.guardarEstados(
            (asistencias.keys + favoritos).filter { it !in pendientes }
                .map { MiEstadoEntity(it, asistencias[it], it in favoritos) }
        )
    }

    suspend fun marcarAsistencia(eventoId: Long, asistencia: Asistencia?) {
        val actual = dao.estado(eventoId) ?: MiEstadoEntity(eventoId)
        guardarYSubir(actual.copy(asistencia = asistencia))
    }

    suspend fun cambiarFavorito(eventoId: Long) {
        val actual = dao.estado(eventoId) ?: MiEstadoEntity(eventoId)
        guardarYSubir(actual.copy(favorito = !actual.favorito))
    }

    private suspend fun guardarYSubir(estado: MiEstadoEntity) {
        dao.guardarEstado(estado.copy(pendiente = true))
        runCatching { subir(estado) }.onSuccess { dao.guardarEstado(estado.copy(pendiente = false)) }
    }

    private suspend fun subirPendientes() {
        dao.pendientes().forEach { e ->
            runCatching { subir(e) }.onSuccess { dao.guardarEstado(e.copy(pendiente = false)) }
        }
    }

    private suspend fun subir(e: MiEstadoEntity) {
        val uid = userId() ?: error("sin sesión")
        remoto.guardarAsistencia(uid, e.eventoId, e.asistencia)
        remoto.guardarFavorito(uid, e.eventoId, e.favorito)
    }

    suspend fun borrarLocal() {
        dao.borrarEventos()
        dao.borrarEstados()
    }
}
