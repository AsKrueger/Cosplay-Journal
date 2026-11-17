package com.cosplayjournal.app.domain

import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.gallery.Foto
import com.cosplayjournal.app.data.gallery.FotoEtiquetas
import com.cosplayjournal.app.data.gallery.FotoGaleria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class GaleriaTest {
    private fun foto(uri: String, fecha: Long, evento: Long? = null, cosplay: String? = null, grupal: Boolean = false) =
        FotoGaleria(Foto(uri, uri, fecha), FotoEtiquetas(uri, evento, cosplay, grupal))

    private val fotos = listOf(
        foto("a", 100, evento = 1, cosplay = "Nezuko"),
        foto("b", 300, evento = 2, cosplay = "nezuko", grupal = true),
        foto("c", 200, evento = 1),
        foto("d", 50),
    )
    private val nombres = mapOf(1L to "Japan Weekend", 2L to "Expocómic")

    @Test fun `por evento el más reciente primero y las sin evento al final`() {
        val g = agrupar(fotos, Orden.EVENTO) { nombres[it] }
        assertEquals(listOf("Expocómic", "Japan Weekend", null), g.map { it.titulo })
        assertEquals(listOf("a", "c"), g[1].fotos.map { it.foto.uri })
        assertEquals(listOf("d"), g[2].fotos.map { it.foto.uri })
    }

    @Test fun `por cosplay sin distinguir mayúsculas`() {
        val g = agrupar(fotos, Orden.COSPLAY) { null }
        assertEquals(2, g.size)
        assertEquals("Nezuko", g[0].titulo)
        assertEquals(2, g[0].fotos.size)
        assertNull(g[1].titulo)
    }

    @Test fun `grupales solo las marcadas`() {
        assertEquals(listOf("b"), agrupar(fotos, Orden.GRUPAL) { null }.single().fotos.map { it.foto.uri })
    }

    @Test fun `sugiere el evento al que fuiste ese día, o el único que hubo`() {
        val dia = LocalDate.of(2026, 7, 4)
        val fui = evento(1, dia.minusDays(1), dia.plusDays(1), asistencia = Asistencia.WENT)
        val otro = evento(2, dia, dia)
        assertEquals(1L, sugerirEvento(dia, listOf(otro, fui))?.evento?.id)
        assertEquals(2L, sugerirEvento(dia, listOf(otro))?.evento?.id)
        assertNull(sugerirEvento(dia, listOf(otro, evento(3, dia, dia))))
        // Foto de la noche anterior al salón
        assertEquals(2L, sugerirEvento(dia.minusDays(1), listOf(otro))?.evento?.id)
        assertNull(sugerirEvento(dia.plusDays(5), listOf(otro)))
    }
}
