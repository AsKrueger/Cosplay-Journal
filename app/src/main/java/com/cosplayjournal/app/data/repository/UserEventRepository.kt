package com.cosplayjournal.app.data.repository

import android.util.Log
import com.cosplayjournal.app.data.dao.UserEventDao
import com.cosplayjournal.app.data.entity.UserEventData
import com.cosplayjournal.app.data.remote.AttendanceApi
import com.cosplayjournal.app.data.remote.EventStatusBody
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Estado de participación del usuario en cada evento.
 *
 * Se guarda en el móvil y, si hay sesión, también en el servidor para verlo desde otros dispositivos.
 * Si el servidor no responde o la sesión ha caducado, el cambio queda guardado en local.
 */
class UserEventRepository(
    private val dao: UserEventDao,
    private val remote: AttendanceApi? = null
) {
    val all: Flow<List<UserEventData>> = dao.getAll()

    suspend fun get(eventId: String): UserEventData? = dao.get(eventId)

    suspend fun save(data: UserEventData) {
        dao.insert(data)
        pushToServer(data)
    }

    /** Sustituye el estado local por el del servidor. Solo tiene efecto con sesión iniciada. */
    suspend fun syncFromServer() {
        val api = remote ?: return
        val statuses = withContext(Dispatchers.IO) { api.myStatuses() }
        dao.clear()
        dao.insertAll(statuses.map { UserEventData(eventId = it.eventId, status = it.status) })
    }

    private suspend fun pushToServer(data: UserEventData) {
        val api = remote ?: return
        try {
            val response = withContext(Dispatchers.IO) {
                api.setStatus(data.eventId, EventStatusBody(data.status))
            }
            // Retrofit no lanza excepción con un 401: hay que comprobar el código
            if (!response.isSuccessful) {
                Log.w("UserEventRepository", "El servidor rechazó el estado: código " + response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin conexión: el cambio ya está guardado en local
            Log.w("UserEventRepository", "No se pudo enviar el estado al servidor", e)
        }
    }
}
