package com.cosplayjournal.app.data.repository

import com.cosplayjournal.app.data.dao.UserEventDao
import com.cosplayjournal.app.data.entity.UserEventData
import kotlinx.coroutines.flow.Flow

/** Estado de participación del usuario en cada evento, guardado en el móvil. */
class UserEventRepository(private val dao: UserEventDao) {
    val all: Flow<List<UserEventData>> = dao.getAll()

    suspend fun get(eventId: String): UserEventData? = dao.get(eventId)

    suspend fun save(data: UserEventData) = dao.insert(data)
}
