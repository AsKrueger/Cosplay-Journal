package com.cosplayjournal.app.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Amistades entre cosplayers (`/api/v1/friends`). Todas requieren sesión. */
interface FriendsApi {

    @GET("api/v1/friends")
    suspend fun friends(): List<RemotePublicProfile>

    @GET("api/v1/friends/requests")
    suspend fun incomingRequests(): List<RemoteFriendRequest>

    @POST("api/v1/friends/requests")
    suspend fun sendRequest(@Body body: SendFriendBody): RemoteFriendship

    @POST("api/v1/friends/requests/{id}/accept")
    suspend fun accept(@Path("id") id: String): Response<Unit>

    @POST("api/v1/friends/requests/{id}/reject")
    suspend fun reject(@Path("id") id: String): Response<Unit>

    @DELETE("api/v1/friends/{nickname}")
    suspend fun remove(@Path("nickname") nickname: String): Response<Unit>
}

/** Equivale a `PublicUserProfile` del backend. Nunca incluye email. */
@Serializable
data class RemotePublicProfile(
    val nickname: String,
    val avatarUrl: String? = null,
    val memberSince: String? = null
)

/** Equivale a `FriendRequestSummary` del backend. */
@Serializable
data class RemoteFriendRequest(val id: String, val requester: RemotePublicProfile)

@Serializable
data class SendFriendBody(val nickname: String)

/** Equivale a `FriendshipResponse` del backend. */
@Serializable
data class RemoteFriendship(val id: String, val status: String)
