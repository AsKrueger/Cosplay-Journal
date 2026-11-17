package com.cosplayjournal.app.ui.chat

import com.cosplayjournal.app.ReglaDispatcher
import com.cosplayjournal.app.data.chat.ChatRepository
import com.cosplayjournal.app.data.chat.InfoChat
import com.cosplayjournal.app.data.chat.Mensaje
import com.cosplayjournal.app.data.chat.ResumenChat
import com.cosplayjournal.app.data.social.Contacto
import com.cosplayjournal.app.data.social.Relacion
import com.cosplayjournal.app.data.social.SocialRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatFalso : ChatRepository {
    val guardados = mutableListOf<Mensaje>()
    val tiempoReal = MutableSharedFlow<Mensaje>()
    var leidos = 0
    private var siguiente = 1L

    fun mensaje(texto: String, autor: String = "otro") =
        Mensaje(siguiente++, "c1", autor, texto, null, "2026-11-17T10:%02d:00Z".format(siguiente % 60)).also { guardados += it }

    override suspend fun misChats() = listOf(ResumenChat("c1", "DIRECT", otroNickname = "bea", unread = 2))
    override suspend fun abrirDirecto(amigoId: String) = "c1"
    override suspend fun crearGrupo(titulo: String, miembros: List<String>, eventoId: Long?) = "g1"
    override suspend fun mensajes(chatId: String, antesDe: String?, limite: Int) =
        guardados.filter { antesDe == null || it.fecha < antesDe }.sortedByDescending { it.id }.take(limite)
    override suspend fun enviarTexto(chatId: String, texto: String) { mensaje(texto, autor = "yo") }
    override suspend fun enviarFoto(chatId: String, fotoUri: String) {}
    override suspend fun marcarLeido(chatId: String) { leidos++ }
    override suspend fun info(chatId: String) = InfoChat(emptyList(), null)
    override suspend fun añadirMiembro(chatId: String, userId: String) {}
    override suspend fun salir(chatId: String) {}
    override fun nuevos(chatId: String) = tiempoReal
}

class ChatViewModelsTest {
    @get:Rule val regla = ReglaDispatcher()

    @Test fun `carga los mensajes, marca leído y recibe en tiempo real sin duplicar`() = runTest(regla.dispatcher) {
        val repo = ChatFalso()
        repo.mensaje("hola")
        val vm = ChatRoomViewModel("c1", repo, yo = "yo")
        advanceUntilIdle()
        assertEquals(listOf("hola"), vm.ui.value.mensajes.map { it.body })
        assertTrue(repo.leidos >= 1)
        assertFalse(vm.ui.value.hayMas)

        val nuevo = repo.mensaje("¿vamos al salón?")
        repo.tiempoReal.emit(nuevo)
        repo.tiempoReal.emit(nuevo)
        advanceUntilIdle()
        assertEquals(listOf("¿vamos al salón?", "hola"), vm.ui.value.mensajes.map { it.body })
    }

    @Test fun `al enviar aparece el mensaje aunque falle el tiempo real`() = runTest(regla.dispatcher) {
        val repo = ChatFalso()
        val vm = ChatRoomViewModel("c1", repo, yo = "yo")
        advanceUntilIdle()
        vm.enviarTexto("¡Voy con el cosplay de Zelda!")
        advanceUntilIdle()
        assertEquals("yo", vm.ui.value.mensajes.first().autor)
        assertFalse(vm.ui.value.enviando)
    }

    @Test fun `pagina hacia atrás de 50 en 50`() = runTest(regla.dispatcher) {
        val repo = ChatFalso()
        repeat(70) { repo.mensaje("m$it") }
        val vm = ChatRoomViewModel("c1", repo, yo = "yo")
        advanceUntilIdle()
        assertEquals(50, vm.ui.value.mensajes.size)
        assertTrue(vm.ui.value.hayMas)
        vm.cargarMas()
        advanceUntilIdle()
        assertEquals(70, vm.ui.value.mensajes.map { it.id }.distinct().size)
    }

    @Test fun `crear un grupo abre el chat nuevo y solo ofrece amigos`() = runTest(regla.dispatcher) {
        val social = mockk<SocialRepository> {
            coEvery { contactos() } returns listOf(
                Contacto("1", "ana", null, Relacion.FRIEND), Contacto("2", "bea", null, Relacion.SENT),
            )
        }
        val vm = ChatsViewModel(ChatFalso(), social)
        vm.cargarAmigos()
        advanceUntilIdle()
        assertEquals(listOf("ana"), vm.ui.value.amigos.map { it.nickname })
        vm.crearGrupo("Grupal JoJo", listOf("1"), null)
        advanceUntilIdle()
        assertEquals("g1", vm.ui.value.abrir)
        assertEquals(2L, vm.ui.value.chats.single().unread)
    }
}
