package com.cosplayjournal.app.data.repository

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
import java.io.IOException

class EventRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: EventRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = EventRepository(CosplayJournalApi.create(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `recorre todas las paginas hasta last y mapea los campos`() = runBlocking {
        server.enqueue(MockResponse().setBody(PAGE_0))
        server.enqueue(MockResponse().setBody(PAGE_1))

        val events = repository.getEvents()

        assertEquals(3, events.size)
        assertEquals("Salon A", events[0].name)
        assertEquals("Malaga", events[0].city)
        assertEquals("Recinto", events[0].venue)
        assertEquals("2026-10-01", events[0].startDate)
        assertEquals("2026-10-04", events[0].endDate)
        assertEquals("", events[1].venue) // campo nulo en la API se convierte en cadena vacía
        assertEquals("Salon C", events[2].name)

        // Pide la página 0 y después la 1, con el tamaño por defecto
        assertEquals("/api/v1/events?page=0&size=100", server.takeRequest().path)
        assertEquals("/api/v1/events?page=1&size=100", server.takeRequest().path)
    }

    @Test
    fun `un error del servidor se propaga como excepcion`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("error"))

        assertThrows(HttpException::class.java) {
            runBlocking { repository.getEvents() }
        }
    }

    @Test
    fun `refresh falla si el backend no responde 2xx`() {
        server.enqueue(MockResponse().setResponseCode(503))

        assertThrows(IOException::class.java) {
            runBlocking { repository.refresh() }
        }
    }

    companion object {
        private const val PAGE_0 = """
            {"content":[
              {"id":"1","name":"Salon A","startDate":"2026-10-01","endDate":"2026-10-04","city":"Malaga","venue":"Recinto","website":"https://a.example"},
              {"id":"2","name":"Salon B","startDate":"2026-10-02","endDate":"2026-10-02","city":null,"venue":null}
            ],"last":false}
        """

        private const val PAGE_1 = """
            {"content":[
              {"id":"3","name":"Salon C","startDate":"2026-11-05","endDate":"2026-11-07","city":"Sevilla"}
            ],"last":true}
        """
    }
}
