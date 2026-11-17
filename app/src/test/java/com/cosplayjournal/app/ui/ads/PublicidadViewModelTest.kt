package com.cosplayjournal.app.ui.ads

import com.cosplayjournal.app.ReglaDispatcher
import com.cosplayjournal.app.data.ads.AdsRepository
import com.cosplayjournal.app.data.ads.Anuncio
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class PublicidadViewModelTest {
    @get:Rule val regla = ReglaDispatcher()

    private class Repo(var lista: List<Anuncio>, var fallar: Boolean = false) : AdsRepository {
        override suspend fun activos(): List<Anuncio> = if (fallar) throw IOException() else lista
    }

    private fun anuncio(id: Long) = Anuncio(id, "Tienda $id", "https://img/$id.webp", "https://tienda/$id")

    @Test fun `sin anuncios la barra no aparece`() = runTest(regla.dispatcher) {
        val vm = PublicidadViewModel(Repo(emptyList()), consulta = 1_000, rotacion = 100)
        runCurrent()
        assertNull(vm.actual.value)
        vm.viewModelScope.cancel()
    }

    @Test fun `rota entre anuncios y se oculta cuando dejan de estar activos`() = runTest(regla.dispatcher) {
        val repo = Repo(listOf(anuncio(1), anuncio(2)))
        val vm = PublicidadViewModel(repo, consulta = 1_000, rotacion = 100)
        runCurrent()
        assertEquals(1L, vm.actual.value?.id)
        advanceTimeBy(101)
        assertEquals(2L, vm.actual.value?.id)

        repo.lista = emptyList()
        advanceTimeBy(1_000)
        runCurrent()
        assertNull(vm.actual.value)
        vm.viewModelScope.cancel()
    }

    @Test fun `sin conexión mantiene el último anuncio conocido`() = runTest(regla.dispatcher) {
        val repo = Repo(listOf(anuncio(7)))
        val vm = PublicidadViewModel(repo, consulta = 1_000, rotacion = 100)
        runCurrent()
        repo.fallar = true
        advanceTimeBy(1_001)
        runCurrent()
        assertEquals(7L, vm.actual.value?.id)
        vm.viewModelScope.cancel()
    }
}

