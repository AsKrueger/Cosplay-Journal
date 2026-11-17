package com.cosplayjournal.app.data.events

import com.cosplayjournal.app.data.llamada
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.OffsetDateTime

/** Acceso a Supabase separado para poder probar el repositorio sin red. */
interface EventosRemotos {
    /** Eventos del rango; con [cambiadosDesde], solo los modificados después de ese `updated_at`. */
    suspend fun eventos(desde: LocalDate, hasta: LocalDate, cambiadosDesde: String? = null): List<EventoRemoto>
    suspend fun misAsistencias(userId: String): List<AsistenciaRemota>
    suspend fun misFavoritos(userId: String): List<FavoritoRemoto>
    suspend fun guardarAsistencia(userId: String, eventoId: Long, asistencia: Asistencia?)
    suspend fun guardarFavorito(userId: String, eventoId: Long, favorito: Boolean)
}

class SupabaseEventosRemotos(private val client: SupabaseClient) : EventosRemotos {
    override suspend fun eventos(desde: LocalDate, hasta: LocalDate, cambiadosDesde: String?) =
        client.from("events").select {
            filter {
                gte("end_date", desde.toString())
                lte("start_date", hasta.toString())
                // La base pone a cada fila su propia hora (clock_timestamp), así que basta con gt
                if (cambiadosDesde != null) gt("updated_at", cambiadosDesde)
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
    private val marcas: MarcaSincronizacion = MarcaEnMemoria(),
    private val hoy: () -> LocalDate = LocalDate::now,
) {
    val eventos: Flow<List<EventoConEstado>> = dao.todos()
    fun evento(id: Long): Flow<EventoConEstado?> = dao.uno(id)

    /**
     * Trae los eventos de un año atrás a un año adelante y el estado del usuario.
     * Normalmente solo pide los salones que han cambiado (unos pocos bytes); una vez por semana
     * lo descarga todo para quitar los que el servidor haya borrado. Así el tráfico gratuito de
     * Supabase alcanza para muchísimos más usuarios.
     */
    suspend fun actualizar() = llamada {
        subirPendientes()
        val d = hoy()
        val marca = marcas.leer()
        val completa = marca == null || !d.isBefore(marca.ultimaCompleta.plusDays(DIAS_ENTRE_COMPLETAS)) ||
            dao.cuantos() == 0
        val remotos = remoto.eventos(d.minusYears(1), d.plusYears(1), if (completa) null else marca?.cambiosDesde)
        val eventos = remotos.map { it.aEntidad() }
        dao.guardarEventos(eventos)
        if (completa) dao.borrarEventosSalvo(eventos.map { it.id })
        else dao.borrarTerminadosAntesDe(d.minusYears(1))
        val masReciente = remotos.mapNotNull { it.updatedAt }.maxByOrNull { OffsetDateTime.parse(it) }
        marcas.guardar(
            Marca(
                cambiosDesde = masReciente ?: marca?.cambiosDesde,
                ultimaCompleta = if (completa) d else marca!!.ultimaCompleta,
            )
        )

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
        marcas.borrar()
    }

    private companion object {
        const val DIAS_ENTRE_COMPLETAS = 7L
    }
}
