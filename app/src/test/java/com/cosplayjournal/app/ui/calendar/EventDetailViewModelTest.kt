package com.cosplayjournal.app.ui.calendar

import com.cosplayjournal.app.ReglaDispatcher
import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.events.DaoFalso
import com.cosplayjournal.app.data.events.EventRepository
import com.cosplayjournal.app.data.events.EventoRemoto
import com.cosplayjournal.app.data.events.RemotoFalso
import com.cosplayjournal.app.reminders.Recordatorios
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class EventDetailViewModelTest {
    @get:Rule val regla = ReglaDispatcher()
    private val remoto = RemotoFalso().apply { eventos = listOf(EventoRemoto(7, "Mangafest", "2026-12-05", "2026-12-06")) }
    private val repo = EventRepository(DaoFalso(), remoto, userId = { "u1" }, hoy = { LocalDate.of(2026, 11, 17) })
    private val avisos = mockk<Avisos>(relaxed = true)

    @Test fun `marcar voy programa el aviso y volver a pulsar lo quita`() = runTest(regla.dispatcher) {
        repo.actualizar()
        val vm = EventDetailViewModel(7, repo, avisos)
        backgroundScope.launch { vm.evento.collect {} }
        advanceUntilIdle()

        vm.asistencia(Asistencia.GOING)
        advanceUntilIdle()
        assertEquals(Asistencia.GOING, vm.evento.value!!.estado.asistencia)
        verify { avisos.programar(7, "Mangafest", LocalDate.of(2026, 12, 5)) }

        vm.asistencia(Asistencia.GOING)
        advanceUntilIdle()
        assertNull(vm.evento.value!!.estado.asistencia)
        verify { avisos.cancelar(7) }
    }

    @Test fun `el aviso es la víspera a las 18 y no se programa si ya pasó`() {
        val inicio = LocalDate.of(2026, 12, 5)
        assertEquals(LocalDateTime.of(2026, 12, 4, 18, 0), Recordatorios.momento(inicio, LocalDateTime.of(2026, 11, 17, 10, 0)))
        assertNull(Recordatorios.momento(inicio, LocalDateTime.of(2026, 12, 4, 19, 0)))
    }
}
