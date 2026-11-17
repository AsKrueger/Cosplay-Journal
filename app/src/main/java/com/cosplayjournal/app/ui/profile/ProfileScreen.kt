package com.cosplayjournal.app.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.social.FotoEscaparate
import com.cosplayjournal.app.data.social.urlPrivada
import com.cosplayjournal.app.ui.calendar.TarjetaEvento
import com.cosplayjournal.app.ui.common.Avatar
import com.cosplayjournal.app.ui.theme.DegradadoMarca
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(vm: ProfileViewModel, onAjustes: () -> Unit, onEvento: (Long) -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var quitar by remember { mutableStateOf<FotoEscaparate?>(null) }
    val elegirAvatar = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { vm.cambiarAvatar(it.toString()) }
    }
    ui.mensaje?.let { res ->
        val t = stringResource(res)
        LaunchedEffect(res) { Toast.makeText(context, t, Toast.LENGTH_SHORT).show(); vm.mensajeVisto() }
    }

    LazyVerticalGrid(
        GridCells.Fixed(3), Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Cabecera(ui, onAjustes, onCambiarFoto = {
                elegirAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            })
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.perfil_mi_escaparate), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.perfil_escaparate_n, ui.escaparate.size), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (ui.escaparate.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(R.string.perfil_escaparate_ayuda), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp))
            }
        }
        items(ui.escaparate, key = { it.id }) { f ->
            AsyncImage(
                model = urlPrivada("showcase", f.ruta), contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(onClick = {}, onLongClick = { quitar = f }),
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(stringResource(R.string.perfil_proximos), style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp))
        }
        if (ui.proximos.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(R.string.perfil_sin_proximos), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp))
            }
        }
        items(ui.proximos, key = { "e" + it.evento.id }, span = { GridItemSpan(maxLineSpan) }) { e ->
            TarjetaEvento(e, LocalDate.now(), onClick = { onEvento(e.evento.id) }, onFavorito = {})
        }
    }

    quitar?.let { f ->
        AlertDialog(
            onDismissRequest = { quitar = null },
            title = { Text(stringResource(R.string.perfil_quitar_foto)) },
            confirmButton = { TextButton(onClick = { vm.quitar(f); quitar = null }) { Text(stringResource(R.string.perfil_quitar)) } },
            dismissButton = { TextButton(onClick = { quitar = null }) { Text(stringResource(R.string.accion_cancelar)) } },
        )
    }
}

@Composable
private fun Cabecera(ui: MiPerfilUi, onAjustes: () -> Unit, onCambiarFoto: () -> Unit) {
    val context = LocalContext.current
    val perfil = ui.perfil
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(DegradadoMarca).statusBarsPadding().padding(20.dp)
    ) {
        IconButton(onClick = onAjustes, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Settings, stringResource(R.string.perfil_ajustes), tint = Color.White)
        }
        Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.clickable(onClick = onCambiarFoto)) {
                Avatar(perfil?.nickname ?: " ", perfil?.avatarPath, 104.dp)
                Box(
                    Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    if (ui.subiendo) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.CameraAlt, stringResource(R.string.perfil_cambiar_foto), Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text(perfil?.nickname ?: "…", style = MaterialTheme.typography.headlineSmall, color = Color.White,
                modifier = Modifier.padding(top = 12.dp))
            perfil?.codigoAmigo?.let { codigo ->
                Card(Modifier.padding(top = 12.dp)) {
                    Row(Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Column {
                            Text(stringResource(R.string.perfil_codigo_amigo), style = MaterialTheme.typography.labelSmall)
                            Text(codigo, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { copiar(context, codigo) }) { Icon(Icons.Default.ContentCopy, null) }
                        IconButton(onClick = { compartir(context, codigo) }) { Icon(Icons.Default.Share, null) }
                    }
                }
            }
        }
    }
}

private fun copiar(context: Context, codigo: String) {
    context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("codigo", codigo))
    Toast.makeText(context, R.string.perfil_codigo_copiado, Toast.LENGTH_SHORT).show()
}

private fun compartir(context: Context, codigo: String) {
    val i = Intent(Intent.ACTION_SEND).setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, context.getString(R.string.perfil_compartir_codigo, codigo))
    context.startActivity(Intent.createChooser(i, null))
}
