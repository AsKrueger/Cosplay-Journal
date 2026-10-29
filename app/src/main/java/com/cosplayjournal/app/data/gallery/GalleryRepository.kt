package com.cosplayjournal.app.data.gallery

import com.cosplayjournal.app.data.remote.GalleryApi
import com.cosplayjournal.app.data.remote.RemoteGalleryPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Galería personal. Los errores de la API llegan como HttpException con su código. */
class GalleryRepository(private val api: GalleryApi) {

    suspend fun photos(tag: String?, eventId: String?): List<RemoteGalleryPhoto> = withContext(Dispatchers.IO) {
        api.photos(tag, eventId)
    }

    suspend fun upload(bytes: ByteArray, contentType: String, tag: String, eventId: String?): RemoteGalleryPhoto =
        withContext(Dispatchers.IO) {
            val file = MultipartBody.Part.createFormData(
                "file",
                "foto",
                bytes.toRequestBody(contentType.toMediaType())
            )
            api.upload(
                file = file,
                tag = tag.toRequestBody("text/plain".toMediaType()),
                eventId = eventId?.toRequestBody("text/plain".toMediaType())
            )
        }
}
