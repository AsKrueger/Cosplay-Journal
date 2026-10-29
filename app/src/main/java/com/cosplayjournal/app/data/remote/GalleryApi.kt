package com.cosplayjournal.app.data.remote

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/** Galería personal (`/api/v1/gallery/photos`). Requiere sesión. */
interface GalleryApi {

    @Multipart
    @POST("api/v1/gallery/photos")
    suspend fun upload(
        @Part file: MultipartBody.Part,
        @Part("tag") tag: RequestBody,
        @Part("eventId") eventId: RequestBody?
    ): RemoteGalleryPhoto

    @GET("api/v1/gallery/photos")
    suspend fun photos(@Query("tag") tag: String?, @Query("eventId") eventId: String?): List<RemoteGalleryPhoto>
}

/** Equivale a `GalleryPhotoView` del backend. */
@Serializable
data class RemoteGalleryPhoto(
    val id: String,
    val tag: String,
    val eventId: String? = null,
    val url: String,
    val uploadedAt: String
)
