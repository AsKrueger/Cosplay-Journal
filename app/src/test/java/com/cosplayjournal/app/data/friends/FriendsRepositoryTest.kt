package com.cosplayjournal.app.data.friends

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

class FriendsRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: FriendsRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = FriendsRepository(CosplayJournalApi.create(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `lista de amigos y solicitudes se leen de sus rutas`() = runBlocking {
        server.enqueue(MockResponse().setBody("""[{"nickname":"bob_2","avatarUrl":null}]"""))
        server.enqueue(MockResponse().setBody("""[{"id":"r1","requester":{"nickname":"ana_1"}}]"""))

        val friends = repository.friends()
        val requests = repository.incomingRequests()

        assertEquals("bob_2", friends[0].nickname)
        assertEquals("ana_1", requests[0].requester.nickname)
        assertEquals("/api/v1/friends", server.takeRequest().path)
        assertEquals("/api/v1/friends/requests", server.takeRequest().path)
    }

    @Test
    fun `enviar solicitud manda el nickname sin espacios`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":"r9","status":"PENDING"}"""))

        val created = repository.sendRequest("  bob_2 ")

        assertEquals("PENDING", created.status)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("""{"nickname":"bob_2"}""", request.body.readUtf8())
    }

    @Test
    fun `aceptar usa la ruta con el id de la solicitud`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))

        repository.accept("r1")

        assertEquals("/api/v1/friends/requests/r1/accept", server.takeRequest().path)
    }

    @Test
    fun `quitar amigo usa DELETE con el nickname`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))

        repository.remove("bob_2")

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/v1/friends/bob_2", request.path)
    }

    @Test
    fun `un error de la API llega con su codigo`() {
        server.enqueue(MockResponse().setResponseCode(404))

        val error = assertThrows(HttpException::class.java) {
            runBlocking { repository.reject("nope") }
        }
        assertEquals(404, error.code())
    }
}
