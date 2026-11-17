package com.cosplayjournal.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.chat.ResumenChat
import com.cosplayjournal.app.data.events.EventoConEstado
import com.cosplayjournal.app.ui.common.Avatar
import com.cosplayjournal.app.ui.common.Aviso
import com.cosplayjournal.app.ui.common.Cargando
import com.cosplayjournal.app.ui.theme.DegradadoMarca
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(vm: ChatsViewModel, misEventos: List<EventoConEstado>, onChat: (String) -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var nuevoGrupo by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.refrescar() }
    LaunchedEffect(ui.abrir) { ui.abrir?.let { onChat(it); vm.abierto() } }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_chats)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.cargarAmigos(); nuevoGrupo = true },
                icon = { Icon(Icons.Default.GroupAdd, null) },
                text = { Text(stringResource(R.string.chat_nuevo_grupo)) },
            )
        },
    ) { padding ->
        PullToRefreshBox(isRefreshing = false, onRefresh = { vm.refrescar() }, modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                ui.cargando -> Cargando()
                ui.chats.isEmpty() -> Aviso(
                    Icons.AutoMirrored.Filled.Chat, stringResource(R.string.chat_vacio_titulo), stringResource(R.string.chat_vacio_texto),
                )
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(ui.chats, key = { it.id }) { c -> FilaChat(c) { onChat(c.id) } }
                }
            }
        }
    }

    if (nuevoGrupo) {
        DialogoGrupo(ui, misEventos, onCerrar = { nuevoGrupo = false }) { titulo, miembros, evento ->
            vm.crearGrupo(titulo, miembros, evento); nuevoGrupo = false
        }
    }
}

@Composable
private fun FilaChat(c: ResumenChat, onClick: () -> Unit) {
    ListItem(
        leadingContent = {
            if (c.esGrupo) {
                Box(Modifier.size(48.dp).background(DegradadoMarca, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Groups, null, tint = Color.White)
                }
            } else Avatar(c.nombre.ifEmpty { "?" }, c.otroAvatar, 48.dp)
        },
        headlineContent = {
            Text(c.nombre.ifEmpty { stringResource(R.string.chat_usuario_eliminado) },
                fontWeight = if (c.unread > 0) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
        },
        supportingContent = {
            Text(
                c.ultimoTexto ?: if (c.ultimoEsFoto == true) stringResource(R.string.chat_foto) else "",
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                c.instante?.let { Text(horaCorta(it), style = MaterialTheme.typography.labelSmall) }
                if (c.unread > 0) Badge(Modifier.padding(top = 4.dp)) { Text("${c.unread}") }
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
fun horaCorta(i: Instant): String {
    val z = i.atZone(ZoneId.systemDefault())
    val hoy = LocalDate.now()
    return when (z.toLocalDate()) {
        hoy -> z.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        hoy.minusDays(1) -> stringResource(R.string.chat_ayer)
        else -> z.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoGrupo(
    ui: ChatsUi,
    misEventos: List<EventoConEstado>,
    onCerrar: () -> Unit,
    onCrear: (String, List<String>, Long?) -> Unit,
) {
    var titulo by remember { mutableStateOf("") }
    val elegidos = remember { mutableStateListOf<String>() }
    var evento by remember { mutableStateOf<EventoConEstado?>(null) }
    var desplegado by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(R.string.chat_nuevo_grupo)) },
        text = {
            Column {
                OutlinedTextField(titulo, { titulo = it.take(60) }, singleLine = true,
                    label = { Text(stringResource(R.string.chat_nombre_grupo)) }, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(desplegado, { desplegado = it }, Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = evento?.evento?.nombre ?: stringResource(R.string.chat_ningun_evento), onValueChange = {},
                        readOnly = true, label = { Text(stringResource(R.string.chat_grupo_evento)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(desplegado) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(desplegado, { desplegado = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.chat_ningun_evento)) }, onClick = { evento = null; desplegado = false })
                        misEventos.forEach { e ->
                            DropdownMenuItem(text = { Text(e.evento.nombre) }, onClick = {
                                evento = e; desplegado = false
                                if (titulo.isBlank()) titulo = e.evento.nombre.take(60)
                            })
                        }
                    }
                }
                Text(stringResource(R.string.chat_elige_amigos), style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 12.dp))
                if (ui.amigos.isEmpty()) Text(stringResource(R.string.chat_sin_amigos), color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.heightIn(max = 240.dp)) {
                    items(ui.amigos, key = { it.id }) { a ->
                        val marcado = a.id in elegidos
                        val alternar = { if (marcado) elegidos.remove(a.id) else elegidos.add(a.id); Unit }
                        ListItem(
                            leadingContent = { Avatar(a.nickname, a.avatarPath, 36.dp) },
                            headlineContent = { Text(a.nickname) },
                            trailingContent = { Checkbox(marcado, { alternar() }) },
                            modifier = Modifier.clickable(onClick = alternar),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCrear(titulo, elegidos.toList(), evento?.evento?.id) },
                enabled = titulo.isNotBlank() && elegidos.isNotEmpty()) { Text(stringResource(R.string.chat_crear)) }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.accion_cancelar)) } },
    )
}
