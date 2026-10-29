package com.cosplayjournal.app.data.gallery

import com.cosplayjournal.app.data.remote.CosplayJournalApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GalleryRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: GalleryRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = GalleryRepository(CosplayJournalApi.create(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `listar pasa la etiqueta como filtro y omite el evento si es nulo`() = runBlocking {
        server.enqueue(MockResponse().setBody(
            """[{"id":"p1","tag":"COSPLAY","eventId":null,"url":"http://x/p1.jpg","uploadedAt":"2026-10-29T10:00:00Z"}]"""
        ))

        val photos = repository.photos("COSPLAY", null)

        assertEquals("p1", photos[0].id)
        assertEquals("/api/v1/gallery/photos?tag=COSPLAY", server.takeRequest().path)
    }

    @Test
    fun `subir manda la imagen y la etiqueta como multipart`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody(
            """{"id":"p2","tag":"EVENTO","eventId":"evt-7","url":"http://x/p2.jpg","uploadedAt":"2026-10-29T10:01:00Z"}"""
        ))

        val sent = repository.upload(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()), "image/jpeg", "EVENTO", "evt-7")

        assertEquals("evt-7", sent.eventId)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/gallery/photos", request.path)
        assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("name=\"file\""))
        assertTrue(body.contains("name=\"tag\""))
        assertTrue(body.contains("EVENTO"))
        assertTrue(body.contains("name=\"eventId\""))
    }

    @Test
    fun `subir sin evento no envia el campo eventId`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody(
            """{"id":"p3","tag":"COSPLAY","eventId":null,"url":"http://x/p3.jpg","uploadedAt":"2026-10-29T10:02:00Z"}"""
        ))

        val sent = repository.upload(byteArrayOf(1, 2, 3), "image/png", "COSPLAY", null)

        assertNull(sent.eventId)
        val body = server.takeRequest().body.readUtf8()
        assertTrue(!body.contains("name=\"eventId\""))
    }
}
