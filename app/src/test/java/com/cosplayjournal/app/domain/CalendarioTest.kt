package com.cosplayjournal.app.domain

import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.events.EventoConEstado
import com.cosplayjournal.app.data.events.EventoEntity
import com.cosplayjournal.app.data.events.MiEstadoEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

private val HOY: LocalDate = LocalDate.of(2026, 11, 17)

fun evento(id: Long, inicio: LocalDate, fin: LocalDate = inicio, nombre: String = "Salón $id", ciudad: String? = null,
           provincia: String? = null, asistencia: Asistencia? = null, favorito: Boolean = false) =
    EventoConEstado(
        EventoEntity(id, nombre, inicio, fin, null, null, ciudad, provincia, null, null, null),
        listOf(MiEstadoEntity(id, asistencia, favorito)),
    )

class CalendarioTest {

    @Test fun `colores según fecha y asistencia`() {
        val ayer = HOY.minusDays(1)
        val mañana = HOY.plusDays(1)
        assertEquals(Tono.PASADO_NO_FUI, tono(ayer, ayer, null, HOY))
        assertEquals(Tono.PASADO_NO_FUI, tono(ayer, ayer, Asistencia.MISSED, HOY))
        assertEquals(Tono.PASADO_FUI, tono(ayer, ayer, Asistencia.WENT, HOY))
        assertEquals(Tono.PASADO_FUI, tono(ayer, ayer, Asistencia.GOING, HOY))
        assertEquals(Tono.EN_CURSO, tono(ayer, mañana, Asistencia.GOING, HOY))
        assertEquals(Tono.FUTURO_SIN_MARCAR, tono(HOY, HOY, null, HOY))
        assertEquals(Tono.FUTURO_VOY, tono(mañana, mañana, Asistencia.GOING, HOY))
        assertEquals(Tono.FUTURO_SIN_MARCAR, tono(mañana, mañana, Asistencia.NOT_GOING, HOY))
    }

    @Test fun `opciones de asistencia segun la fecha`() {
        assertEquals(listOf(Asistencia.GOING, Asistencia.NOT_GOING), opcionesAsistencia(HOY, HOY))
        assertEquals(listOf(Asistencia.WENT, Asistencia.MISSED), opcionesAsistencia(HOY.minusDays(1), HOY))
    }

    private val lista = listOf(
        evento(1, HOY.minusDays(30), nombre = "Japan Weekend", ciudad = "Madrid", provincia = "28", asistencia = Asistencia.WENT),
        evento(2, HOY.plusDays(10), nombre = "Salón del Manga", ciudad = "Málaga", provincia = "29", favorito = true),
        evento(3, HOY.plusDays(3), nombre = "Expocómic", ciudad = "Madrid", provincia = "28", asistencia = Asistencia.GOING),
        evento(4, HOY.plusDays(5), nombre = "Freak Fest", ciudad = "Toledo", provincia = "45"),
        evento(5, HOY.minusDays(1), HOY.plusDays(1), nombre = "En curso", provincia = "08"),
    )

    @Test fun `próximos ordenados sin pasados y con los que están en curso`() {
        assertEquals(listOf(5L, 3L, 4L, 2L), filtrar(lista, Consulta(), HOY).map { it.evento.id })
    }

    @Test fun `pasados del más reciente al más antiguo`() {
        assertEquals(listOf(1L), filtrar(lista, Consulta(pasados = true), HOY).map { it.evento.id })
    }

    @Test fun `búsqueda sin acentos ni mayúsculas por nombre, ciudad o provincia`() {
        assertEquals(listOf(2L), filtrar(lista, Consulta(texto = "MALAGA"), HOY).map { it.evento.id })
        assertEquals(listOf(3L), filtrar(lista, Consulta(texto = "expocomic"), HOY).map { it.evento.id })
        assertEquals(listOf(5L), filtrar(lista, Consulta(texto = "barcelona"), HOY).map { it.evento.id })
    }

    @Test fun `filtros favoritos, voy y fui`() {
        assertEquals(listOf(2L), filtrar(lista, Consulta(Filtro.FAVORITOS), HOY).map { it.evento.id })
        assertEquals(listOf(3L), filtrar(lista, Consulta(Filtro.VOY), HOY).map { it.evento.id })
        assertEquals(listOf(1L), filtrar(lista, Consulta(Filtro.FUI, pasados = true), HOY).map { it.evento.id })
    }

    @Test fun `cercanos dentro del radio y del más cercano al más lejano`() {
        val r = filtrar(lista, Consulta(Filtro.CERCANOS, miProvincia = "45"), HOY).map { it.evento.id }
        assertEquals(listOf(4L, 3L), r) // Toledo y Madrid; Málaga y Barcelona quedan lejos
        assertTrue(filtrar(lista, Consulta(Filtro.CERCANOS), HOY).isEmpty())
    }

    @Test fun `vista de mes incluye pasados y futuros`() {
        assertEquals(5, filtrar(lista, Consulta(separarPasados = false), HOY).size)
        assertEquals(listOf(5L), eventosDelDia(lista, HOY).map { it.evento.id })
    }

    @Test fun `provincias por coordenadas y distancias`() {
        assertEquals("28", Provincias.masCercana(40.45, -3.69))
        assertEquals("08", Provincias.masCercana(41.40, 2.15))
        val madridToledo = Provincias.distanciaKm("28", "45")
        assertTrue(madridToledo in 50.0..90.0)
        assertEquals(Double.MAX_VALUE, Provincias.distanciaKm("28", "99"), 0.0)
    }
}
