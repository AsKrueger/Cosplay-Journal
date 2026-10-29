package com.cosplayjournal.app.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/** Subconjunto de la API REST del backend (`/api/v1`) que usa la app. */
interface CosplayJournalApi : FriendsApi, AttendanceApi, ChatApi, GalleryApi {

    @GET("api/v1/events")
    suspend fun getEvents(
        @Query("page") page: Int,
        @Query("size") size: Int = PAGE_SIZE
    ): RemoteEventPage

    /** Pide al backend que sincronice con ListadoManga si la última sincronización es antigua. */
    @POST("api/v1/events/refresh")
    suspend fun refreshEvents(): Response<Unit>

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterBody): Response<Unit>

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginBody): RemoteToken

    @GET("api/v1/me")
    suspend fun myProfile(): RemoteOwnProfile

    @PUT("api/v1/me/avatar")
    suspend fun changeAvatar(@Body body: AvatarBody): RemoteOwnProfile

    companion object {
        const val PAGE_SIZE = 100

        /**
         * @param tokenProvider devuelve el token guardado; si existe, se añade a cada petición.
         */
        fun create(baseUrl: String, tokenProvider: () -> String? = { null }): CosplayJournalApi {
            val json = Json { ignoreUnknownKeys = true }
            val authInterceptor = Interceptor { chain ->
                val request = chain.request()
                val token = tokenProvider()
                val authorized = if (token != null) {
                    request.newBuilder().header("Authorization", "Bearer $token").build()
                } else {
                    request
                }
                chain.proceed(authorized)
            }
            // La sincronización completa recorre varios meses en el backend y puede tardar
            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(CosplayJournalApi::class.java)
        }
    }
}

/** Equivale a `PageResult<EventResponse>` del backend. */
@Serializable
data class RemoteEventPage(
    val content: List<RemoteEvent>,
    val last: Boolean
)

/** Equivale a `EventResponse` del backend. Las fechas llegan como `YYYY-MM-DD`. */
@Serializable
data class RemoteEvent(
    val id: String,
    val name: String,
    val description: String? = null,
    val startDate: String,
    val endDate: String,
    val city: String? = null,
    val venue: String? = null,
    val address: String? = null,
    val website: String? = null
)

@Serializable
data class RegisterBody(val username: String, val email: String, val password: String)

@Serializable
data class LoginBody(val email: String, val password: String)

/** Equivale a `AuthTokenResponse` del backend. */
@Serializable
data class RemoteToken(val accessToken: String, val tokenType: String = "Bearer", val expiresIn: Long = 0)

/** Equivale a `OwnProfile` del backend. */
@Serializable
data class RemoteOwnProfile(
    val nickname: String,
    val email: String,
    val avatarUrl: String? = null,
    val memberSince: String? = null
)

@Serializable
data class AvatarBody(val avatarUrl: String?)
