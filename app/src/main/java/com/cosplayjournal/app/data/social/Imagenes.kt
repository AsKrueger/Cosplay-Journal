package com.cosplayjournal.app.data.social

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Prepara una foto para subirla: máximo 1280 px y unos 300 KB en WebP. Así el escaparate y los
 * chats caben de sobra en el almacenamiento gratuito.
 */
class CompresorImagenes(private val context: Context) {

    suspend fun comprimir(uri: String, ladoMax: Int = 1280, maxBytes: Int = 300_000): ByteArray = withContext(Dispatchers.IO) {
        val u = Uri.parse(uri)
        val resolver = context.contentResolver
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(u).use { BitmapFactory.decodeStream(it, null, limites) }
        var muestra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / (muestra * 2) >= ladoMax) muestra *= 2

        val original = resolver.openInputStream(u).use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestra })
        } ?: error("imagen ilegible")
        val girada = girarSegunExif(original, resolver.openInputStream(u)?.use { ExifInterface(it) })
        val escala = ladoMax.toFloat() / maxOf(girada.width, girada.height)
        val final = if (escala < 1f) {
            Bitmap.createScaledBitmap(girada, (girada.width * escala).toInt(), (girada.height * escala).toInt(), true)
        } else girada

        val formato = if (Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY
        else @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
        var calidad = 85
        var bytes: ByteArray
        do {
            bytes = ByteArrayOutputStream().use { final.compress(formato, calidad, it); it.toByteArray() }
            calidad -= 10
        } while (bytes.size > maxBytes && calidad >= 35)
        bytes
    }

    private fun girarSegunExif(b: Bitmap, exif: ExifInterface?): Bitmap {
        val grados = when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return b
        }
        return Bitmap.createBitmap(b, 0, 0, b.width, b.height, Matrix().apply { postRotate(grados) }, true)
    }
}
