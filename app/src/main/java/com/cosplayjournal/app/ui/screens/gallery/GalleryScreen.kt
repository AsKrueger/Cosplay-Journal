package com.cosplayjournal.app.ui.screens.gallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.data.remote.RemoteGalleryPhoto
import com.cosplayjournal.app.ui.viewmodel.GalleryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FILTERS = listOf(null to "Todas", "COSPLAY" to "Cosplay", "GRUPAL" to "Grupal", "EVENTO" to "Evento")
private val UPLOAD_TAGS = listOf("COSPLAY" to "Cosplay", "GRUPAL" to "Grupal", "EVENTO" to "Evento")

/** Galería personal: fotos etiquetadas por cosplay, grupal o evento, con filtro y subida. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    events: List<Event>,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<PendingPhoto?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val type = context.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            if (bytes != null) pending = PendingPhoto(bytes, type)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galería", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Button(
                onClick = { picker.launch("image/*") },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (state.busy) "Subiendo..." else "Subir foto") }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FILTERS.forEach { (tag, label) ->
                    FilterChip(
                        selected = state.tagFilter == tag,
                        onClick = { viewModel.setFilter(tag) },
                        label = { Text(label) }
                    )
                }
            }

            state.message?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (state.photos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Aún no tienes fotos aquí. Sube una y elige si es de cosplay, grupal o de un evento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.photos, key = { it.id }) { photo -> PhotoTile(photo) }
                }
            }
        }
    }

    pending?.let { photo ->
        UploadDialog(
            events = events,
            busy = state.busy,
            onDismiss = { pending = null },
            onConfirm = { tag, eventId ->
                viewModel.upload(photo.bytes, photo.contentType, tag, eventId)
                pending = null
            }
        )
    }
}

private data class PendingPhoto(val bytes: ByteArray, val contentType: String)

@Composable
private fun PhotoTile(photo: RemoteGalleryPhoto) {
    Card(shape = RoundedCornerShape(16.dp)) {
        AsyncImage(
            model = photo.url,
            contentDescription = "Foto de tu galería",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

@Composable
private fun UploadDialog(
    events: List<Event>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (tag: String, eventId: String?) -> Unit
) {
    var tag by remember { mutableStateOf("COSPLAY") }
    var eventId by remember { mutableStateOf<String?>(null) }
    val canConfirm = !busy && (tag != "EVENTO" || eventId != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿De qué es esta foto?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UPLOAD_TAGS.forEach { (value, label) ->
                        FilterChip(
                            selected = tag == value,
                            onClick = {
                                tag = value
                                if (value != "EVENTO") eventId = null
                            },
                            label = { Text(label) }
                        )
                    }
                }
                if (tag == "EVENTO") {
                    if (events.isEmpty()) {
                        Text("No hay eventos cargados. Abre el calendario primero.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            listItems(events) { event ->
                                Text(
                                    event.name,
                                    color = if (event.id == eventId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (event.id == eventId) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { eventId = event.id }
                                        .padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tag, eventId) }, enabled = canConfirm) { Text("Subir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
