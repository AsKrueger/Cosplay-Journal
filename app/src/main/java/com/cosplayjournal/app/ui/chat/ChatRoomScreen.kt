package com.cosplayjournal.app.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.time.Instant
import java.time.Duration
import androidx.compose.material.icons.outlined.BrokenImage
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.chat.Mensaje
import com.cosplayjournal.app.data.chat.ResumenChat
import com.cosplayjournal.app.data.social.Contacto
import com.cosplayjournal.app.data.social.urlPrivada
import com.cosplayjournal.app.ui.common.Avatar
import com.cosplayjournal.app.ui.common.Cargando

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    vm: ChatRoomViewModel,
    resumen: ResumenChat?,
    amigos: List<Contacto>,
    onAtras: () -> Unit,
    onEvento: (Long) -> Unit,
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var texto by rememberSaveable { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    var dialogo by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val lista = rememberLazyListState()
    val esGrupo = resumen?.esGrupo == true
    val nombres = ui.info?.miembros?.associate { it.id to it.nickname }.orEmpty()
    val foto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { vm.enviarFoto(it.toString()) }
    }

    LaunchedEffect(ui.salido) { if (ui.salido) onAtras() }
    LaunchedEffect(ui.mensajes.firstOrNull()?.id) { if (lista.firstVisibleItemIndex <= 2) lista.animateScrollToItem(0) }
    ui.error?.let { res ->
        val t = stringResource(res)
        LaunchedEffect(res) { snackbar.showSnackbar(t); vm.errorVisto() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onAtras) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.accion_atras)) } },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!esGrupo && resumen != null) Avatar(resumen.nombre.ifEmpty { "?" }, resumen.otroAvatar, 36.dp)
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(resumen?.nombre.orEmpty(), maxLines = 1)
                            if (esGrupo) ui.info?.let {
                                Text(stringResource(R.string.chat_miembros, it.miembros.size), style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.clickable { dialogo = "miembros" })
                            }
                        }
                    }
                },
                actions = {
                    resumen?.eventoId?.let { id ->
                        IconButton(onClick = { onEvento(id) }) { Icon(Icons.Default.Event, stringResource(R.string.chat_ver_salon)) }
                    }
                    if (esGrupo) {
                        IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, null) }
                        DropdownMenu(menu, { menu = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.chat_anadir_miembro)) },
                                leadingIcon = { Icon(Icons.Default.PersonAdd, null) },
                                onClick = { menu = false; dialogo = "añadir" })
                            DropdownMenuItem(text = { Text(stringResource(R.string.chat_salir)) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                                onClick = { menu = false; dialogo = "salir" })
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(8.dp), verticalAlignment = Alignment.Bottom) {
                    IconButton(onClick = { foto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Icon(Icons.Default.AddPhotoAlternate, stringResource(R.string.chat_enviar_foto))
                    }
                    OutlinedTextField(
                        texto, { texto = it.take(2000) }, placeholder = { Text(stringResource(R.string.chat_escribe)) },
                        shape = RoundedCornerShape(24.dp), maxLines = 5,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    )
                    FilledIconButton(
                        onClick = { vm.enviarTexto(texto); texto = "" },
                        enabled = texto.isNotBlank() && !ui.enviando,
                        modifier = Modifier.padding(start = 6.dp),
                    ) {
                        if (ui.enviando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Icon(Icons.AutoMirrored.Filled.Send, stringResource(R.string.accion_enviar))
                    }
                }
            }
        },
    ) { padding ->
        if (ui.cargando && ui.mensajes.isEmpty()) { Cargando(Modifier.padding(padding)); return@Scaffold }
        LazyColumn(
            state = lista, reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            items(ui.mensajes, key = { it.id }) { m ->
                Burbuja(m, mio = m.autor == vm.yo, autor = if (esGrupo && m.autor != vm.yo) nombres[m.autor] else null)
            }
            if (ui.hayMas && ui.mensajes.isNotEmpty()) {
                item {
                    TextButton(onClick = vm::cargarMas, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.chat_cargar_mas)) }
                }
            }
        }
    }

    when (dialogo) {
        "miembros" -> AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text(stringResource(R.string.chat_miembros, ui.info?.miembros?.size ?: 0)) },
            text = {
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(ui.info?.miembros.orEmpty(), key = { it.id }) { p ->
                        ListItem(leadingContent = { Avatar(p.nickname, p.avatarPath, 36.dp) }, headlineContent = { Text(p.nickname) })
                    }
                }
            },
            confirmButton = { TextButton(onClick = { dialogo = null }) { Text(stringResource(R.string.accion_cerrar)) } },
        )
        "añadir" -> {
            val ya = ui.info?.miembros?.map { it.id }.orEmpty().toSet()
            AlertDialog(
                onDismissRequest = { dialogo = null },
                title = { Text(stringResource(R.string.chat_anadir_miembro)) },
                text = {
                    LazyColumn(Modifier.heightIn(max = 360.dp)) {
                        items(amigos.filter { it.id !in ya }, key = { it.id }) { a ->
                            ListItem(
                                leadingContent = { Avatar(a.nickname, a.avatarPath, 36.dp) },
                                headlineContent = { Text(a.nickname) },
                                modifier = Modifier.clickable { vm.añadirMiembro(a.id); dialogo = null },
                            )
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { dialogo = null }) { Text(stringResource(R.string.accion_cerrar)) } },
            )
        }
        "salir" -> AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text(stringResource(R.string.chat_salir)) },
            text = { Text(stringResource(R.string.chat_salir_texto)) },
            confirmButton = { TextButton(onClick = { vm.salir(); dialogo = null }) { Text(stringResource(R.string.chat_salir), color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { dialogo = null }) { Text(stringResource(R.string.accion_cancelar)) } },
        )
    }
}

@Composable
private fun Burbuja(m: Mensaje, mio: Boolean, autor: String?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mio) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(18.dp))
                .background(if (mio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(if (m.foto != null) 4.dp else 12.dp)
        ) {
            autor?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 2.dp))
            }
            m.foto?.let {
                var fallo by remember(it) { mutableStateOf(false) }
                if (fallo) {
                    // Las fotos del chat se borran a los 90 días para no llenar el almacenamiento gratuito
                    val caducada = m.instante.isBefore(Instant.now().minus(Duration.ofDays(90)))
                    val color = if (mio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.BrokenImage, null, tint = color, modifier = Modifier.size(20.dp))
                        Text(stringResource(if (caducada) R.string.chat_foto_caducada else R.string.chat_foto_error),
                            color = color, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                    }
                } else {
                    AsyncImage(model = urlPrivada("chat", it), contentDescription = null, contentScale = ContentScale.Crop,
                        onError = { fallo = true },
                        modifier = Modifier.size(240.dp).clip(RoundedCornerShape(14.dp)))
                }
            }
            m.body?.let {
                Text(it, color = if (mio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            }
            Text(horaCorta(m.instante), style = MaterialTheme.typography.labelSmall,
                color = (if (mio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End).padding(horizontal = 6.dp))
        }
    }
}
