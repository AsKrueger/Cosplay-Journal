package com.cosplayjournal.app.data.gallery

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FotoDaoFalso : FotoDao {
    val tabla = MutableStateFlow<Map<String, FotoEtiquetas>>(emptyMap())
    override fun todas() = tabla.map { it.values.toList() }
    override suspend fun guardar(fotos: List<FotoEtiquetas>) { tabla.value += fotos.associateBy { it.uri } }
    override fun cosplays() = tabla.map { t -> t.values.mapNotNull { it.cosplay }.distinct().sortedBy { it.lowercase() } }
    override suspend fun borrarTodo() { tabla.value = emptyMap() }
}

class LectorFalso : LectorCarpeta {
    val contenido = mutableMapOf<String, List<Foto>>()
    val permisos = mutableSetOf<String>()
    override suspend fun imagenes(carpeta: String) = contenido[carpeta] ?: throw SecurityException("sin permiso")
    override fun conservarPermiso(carpeta: String) { permisos += carpeta }
    override fun soltarPermiso(carpeta: String) { permisos -= carpeta }
}

class CarpetaEnMemoria : CarpetaGuardada {
    override val carpeta = MutableStateFlow<String?>(null)
    override suspend fun guardar(uri: String?) { carpeta.value = uri }
}

class GalleryRepositoryTest {
    private val dao = FotoDaoFalso()
    private val lector = LectorFalso().apply {
        contenido["tree://a"] = listOf(Foto("img://1", "1.jpg", 2000), Foto("img://2", "2.jpg", 1000))
        contenido["tree://b"] = listOf(Foto("img://3", "3.jpg", 3000))
    }
    private val guardada = CarpetaEnMemoria()
    private val repo = GalleryRepository(dao, lector, guardada)

    @Test fun `vincular guarda la carpeta, conserva el permiso y lee las fotos`() = runTest {
        repo.vincular("tree://a")
        assertEquals("tree://a", guardada.carpeta.value)
        assertTrue("tree://a" in lector.permisos)
        assertEquals(listOf("img://1", "img://2"), repo.fotos.first().map { it.foto.uri })
    }

    @Test fun `cambiar de carpeta suelta el permiso de la anterior`() = runTest {
        repo.vincular("tree://a")
        repo.vincular("tree://b")
        assertFalse("tree://a" in lector.permisos)
        assertEquals(listOf("img://3"), repo.fotos.first().map { it.foto.uri })
    }

    @Test fun `si se pierde el permiso hay que volver a elegir carpeta`() = runTest {
        guardada.guardar("tree://borrada")
        repo.escanear()
        assertNull(guardada.carpeta.value)
        assertTrue(repo.fotos.first().isEmpty())
    }

    @Test fun `etiquetar varias fotos conserva lo que no se cambia`() = runTest {
        repo.vincular("tree://a")
        repo.etiquetar(listOf("img://1"), eventoId = 7, cosplay = "Nezuko", grupal = false)
        repo.etiquetar(listOf("img://1", "img://2"), grupal = true)
        val f = repo.fotos.first().associateBy { it.foto.uri }
        assertEquals(7L, f["img://1"]!!.etiquetas.eventoId)
        assertEquals("Nezuko", f["img://1"]!!.etiquetas.cosplay)
        assertTrue(f["img://2"]!!.etiquetas.grupal)
        assertNull(f["img://2"]!!.etiquetas.eventoId)

        repo.etiquetar(listOf("img://1"), quitarEvento = true, quitarCosplay = true)
        assertNull(repo.fotos.first().first { it.foto.uri == "img://1" }.etiquetas.eventoId)
        assertEquals(emptyList<String>(), repo.cosplays.first())
    }

    @Test fun `borrar datos suelta la carpeta y las etiquetas`() = runTest {
        repo.vincular("tree://a")
        repo.etiquetar(listOf("img://1"), cosplay = "Zelda")
        repo.borrarLocal()
        assertNull(guardada.carpeta.value)
        assertTrue(lector.permisos.isEmpty())
        assertTrue(dao.tabla.value.isEmpty())
    }
}
