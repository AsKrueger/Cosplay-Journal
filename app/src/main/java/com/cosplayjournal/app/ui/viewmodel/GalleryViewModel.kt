package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.gallery.GalleryRepository
import com.cosplayjournal.app.data.remote.RemoteGalleryPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class GalleryUiState(
    val photos: List<RemoteGalleryPhoto> = emptyList(),
    /** Etiqueta por la que se filtra (EVENTO, COSPLAY, GRUPAL), o null para ver todas. */
    val tagFilter: String? = null,
    val busy: Boolean = false,
    val message: String? = null
)

/** Galería personal: lista, filtro por etiqueta y subida de fotos. */
class GalleryViewModel(private val repository: GalleryRepository) : ViewModel() {

    private val _state = MutableStateFlow(GalleryUiState())
    val state: StateFlow<GalleryUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun setFilter(tag: String?) {
        _state.value = _state.value.copy(tagFilter = tag)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                val photos = repository.photos(_state.value.tagFilter, null)
                _state.value = _state.value.copy(photos = photos, message = null)
            } catch (e: HttpException) {
                _state.value = _state.value.copy(message = messageFor(e.code()))
            } catch (e: IOException) {
                _state.value = _state.value.copy(message = "Sin conexión con el servidor")
            }
        }
    }

    /** Sube una foto y la añade al principio de la lista. El evento solo se envía con la etiqueta EVENTO. */
    fun upload(bytes: ByteArray, contentType: String, tag: String, eventId: String?) {
        if (bytes.size > MAX_BYTES) {
            _state.value = _state.value.copy(message = "La imagen supera el tamaño máximo de 5 MB")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            try {
                val sent = repository.upload(bytes, contentType, tag, if (tag == "EVENTO") eventId else null)
                val visible = _state.value.tagFilter == null || _state.value.tagFilter == sent.tag
                _state.value = _state.value.copy(
                    busy = false,
                    photos = if (visible) listOf(sent) + _state.value.photos else _state.value.photos,
                    message = "Foto subida"
                )
            } catch (e: HttpException) {
                _state.value = _state.value.copy(busy = false, message = messageFor(e.code()))
            } catch (e: IOException) {
                _state.value = _state.value.copy(busy = false, message = "Sin conexión: la foto no se ha subido")
            }
        }
    }

    private fun messageFor(code: Int): String = when (code) {
        400 -> "La foto no es válida: usa JPEG, PNG o WebP de hasta 5 MB"
        401 -> "Inicia sesión para usar tu galería"
        else -> "No se pudo completar la operación (código " + code + ")"
    }

    private companion object {
        const val MAX_BYTES = 5 * 1024 * 1024
    }
}

class GalleryViewModelFactory(private val repository: GalleryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GalleryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
