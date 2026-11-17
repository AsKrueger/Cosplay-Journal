package com.cosplayjournal.app.data.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Etiquetas de una foto de la carpeta vinculada. La foto en sí no se copia ni se sube. */
@Entity(tableName = "fotos", indices = [Index("evento_id"), Index("cosplay")])
data class FotoEtiquetas(
    @PrimaryKey val uri: String,
    @ColumnInfo(name = "evento_id") val eventoId: Long? = null,
    val cosplay: String? = null,
    val grupal: Boolean = false,
)

@Dao
interface FotoDao {
    @Query("SELECT * FROM fotos") fun todas(): Flow<List<FotoEtiquetas>>
    @Upsert suspend fun guardar(fotos: List<FotoEtiquetas>)
    @Query("SELECT DISTINCT cosplay FROM fotos WHERE cosplay IS NOT NULL ORDER BY cosplay COLLATE NOCASE")
    fun cosplays(): Flow<List<String>>
    @Query("DELETE FROM fotos") suspend fun borrarTodo()
}

/** Imagen encontrada en la carpeta. */
data class Foto(val uri: String, val nombre: String, val fecha: Long)

/** Lee las imágenes de una carpeta elegida con el selector de documentos (sin permisos de almacenamiento). */
interface LectorCarpeta {
    suspend fun imagenes(carpeta: String): List<Foto>
    fun conservarPermiso(carpeta: String)
    fun soltarPermiso(carpeta: String)
}

class LectorCarpetaSaf(private val context: Context) : LectorCarpeta {
    private val resolver get() = context.contentResolver

    override fun conservarPermiso(carpeta: String) {
        resolver.takePersistableUriPermission(Uri.parse(carpeta), Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    override fun soltarPermiso(carpeta: String) {
        runCatching { resolver.releasePersistableUriPermission(Uri.parse(carpeta), Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }

    override suspend fun imagenes(carpeta: String): List<Foto> = withContext(Dispatchers.IO) {
        val fotos = mutableListOf<Foto>()
        val arbol = Uri.parse(carpeta)
        recorrer(arbol, DocumentsContract.getTreeDocumentId(arbol), profundidad = 0, fotos)
        fotos.sortedByDescending { it.fecha }
    }

    /** Recorre la carpeta y hasta 2 niveles de subcarpetas (p. ej. "Salones/2026/Japan Weekend"). */
    private fun recorrer(arbol: Uri, docId: String, profundidad: Int, fuera: MutableList<Foto>) {
        val hijos = DocumentsContract.buildChildDocumentsUriUsingTree(arbol, docId)
        val columnas = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
        resolver.query(hijos, columnas, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(0)
                val nombre = c.getString(1) ?: ""
                val mime = c.getString(2) ?: ""
                when {
                    mime == DocumentsContract.Document.MIME_TYPE_DIR && profundidad < 2 ->
                        recorrer(arbol, id, profundidad + 1, fuera)
                    mime.startsWith("image/") && !nombre.startsWith(".") ->
                        fuera += Foto(DocumentsContract.buildDocumentUriUsingTree(arbol, id).toString(), nombre, c.getLong(3))
                }
            }
        }
    }
}
