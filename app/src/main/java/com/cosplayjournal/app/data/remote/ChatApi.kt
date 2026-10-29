package com.cosplayjournal.app.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Chats privados y grupos (`/api/v1/chats`). Requieren sesión. */
interface ChatApi {

    @GET("api/v1/chats")
    suspend fun conversations(): List<RemoteConversation>

    @POST("api/v1/chats/private")
    suspend fun openPrivate(@Body body: OpenPrivateBody): RemoteConversation

    @POST("api/v1/chats/groups")
    suspend fun createGroup(@Body body: CreateGroupBody): RemoteConversation

    /** Mensajes posteriores a [since] (ISO-8601), o todos si es null. */
    @GET("api/v1/chats/{id}/messages")
    suspend fun messages(@Path("id") id: String, @Query("since") since: String?): List<RemoteMessage>

    @POST("api/v1/chats/{id}/messages")
    suspend fun send(@Path("id") id: String, @Body body: SendMessageBody): RemoteMessage
}

/** Equivale a `ConversationView` del backend. */
@Serializable
data class RemoteConversation(
    val id: String,
    val type: String,
    val name: String? = null,
    val memberNicknames: List<String> = emptyList(),
    val createdAt: String? = null
)

/** Equivale a `MessageView` del backend. */
@Serializable
data class RemoteMessage(
    val id: String,
    val senderNickname: String,
    val kind: String,
    val body: String,
    val createdAt: String
)

@Serializable
data class OpenPrivateBody(val nickname: String)

@Serializable
data class CreateGroupBody(val name: String, val nicknames: List<String>)

@Serializable
data class SendMessageBody(val kind: String, val body: String)
