package com.cosplayjournal.app.ui.viewmodel

import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.data.repository.UserEventRepository
import com.cosplayjournal.app.data.repository.EventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: EventRepository
    private lateinit var userEventRepository: UserEventRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()
        userEventRepository = mockk()
        every { userEventRepository.all } returns flowOf(emptyList())
        every { userEventRepository.sessionExpired } returns MutableStateFlow(false)
        coEvery { userEventRepository.syncFromServer() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `si falla la primera carga, muestra error y lista vacia`() = runTest(dispatcher) {
        coEvery { repository.getEvents() } throws IOException("sin conexion")

        val viewModel = EventViewModel(repository, userEventRepository)
        advanceUntilIdle()

        assertTrue(viewModel.loadError.value)
        assertTrue(viewModel.events.value.isEmpty())
    }

    @Test
    fun `si falla solo el refresco, conserva los eventos mostrados sin error`() = runTest(dispatcher) {
        coEvery { repository.getEvents() } returns listOf(event("1"))
        coEvery { repository.refresh() } throws IOException("backend ocupado")

        val viewModel = EventViewModel(repository, userEventRepository)
        advanceUntilIdle()

        assertFalse(viewModel.loadError.value)
        assertEquals(listOf("1"), viewModel.events.value.map { it.id })
    }

    @Test
    fun `tras el refresco vuelve a cargar y muestra los eventos nuevos`() = runTest(dispatcher) {
        coEvery { repository.getEvents() } returnsMany listOf(
            emptyList(),
            listOf(event("1"), event("2"))
        )
        coEvery { repository.refresh() } returns Unit

        val viewModel = EventViewModel(repository, userEventRepository)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.refresh() }
        coVerify(exactly = 2) { repository.getEvents() }
        assertEquals(listOf("1", "2"), viewModel.events.value.map { it.id })
    }

    private fun event(id: String) = Event(
        id = id,
        name = "Salon $id",
        city = "Malaga",
        venue = "",
        startDate = "2026-10-01",
        endDate = "2026-10-04",
        website = ""
    )
}
