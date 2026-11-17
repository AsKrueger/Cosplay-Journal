package com.cosplayjournal.app.data.events

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

/** DAO en memoria con el mismo comportamiento que las consultas de Room. */
class DaoFalso : EventDao {
    val eventosTabla = MutableStateFlow<Map<Long, EventoEntity>>(emptyMap())
    val estadosTabla = MutableStateFlow<Map<Long, MiEstadoEntity>>(emptyMap())

    override fun todos(): Flow<List<EventoConEstado>> = combine(eventosTabla, estadosTabla) { ev, es ->
        ev.values.sortedBy { it.inicio }.map { EventoConEstado(it, listOfNotNull(es[it.id])) }
    }
    override fun uno(id: Long) = todos().map { l -> l.firstOrNull { it.evento.id == id } }
    override suspend fun pendientes() = estadosTabla.value.values.filter { it.pendiente }
    override suspend fun estado(id: Long) = estadosTabla.value[id]
    override suspend fun guardarEventos(eventos: List<EventoEntity>) { eventosTabla.value += eventos.associateBy { it.id } }
    override suspend fun guardarEstado(estado: MiEstadoEntity) { estadosTabla.value += (estado.eventoId to estado) }
    override suspend fun guardarEstados(estados: List<MiEstadoEntity>) { estadosTabla.value += estados.associateBy { it.eventoId } }
    override suspend fun borrarEventosSalvo(ids: List<Long>) { eventosTabla.value = eventosTabla.value.filterKeys { it in ids } }
    override suspend fun borrarEstadosSincronizados() { estadosTabla.value = estadosTabla.value.filterValues { it.pendiente } }
    override suspend fun borrarEventos() { eventosTabla.value = emptyMap() }
    override suspend fun borrarEstados() { estadosTabla.value = emptyMap() }
}

class RemotoFalso : EventosRemotos {
    var sinRed = false
    var eventos = listOf<EventoRemoto>()
    val asistencias = mutableMapOf<Long, Asistencia>()
    val favoritos = mutableSetOf<Long>()
    var rango: Pair<LocalDate, LocalDate>? = null

    private fun red() { if (sinRed) throw IOException("sin red") }
    override suspend fun eventos(desde: LocalDate, hasta: LocalDate): List<EventoRemoto> { red(); rango = desde to hasta; return eventos }
    override suspend fun misAsistencias(userId: String) = asistencias.map { AsistenciaRemota(userId, it.key, it.value) }.also { red() }
    override suspend fun misFavoritos(userId: String) = favoritos.map { FavoritoRemoto(userId, it) }.also { red() }
    override suspend fun guardarAsistencia(userId: String, eventoId: Long, asistencia: Asistencia?) {
        red(); if (asistencia == null) asistencias.remove(eventoId) else asistencias[eventoId] = asistencia
    }
    override suspend fun guardarFavorito(userId: String, eventoId: Long, favorito: Boolean) {
        red(); if (favorito) favoritos += eventoId else favoritos -= eventoId
    }
}

class EventRepositoryTest {
    private val hoy = LocalDate.of(2026, 11, 17)
    private val dao = DaoFalso()
    private val remoto = RemotoFalso()
    private val repo = EventRepository(dao, remoto, userId = { "u1" }, hoy = { hoy })

    private fun remotoEvento(id: Long, inicio: String) = EventoRemoto(id, "Salón $id", inicio, inicio, provinceCode = "28 ")

    @Test fun `descarga un año atrás y uno adelante con el estado del usuario`() = runTest {
        remoto.eventos = listOf(remotoEvento(1, "2026-12-05"), remotoEvento(2, "2026-06-01"))
        remoto.asistencias[1] = Asistencia.GOING
        remoto.favoritos += 2
        repo.actualizar()

        assertEquals(LocalDate.of(2025, 11, 17) to LocalDate.of(2027, 11, 17), remoto.rango)
        val lista = repo.eventos.first()
        assertEquals(listOf(2L, 1L), lista.map { it.evento.id })
        assertEquals("28", lista[0].evento.provincia)
        assertEquals(Asistencia.GOING, lista[1].estado.asistencia)
        assertTrue(lista[0].estado.favorito)
    }

    @Test fun `quita los eventos que ya no están en el servidor`() = runTest {
        remoto.eventos = listOf(remotoEvento(1, "2026-12-05"), remotoEvento(2, "2026-12-06"))
        repo.actualizar()
        remoto.eventos = listOf(remotoEvento(2, "2026-12-06"))
        repo.actualizar()
        assertEquals(listOf(2L), repo.eventos.first().map { it.evento.id })
    }

    @Test fun `sin red el cambio queda pendiente y se sube al volver`() = runTest {
        remoto.eventos = listOf(remotoEvento(1, "2026-12-05"))
        repo.actualizar()

        remoto.sinRed = true
        repo.marcarAsistencia(1, Asistencia.GOING)
        assertEquals(Asistencia.GOING, repo.eventos.first()[0].estado.asistencia)
        assertTrue(dao.estado(1)!!.pendiente)
        assertTrue(remoto.asistencias.isEmpty())

        remoto.sinRed = false
        repo.actualizar()
        assertEquals(Asistencia.GOING, remoto.asistencias[1])
        assertFalse(dao.estado(1)!!.pendiente)
    }

    @Test fun `un cambio pendiente no se pisa con lo que viene del servidor`() = runTest {
        remoto.eventos = listOf(remotoEvento(1, "2026-12-05"))
        remoto.asistencias[1] = Asistencia.NOT_GOING
        repo.actualizar()
        dao.guardarEstado(MiEstadoEntity(1, Asistencia.GOING, pendiente = true))
        // La subida falla solo para la asistencia: simulamos que sigue pendiente
        remoto.sinRed = true
        runCatching { repo.actualizar() }
        assertEquals(Asistencia.GOING, dao.estado(1)!!.asistencia)
    }

    @Test fun `favorito alterna y se sincroniza`() = runTest {
        remoto.eventos = listOf(remotoEvento(1, "2026-12-05"))
        repo.actualizar()
        repo.cambiarFavorito(1)
        assertTrue(1L in remoto.favoritos)
        repo.cambiarFavorito(1)
        assertFalse(1L in remoto.favoritos)
    }
}
