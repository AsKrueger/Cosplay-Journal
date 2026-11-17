package com.cosplayjournal.app.ui.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.gallery.FotoGaleria
import com.cosplayjournal.app.domain.Orden
import com.cosplayjournal.app.ui.calendar.rangoFechas
import com.cosplayjournal.app.ui.common.Aviso
import com.cosplayjournal.app.ui.common.Cargando

/** Acción extra del visor (la fase social añade "Al escaparate"). */
data class AccionFoto(val icono: androidx.compose.ui.graphics.vector.ImageVector, val texto: Int, val accion: (FotoGaleria) -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(vm: GalleryViewModel, accionesExtra: List<AccionFoto> = emptyList()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var etiquetando by remember { mutableStateOf<Set<String>?>(null) }
    var visor by rememberSaveable { mutableStateOf<String?>(null) }
    val elegirCarpeta = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { vm.vincular(it.toString()) }
    }

    Scaffold(
        topBar = {
            if (ui.seleccionando) {
                TopAppBar(
                    title = { Text(stringResource(R.string.gal_seleccionadas, ui.seleccion.size)) },
                    navigationIcon = { IconButton(onClick = vm::limpiarSeleccion) { Icon(Icons.Default.Close, null) } },
                    actions = {
                        TextButton(onClick = { etiquetando = ui.seleccion }) {
                            Icon(Icons.Default.Label, null)
                            Text(stringResource(R.string.gal_etiquetar), Modifier.padding(start = 6.dp))
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_galeria)) },
                    actions = {
                        if (ui.carpeta != null) {
                            IconButton(onClick = { elegirCarpeta.launch(null) }) {
                                Icon(Icons.Default.FolderOpen, stringResource(R.string.gal_cambiar_carpeta))
                            }
                        }
                    },
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                ui.cargando -> Cargando()
                ui.carpeta == null -> Aviso(
                    Icons.Default.PhotoLibrary, stringResource(R.string.gal_vincular_titulo),
                    stringResource(R.string.gal_vincular_texto), accion = R.string.gal_elegir_carpeta,
                    onAccion = { elegirCarpeta.launch(null) },
                )
                else -> Column {
                    Ordenes(ui.orden, vm::orden)
                    PullToRefreshBox(isRefreshing = ui.escaneando, onRefresh = { vm.escanear() }) {
                        Rejilla(ui, onFoto = { f ->
                            if (ui.seleccionando) vm.alternar(f.foto.uri) else visor = f.foto.uri
                        }, onLargo = { vm.alternar(it.foto.uri) }, onGrupo = vm::seleccionar)
                    }
                }
            }
        }
    }

    etiquetando?.let { uris ->
        HojaEtiquetar(vm, uris, onCerrar = { etiquetando = null })
    }
    visor?.let { inicial ->
        Visor(ui.visibles, inicial, accionesExtra,
            onEtiquetar = { etiquetando = setOf(it) }, onCerrar = { visor = null })
    }
}

@Composable
private fun Ordenes(actual: Orden, onOrden: (Orden) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            Orden.TODAS to R.string.gal_orden_todas, Orden.EVENTO to R.string.gal_orden_evento,
            Orden.COSPLAY to R.string.gal_orden_cosplay, Orden.GRUPAL to R.string.gal_orden_grupal,
        ).forEach { (o, res) ->
            FilterChip(selected = actual == o, onClick = { onOrden(o) }, label = { Text(stringResource(res)) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Rejilla(
    ui: GaleriaUi,
    onFoto: (FotoGaleria) -> Unit,
    onLargo: (FotoGaleria) -> Unit,
    onGrupo: (Collection<String>) -> Unit,
) {
    if (ui.total == 0) {
        Aviso(Icons.Default.HideImage, stringResource(R.string.gal_vacia_titulo), stringResource(R.string.gal_vacia_texto))
        return
    }
    if (ui.orden == Orden.GRUPAL && ui.grupos.all { it.fotos.isEmpty() }) {
        Aviso(Icons.Default.Groups, stringResource(R.string.gal_sin_grupales))
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        ui.grupos.forEach { g ->
            if (ui.orden == Orden.EVENTO || ui.orden == Orden.COSPLAY) {
                item(key = "h" + g.clave, span = { GridItemSpan(maxLineSpan) }) {
                    Row(Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                g.titulo ?: stringResource(if (ui.orden == Orden.EVENTO) R.string.gal_sin_evento else R.string.gal_sin_cosplay),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(stringResource(R.string.gal_n_fotos, g.fotos.size), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onGrupo(g.fotos.map { it.foto.uri }) }) {
                            Text(stringResource(R.string.gal_seleccionar_grupo))
                        }
                    }
                }
            }
            items(g.fotos, key = { g.clave + it.foto.uri }) { f ->
                val marcada = f.foto.uri in ui.seleccion
                Box(
                    Modifier.aspectRatio(1f).padding(2.dp).clip(RoundedCornerShape(10.dp))
                        .combinedClickable(onClick = { onFoto(f) }, onLongClick = { onLargo(f) })
                        .then(if (marcada) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)) else Modifier)
                ) {
                    Miniatura(f.foto.uri, Modifier.fillMaxSize())
                    if (marcada) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.White, CircleShape))
                    } else if (f.etiquetas.grupal) {
                        Icon(Icons.Default.Groups, null, tint = Color.White,
                            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Miniatura(uri: String, modifier: Modifier) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context).data(Uri.parse(uri)).size(360).build(),
        contentDescription = null, contentScale = ContentScale.Crop,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaEtiquetar(vm: GalleryViewModel, uris: Set<String>, onCerrar: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val propuesta = remember(uris) { vm.propuesta(uris) }
    var eventoId by remember { mutableStateOf(propuesta.evento?.evento?.id) }
    var cosplay by remember { mutableStateOf(propuesta.cosplay ?: "") }
    var grupal by remember { mutableStateOf(propuesta.grupal) }
    var busqueda by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onCerrar, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.gal_etiquetar_titulo), style = MaterialTheme.typography.titleLarge)

            Text(stringResource(R.string.gal_evento), style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = busqueda, onValueChange = { busqueda = it }, singleLine = true,
                placeholder = { Text(stringResource(R.string.gal_buscar_evento)) }, modifier = Modifier.fillMaxWidth(),
            )
            val sugerido = propuesta.evento
            val lista = remember(busqueda, ui.eventos) {
                val q = busqueda.trim().lowercase()
                ui.eventos.filter { q.isEmpty() || it.evento.nombre.lowercase().contains(q) }
                    .sortedByDescending { it.evento.inicio }
                    .let { l -> if (sugerido != null && q.isEmpty()) listOf(sugerido) + (l - sugerido) else l }
                    .take(40)
            }
            LazyColumn(Modifier.heightIn(max = 220.dp)) {
                item {
                    ListItem(
                        leadingContent = { RadioButton(selected = eventoId == null, onClick = { eventoId = null }) },
                        headlineContent = { Text(stringResource(R.string.gal_ningun_evento)) },
                        modifier = Modifier.clickable { eventoId = null },
                    )
                }
                items(lista, key = { it.evento.id }) { e ->
                    ListItem(
                        leadingContent = { RadioButton(selected = eventoId == e.evento.id, onClick = { eventoId = e.evento.id }) },
                        headlineContent = { Text(e.evento.nombre) },
                        supportingContent = {
                            Text(
                                if (e == sugerido) stringResource(R.string.gal_sugerido)
                                else rangoFechas(e.evento.inicio, e.evento.fin)
                            )
                        },
                        modifier = Modifier.clickable { eventoId = e.evento.id },
                    )
                }
            }

            OutlinedTextField(
                value = cosplay, onValueChange = { cosplay = it.take(60) }, singleLine = true,
                label = { Text(stringResource(R.string.gal_cosplay)) }, modifier = Modifier.fillMaxWidth(),
            )
            val sugerencias = ui.cosplays.filter { it.contains(cosplay.trim(), ignoreCase = true) && !it.equals(cosplay.trim(), true) }
            if (sugerencias.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sugerencias.take(8).forEach { s -> AssistChip(onClick = { cosplay = s }, label = { Text(s) }) }
                }
            }

            Row(Modifier.fillMaxWidth().clickable { grupal = !grupal }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, null)
                Text(stringResource(R.string.gal_grupal), Modifier.weight(1f).padding(start = 12.dp))
                Switch(grupal, onCheckedChange = { grupal = it })
            }

            Button(
                onClick = { vm.etiquetar(uris, eventoId, cosplay, grupal); onCerrar() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            ) { Text(stringResource(R.string.gal_guardar)) }
        }
    }
}

@Composable
private fun Visor(
    fotos: List<FotoGaleria>,
    inicial: String,
    accionesExtra: List<AccionFoto>,
    onEtiquetar: (String) -> Unit,
    onCerrar: () -> Unit,
) {
    val context = LocalContext.current
    val pager = rememberPagerState(initialPage = fotos.indexOfFirst { it.foto.uri == inicial }.coerceAtLeast(0)) { fotos.size }
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(pager, Modifier.fillMaxSize()) { i ->
                AsyncImage(model = Uri.parse(fotos[i].foto.uri), contentDescription = null,
                    contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            }
            IconButton(onClick = onCerrar, modifier = Modifier.statusBarsPadding().padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.accion_atras), tint = Color.White)
            }
            val actual = fotos.getOrNull(pager.currentPage) ?: return@Box
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding()
                    .background(Color.Black.copy(alpha = 0.5f)).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BotonVisor(Icons.Default.Label, R.string.gal_etiquetar) { onEtiquetar(actual.foto.uri) }
                BotonVisor(Icons.Default.Share, R.string.gal_compartir) { compartir(context, actual.foto.uri) }
                accionesExtra.forEach { a -> BotonVisor(a.icono, a.texto) { a.accion(actual) } }
            }
        }
    }
}

@Composable
private fun BotonVisor(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: Int, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, null, tint = Color.White)
            Text(stringResource(texto), color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun compartir(context: Context, uri: String) {
    val i = Intent(Intent.ACTION_SEND).setType("image/*")
        .putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(i, null))
}
