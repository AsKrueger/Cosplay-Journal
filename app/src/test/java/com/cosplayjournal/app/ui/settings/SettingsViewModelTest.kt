package com.cosplayjournal.app.ui.settings

import com.cosplayjournal.app.R
import com.cosplayjournal.app.ReglaDispatcher
import com.cosplayjournal.app.data.auth.AuthRepository
import com.cosplayjournal.app.data.feedback.FeedbackRepository
import com.cosplayjournal.app.data.settings.Ajustes
import com.cosplayjournal.app.data.settings.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule val regla = ReglaDispatcher()

    private val ajustes = MutableStateFlow(Ajustes())
    private val settings = object : SettingsRepository {
        override val ajustes = this@SettingsViewModelTest.ajustes
        override suspend fun setImagenesSinConexion(activo: Boolean) { ajustes.value = Ajustes(activo) }
        override suspend fun borrarTodo() { ajustes.value = Ajustes() }
    }
    private val auth = mockk<AuthRepository>(relaxed = true) { every { userIdActual() } returns "u1" }
    private val comentarios = mockk<FeedbackRepository>(relaxed = true)
    private val almacen = mockk<AlmacenLocal>(relaxed = true) { coEvery { tamañoCache() } returns 2048 }

    private fun vm() = SettingsViewModel(settings, auth, comentarios, almacen, "2.0.0")

    @Test fun `desactivar imágenes sin conexión borra la caché`() = runTest(regla.dispatcher) {
        val vm = vm()
        backgroundScope.launch { vm.ui.collect {} }
        vm.imagenesSinConexion(false)
        advanceUntilIdle()
        assertFalse(vm.ui.value.imagenesSinConexion)
        coVerify { almacen.borrarCache() }
        assertEquals(2048L, vm.ui.value.bytesCache)
    }

    @Test fun `envía el comentario con usuario, versión e idioma`() = runTest(regla.dispatcher) {
        val vm = vm()
        backgroundScope.launch { vm.ui.collect {} }
        vm.enviarComentario("  ", "es")
        vm.enviarComentario("Me encanta", "es-ES")
        advanceUntilIdle()
        coVerify(exactly = 1) { comentarios.enviar("u1", "Me encanta", "2.0.0", "es-ES") }
        assertEquals(R.string.ajustes_comentarios_gracias, vm.ui.value.mensaje)
    }

    @Test fun `borrar la cuenta también limpia los datos del móvil`() = runTest(regla.dispatcher) {
        val vm = vm()
        vm.borrarCuenta()
        advanceUntilIdle()
        coVerify { auth.borrarCuenta() }
        coVerify { almacen.borrarDatos() }
    }

    @Test fun `un fallo de red se muestra al usuario`() = runTest(regla.dispatcher) {
        coEvery { comentarios.enviar(any(), any(), any(), any()) } throws java.io.IOException()
        val vm = vm()
        backgroundScope.launch { vm.ui.collect {} }
        vm.enviarComentario("hola", "es")
        advanceUntilIdle()
        assertEquals(R.string.error_sin_conexion, vm.ui.value.mensaje)
    }
}
