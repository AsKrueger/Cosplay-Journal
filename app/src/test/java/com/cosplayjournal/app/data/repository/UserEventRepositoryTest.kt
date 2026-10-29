package com.cosplayjournal.app.data.repository

import com.cosplayjournal.app.data.dao.UserEventDao
import com.cosplayjournal.app.data.entity.UserEventData
import com.cosplayjournal.app.data.remote.CosplayJournalApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserEventRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var dao: UserEventDao

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dao = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `guardar un estado lo escribe en local y lo envia al servidor`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))
        val repository = UserEventRepository(dao, CosplayJournalApi.create(server.url("/").toString()))

        repository.save(UserEventData(eventId = "evt-1", status = "ATTENDING"))

        coVerify { dao.insert(UserEventData(eventId = "evt-1", status = "ATTENDING")) }
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/me/events/evt-1", request.path)
        assertEquals("""{"status":"ATTENDING"}""", request.body.readUtf8())
    }

    @Test
    fun `si el servidor no responde, el cambio queda en local igualmente`() = runBlocking {
        val api = CosplayJournalApi.create(server.url("/").toString())
        server.shutdown()
        val repository = UserEventRepository(dao, api)

        repository.save(UserEventData(eventId = "evt-2", status = "INTERESTED"))

        coVerify { dao.insert(UserEventData(eventId = "evt-2", status = "INTERESTED")) }
    }

    @Test
    fun `sincronizar sustituye el estado local por el del servidor`() = runBlocking {
        server.enqueue(MockResponse().setBody("""[{"eventId":"evt-9","status":"ATTENDED"}]"""))
        coEvery { dao.clear() } returns Unit
        val repository = UserEventRepository(dao, CosplayJournalApi.create(server.url("/").toString()))

        repository.syncFromServer()

        coVerify { dao.clear() }
        coVerify { dao.insertAll(listOf(UserEventData(eventId = "evt-9", status = "ATTENDED"))) }
    }
}
