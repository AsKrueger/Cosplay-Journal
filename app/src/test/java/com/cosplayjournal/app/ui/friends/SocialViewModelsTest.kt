package com.cosplayjournal.app.ui.friends

import com.cosplayjournal.app.R
import com.cosplayjournal.app.ReglaDispatcher
import com.cosplayjournal.app.data.AppException
import com.cosplayjournal.app.data.ErrorApp
import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.profile.Perfil
import com.cosplayjournal.app.data.profile.ProfileRepository
import com.cosplayjournal.app.data.social.Contacto
import com.cosplayjournal.app.data.social.PerfilAmigo
import com.cosplayjournal.app.data.social.Relacion
import com.cosplayjournal.app.data.social.ResultadoBusqueda
import com.cosplayjournal.app.data.social.SocialRepository
import com.cosplayjournal.app.domain.evento
import com.cosplayjournal.app.ui.profile.ProfileViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class SocialViewModelsTest {
    @get:Rule val regla = ReglaDispatcher()
    private val social = mockk<SocialRepository>(relaxed = true)
    private val hoy = LocalDate.of(2026, 11, 17)

    @Test fun `separa solicitudes recibidas, enviadas y amigos`() = runTest(regla.dispatcher) {
        coEvery { social.contactos() } returns listOf(
            Contacto("1", "ana", null, Relacion.RECEIVED),
            Contacto("2", "bea", null, Relacion.SENT),
            Contacto("3", "carla", null, Relacion.FRIEND),
        )
        val vm = FriendsViewModel(social)
        advanceUntilIdle()
        assertEquals(listOf("ana"), vm.ui.value.recibidas.map { it.nickname })
        assertEquals(listOf("bea"), vm.ui.value.enviadas.map { it.nickname })
        assertEquals(listOf("carla"), vm.ui.value.amigos.map { it.nickname })
    }

    @Test fun `busca a partir de 3 letras y marca la solicitud enviada`() = runTest(regla.dispatcher) {
        coEvery { social.buscar("kir") } returns listOf(ResultadoBusqueda("9", "kira"))
        coEvery { social.enviarSolicitud("9") } returns Relacion.SENT
        val vm = FriendsViewModel(social)
        vm.buscar("ki")
        advanceUntilIdle()
        assertNull(vm.ui.value.resultados)
        vm.buscar("kir")
        advanceUntilIdle()
        assertEquals(Relacion.NONE, vm.ui.value.resultados!!.single().relation)
        vm.enviar("9")
        advanceUntilIdle()
        assertEquals(Relacion.SENT, vm.ui.value.resultados!!.single().relation)
        assertEquals(R.string.ami_solicitud_enviada, vm.ui.value.mensaje)
    }

    @Test fun `un código que no existe muestra su mensaje`() = runTest(regla.dispatcher) {
        coEvery { social.enviarSolicitudPorCodigo(any()) } throws AppException(ErrorApp.Servidor("user_not_found"))
        val vm = FriendsViewModel(social)
        vm.enviarPorCodigo("ABCD1234")
        advanceUntilIdle()
        assertEquals(R.string.ami_no_encontrado, vm.ui.value.mensaje)
    }

    @Test fun `bloquear desde el perfil de un amigo cierra la pantalla`() = runTest(regla.dispatcher) {
        coEvery { social.perfilAmigo("3", any()) } returns PerfilAmigo(Perfil("3", "carla"), emptyList(), listOf(1, 2))
        val eventos = MutableStateFlow(listOf(
            evento(1, hoy.plusDays(3)),
            evento(2, hoy.minusDays(10)),
        ))
        val vm = FriendProfileViewModel("3", social, eventos, hoy = { hoy })
        backgroundScope.launch { vm.ui.collect {} }
        advanceUntilIdle()
        assertEquals(listOf(1L), vm.ui.value.vaA.map { it.evento.id }) // solo los próximos
        vm.bloquear()
        advanceUntilIdle()
        coVerify { social.bloquear("3") }
        assertTrue(vm.ui.value.cerrar)
    }

    @Test fun `escaparate lleno muestra un aviso claro`() = runTest(regla.dispatcher) {
        val perfiles = mockk<ProfileRepository> { coEvery { miPerfil("u1") } returns Perfil("u1", "yo") }
        coEvery { social.añadirAlEscaparate(any(), any(), any()) } throws AppException(ErrorApp.Servidor("showcase_full"))
        val eventos = MutableStateFlow(listOf(
            evento(1, hoy.plusDays(3), asistencia = Asistencia.GOING),
            evento(2, hoy.plusDays(5), asistencia = Asistencia.NOT_GOING),
        ))
        val vm = ProfileViewModel(perfiles, social, eventos, userId = { "u1" }, hoy = { hoy })
        backgroundScope.launch { vm.ui.collect {} }
        vm.añadirAlEscaparate("content://foto", 1)
        advanceUntilIdle()
        assertEquals(R.string.esc_lleno, vm.ui.value.mensaje)
        assertEquals(listOf(1L), vm.ui.value.proximos.map { it.evento.id })
        assertEquals("yo", vm.ui.value.perfil?.nickname)
    }
}
