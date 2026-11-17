package com.cosplayjournal.app.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.social.urlPrivada
import com.cosplayjournal.app.ui.calendar.TarjetaEvento
import com.cosplayjournal.app.ui.common.Avatar
import com.cosplayjournal.app.ui.common.Aviso
import com.cosplayjournal.app.ui.common.Cargando
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    vm: FriendProfileViewModel,
    onAtras: () -> Unit,
    onEvento: (Long) -> Unit,
    onEscribir: (() -> Unit)?,
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var menu by remember { mutableStateOf(false) }
    var dialogo by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.cerrar) { if (ui.cerrar) onAtras() }
    ui.mensaje?.let { res ->
        val t = stringResource(res)
        LaunchedEffect(res) { snackbar.showSnackbar(t); vm.mensajeVisto() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ui.perfil?.perfil?.nickname ?: "") },
                navigationIcon = { IconButton(onClick = onAtras) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.accion_atras)) } },
                actions = {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, null) }
                    DropdownMenu(menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.pa_eliminar)) },
                            leadingIcon = { Icon(Icons.Default.PersonRemove, null) },
                            onClick = { menu = false; dialogo = "eliminar" })
                        DropdownMenuItem(text = { Text(stringResource(R.string.pa_bloquear)) },
                            leadingIcon = { Icon(Icons.Default.Block, null) },
                            onClick = { menu = false; dialogo = "bloquear" })
                        DropdownMenuItem(text = { Text(stringResource(R.string.pa_denunciar)) },
                            leadingIcon = { Icon(Icons.Default.Flag, null) },
                            onClick = { menu = false; dialogo = "denunciar" })
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val p = ui.perfil
        when {
            ui.cargando -> Cargando(Modifier.padding(padding))
            p == null -> Aviso(Icons.Default.PersonOff, stringResource(ui.error ?: R.string.pa_no_disponible),
                modifier = Modifier.padding(padding), accion = R.string.accion_reintentar, onAccion = { vm.cargar() })
            else -> LazyVerticalGrid(
                GridCells.Fixed(3), Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Avatar(p.perfil.nickname, p.perfil.avatarPath, 104.dp)
                        Text(p.perfil.nickname, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp))
                        onEscribir?.let {
                            Button(onClick = it, modifier = Modifier.padding(top = 12.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Chat, null)
                                Text(stringResource(R.string.pa_escribir), Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
                if (ui.vaA.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { Titulo(R.string.pa_va_a) }
                    items(ui.vaA, key = { "e" + it.evento.id }, span = { GridItemSpan(maxLineSpan) }) { e ->
                        TarjetaEvento(e, LocalDate.now(), onClick = { onEvento(e.evento.id) }, onFavorito = {}, mostrarMiEstado = false)
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Titulo(R.string.pa_escaparate) }
                if (p.escaparate.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(stringResource(R.string.pa_escaparate_vacio), color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp))
                    }
                }
                items(p.escaparate, key = { it.id }) { f ->
                    AsyncImage(
                        model = urlPrivada("showcase", f.ruta), contentDescription = f.caption, contentScale = ContentScale.Crop,
                        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }

    when (dialogo) {
        "eliminar" -> Confirmar(R.string.pa_eliminar, null, onCerrar = { dialogo = null }) { vm.eliminar() }
        "bloquear" -> Confirmar(R.string.pa_bloquear, R.string.pa_bloquear_texto, onCerrar = { dialogo = null }) { vm.bloquear() }
        "denunciar" -> Denunciar(onCerrar = { dialogo = null }) { vm.denunciar(it) }
    }
}

@Composable
private fun Titulo(res: Int) {
    Text(stringResource(res), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
}

@Composable
private fun Confirmar(titulo: Int, texto: Int?, onCerrar: () -> Unit, onOk: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(titulo)) },
        text = texto?.let { { Text(stringResource(it)) } },
        confirmButton = { TextButton(onClick = { onOk(); onCerrar() }) { Text(stringResource(titulo), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.accion_cancelar)) } },
    )
}

@Composable
private fun Denunciar(onCerrar: () -> Unit, onEnviar: (String) -> Unit) {
    var motivo by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(R.string.pa_denunciar)) },
        text = {
            Column {
                Text(stringResource(R.string.pa_denunciar_texto))
                OutlinedTextField(motivo, { motivo = it.take(1000) }, placeholder = { Text(stringResource(R.string.pa_denunciar_hint)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).padding(top = 12.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = { onEnviar(motivo); onCerrar() }, enabled = motivo.isNotBlank()) {
                Text(stringResource(R.string.accion_enviar))
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.accion_cancelar)) } },
    )
}
