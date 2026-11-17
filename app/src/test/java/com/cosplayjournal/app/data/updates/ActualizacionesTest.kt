package com.cosplayjournal.app.data.updates

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ActualizacionesTest {
    private class Fuente(var v: VersionPublicada?, var falla: Boolean = false) : FuenteVersiones {
        var consultas = 0
        override suspend fun ultima(): VersionPublicada? { consultas++; if (falla) throw IOException(); return v }
    }

    private class Descartada : VersionDescartada {
        var v: String? = null
        override suspend fun leer() = v
        override suspend fun guardar(version: String) { v = version }
    }

    @Test fun `compara versiones número a número`() {
        assertTrue(Versiones.esMasNueva("2.10.0", "2.9.3"))
        assertTrue(Versiones.esMasNueva("v2.1", "2.0.9"))
        assertFalse(Versiones.esMasNueva("2.0.0", "2.0.0"))
        assertFalse(Versiones.esMasNueva("1.9.9", "2.0.0"))
        assertFalse(Versiones.esMasNueva("2.0.0-beta", "2.0.0"))
    }

    @Test fun `avisa una sola vez por versión y no consulta dos veces`() = runTest {
        val fuente = Fuente(VersionPublicada("2.1.0", "https://x/app.apk"))
        val descartada = Descartada()
        val repo = ActualizacionesRepository(fuente, "2.0.0", descartada)
        repo.comprobar()
        repo.comprobar()
        assertEquals(1, fuente.consultas)
        assertEquals("2.1.0", repo.aviso.value?.version)
        repo.descartarAviso()
        assertNull(repo.aviso.value)
        assertEquals("2.1.0", repo.disponible.value?.version) // sigue visible en Ajustes

        val otraApertura = ActualizacionesRepository(fuente, "2.0.0", descartada)
        otraApertura.comprobar()
        assertNull(otraApertura.aviso.value)
        assertEquals("2.1.0", otraApertura.disponible.value?.version)
    }

    @Test fun `sin red o sin versión nueva no molesta`() = runTest {
        val sinRed = ActualizacionesRepository(Fuente(null, falla = true), "2.0.0", Descartada())
        sinRed.comprobar()
        assertNull(sinRed.disponible.value)
        val igual = ActualizacionesRepository(Fuente(VersionPublicada("2.0.0", "u")), "2.0.0", Descartada())
        igual.comprobar()
        assertNull(igual.aviso.value)
    }
}
