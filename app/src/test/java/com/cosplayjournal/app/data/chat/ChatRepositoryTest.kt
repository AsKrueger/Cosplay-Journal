package com.cosplayjournal.app.data.chat

import com.cosplayjournal.app.data.remote.CosplayJournalApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class ChatRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: ChatRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = ChatRepository(CosplayJournalApi.create(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `abrir chat privado manda el nickname sin espacios`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":"c1","type":"PRIVATE","memberNicknames":["ana_1","bob_2"]}"""))

        val conversation = repository.openPrivate("  bob_2 ")

        assertEquals("c1", conversation.id)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/chats/private", request.path)
        assertEquals("""{"nickname":"bob_2"}""", request.body.readUtf8())
    }

    @Test
    fun `crear grupo descarta nicknames vacios`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":"g1","type":"GROUP","name":"Cosplayers"}"""))

        val group = repository.createGroup(" Cosplayers ", listOf("bob_2", " ", "ana_1 "))

        assertEquals("GROUP", group.type)
        val request = server.takeRequest()
        assertEquals("/api/v1/chats/groups", request.path)
        assertEquals("""{"name":"Cosplayers","nicknames":["bob_2","ana_1"]}""", request.body.readUtf8())
    }

    @Test
    fun `mensajes se piden con since`() = runBlocking {
        server.enqueue(MockResponse().setBody("""[{"id":"m1","senderNickname":"ana_1","kind":"TEXT","body":"hola","createdAt":"2026-10-28T10:00:00Z"}]"""))

        val messages = repository.messages("c1", "2026-10-28T09:00:00Z")

        assertEquals("hola", messages[0].body)
        val request = server.takeRequest()
        assertEquals("/api/v1/chats/c1/messages", request.requestUrl!!.encodedPath)
        assertEquals("2026-10-28T09:00:00Z", request.requestUrl!!.queryParameter("since"))
    }

    @Test
    fun `enviar foto manda kind IMAGE con la url sin espacios`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":"m2","senderNickname":"ana_1","kind":"IMAGE","body":"https://x.test/a.jpg","createdAt":"2026-10-28T10:01:00Z"}"""))

        val sent = repository.sendPhoto("c1", " https://x.test/a.jpg ")

        assertEquals("IMAGE", sent.kind)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("""{"kind":"IMAGE","body":"https://x.test/a.jpg"}""", request.body.readUtf8())
    }

    @Test
    fun `el texto se envia sin saltos de linea sobrantes`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":"m3","senderNickname":"ana_1","kind":"TEXT","body":"hola","createdAt":"2026-10-28T10:02:00Z"}"""))

        repository.sendText("c1", "hola\n")

        assertEquals("""{"kind":"TEXT","body":"hola"}""", server.takeRequest().body.readUtf8())
    }

    @Test
    fun `un no miembro recibe el 404 de la API`() {
        server.enqueue(MockResponse().setResponseCode(404))

        val error = assertThrows(HttpException::class.java) {
            runBlocking { repository.sendText("c1", "hola") }
        }
        assertEquals(404, error.code())
    }
}
