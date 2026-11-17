package com.cosplayjournal.app.ui.auth

import com.cosplayjournal.app.R
import com.cosplayjournal.app.ReglaDispatcher
import com.cosplayjournal.app.data.AppException
import com.cosplayjournal.app.data.ErrorApp
import com.cosplayjournal.app.data.auth.AccesoException
import com.cosplayjournal.app.data.auth.AuthRepository
import com.cosplayjournal.app.data.auth.ErrorAcceso
import com.cosplayjournal.app.data.auth.ResultadoRegistro
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AuthViewModelTest {
    @get:Rule val regla = ReglaDispatcher()
    private val repo = mockk<AuthRepository>(relaxed = true)

    @Test fun `valida nickname, email y password`() {
        assertTrue(Reglas.nicknameValido("cos_player.01"))
        assertFalse(Reglas.nicknameValido("ab"))
        assertFalse(Reglas.nicknameValido("con espacio"))
        assertFalse(Reglas.nicknameValido("ñandú"))
        assertTrue(Reglas.emailValido(" a@b.es "))
        assertFalse(Reglas.emailValido("a@b"))
        assertFalse(Reglas.passwordValida("1234567"))
    }

    @Test fun `comprueba el nickname solo cuando deja de escribir`() = runTest(regla.dispatcher) {
        coEvery { repo.nicknameDisponible("kira") } returns true
        val vm = AuthViewModel(repo)
        vm.nickname("ki")
        assertEquals(EstadoNickname.INVALIDO, vm.ui.value.estadoNickname)
        vm.nickname("kir")
        vm.nickname("kira")
        assertEquals(EstadoNickname.COMPROBANDO, vm.ui.value.estadoNickname)
        advanceUntilIdle()
        assertEquals(EstadoNickname.LIBRE, vm.ui.value.estadoNickname)
        coVerify(exactly = 1) { repo.nicknameDisponible(any()) }
    }

    @Test fun `registro exige nickname libre, edad y privacidad`() = runTest(regla.dispatcher) {
        coEvery { repo.nicknameDisponible(any()) } returns true
        coEvery { repo.registrar(any(), any(), any()) } returns ResultadoRegistro.CONFIRMAR_EMAIL
        val vm = AuthViewModel(repo)
        vm.cambiarModo()
        vm.email("kira@mail.es"); vm.password("12345678"); vm.nickname("kira")
        advanceUntilIdle()
        assertFalse(vm.ui.value.puedeEnviar)
        vm.mayorDe14(true); vm.aceptaPrivacidad(true)
        assertTrue(vm.ui.value.puedeEnviar)
        vm.enviar()
        advanceUntilIdle()
        coVerify { repo.registrar("kira@mail.es", "12345678", "kira") }
        assertTrue(vm.ui.value.confirmarEmail)
    }

    @Test fun `recuperar contraseña pide el código y luego guarda la nueva`() = runTest(regla.dispatcher) {
        val vm = AuthViewModel(repo)
        vm.email("kira@mail.es")
        vm.empezarRecuperacion()
        assertEquals(PasoRecuperar.EMAIL, vm.ui.value.recuperar)
        vm.recuperar()
        advanceUntilIdle()
        coVerify { repo.enviarCodigoRecuperacion("kira@mail.es") }
        assertEquals(PasoRecuperar.CODIGO, vm.ui.value.recuperar)

        vm.codigo("12a34 56")
        assertEquals("123456", vm.ui.value.codigo)
        vm.password("corta")
        assertFalse(vm.ui.value.puedeRecuperar)
        vm.password("nueva-segura")
        vm.recuperar()
        advanceUntilIdle()
        coVerify { repo.restablecerPassword("kira@mail.es", "123456", "nueva-segura") }
    }

    @Test fun `un código caducado lo explica`() = runTest(regla.dispatcher) {
        coEvery { repo.restablecerPassword(any(), any(), any()) } throws
            AppException(ErrorApp.Servidor("CODIGO_INCORRECTO"), AccesoException(ErrorAcceso.CODIGO_INCORRECTO))
        val vm = AuthViewModel(repo)
        vm.email("kira@mail.es")
        vm.empezarRecuperacion()
        vm.recuperar()
        advanceUntilIdle()
        vm.codigo("000000"); vm.password("12345678")
        vm.recuperar()
        advanceUntilIdle()
        assertEquals(R.string.auth_error_codigo, vm.ui.value.error)
        assertEquals(PasoRecuperar.CODIGO, vm.ui.value.recuperar)
    }

    @Test fun `credenciales incorrectas muestran su mensaje`() = runTest(regla.dispatcher) {
        coEvery { repo.entrar(any(), any()) } throws
            AppException(ErrorApp.Servidor("CREDENCIALES"), AccesoException(ErrorAcceso.CREDENCIALES))
        val vm = AuthViewModel(repo)
        vm.email("a@b.es"); vm.password("12345678")
        vm.enviar()
        advanceUntilIdle()
        assertEquals(R.string.auth_error_credenciales, vm.ui.value.error)
        assertFalse(vm.ui.value.enviando)
    }

    @Test fun `sin red muestra error de conexión`() = runTest(regla.dispatcher) {
        coEvery { repo.entrar(any(), any()) } throws IOException("offline")
        val vm = AuthViewModel(repo)
        vm.email("a@b.es"); vm.password("12345678")
        vm.enviar()
        advanceUntilIdle()
        assertEquals(R.string.error_sin_conexion, vm.ui.value.error)
    }
}
