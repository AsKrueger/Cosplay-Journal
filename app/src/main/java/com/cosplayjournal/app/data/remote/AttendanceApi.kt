package com.cosplayjournal.app.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/** Estado de participación del usuario en eventos (`/api/v1/me/events`). Requiere sesión. */
interface AttendanceApi {

    @GET("api/v1/me/events")
    suspend fun myStatuses(): List<RemoteEventStatus>

    @PUT("api/v1/me/events/{eventId}")
    suspend fun setStatus(@Path("eventId") eventId: String, @Body body: EventStatusBody): Response<Unit>
}

/** Equivale a `EventStatusResponse` del backend. */
@Serializable
data class RemoteEventStatus(val eventId: String, val status: String)

@Serializable
data class EventStatusBody(val status: String)
