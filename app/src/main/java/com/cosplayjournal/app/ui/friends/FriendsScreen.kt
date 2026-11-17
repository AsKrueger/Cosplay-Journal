package com.cosplayjournal.app.ui.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.social.Contacto
import com.cosplayjournal.app.data.social.Relacion
import com.cosplayjournal.app.ui.common.Aviso
import com.cosplayjournal.app.ui.common.Avatar
import com.cosplayjournal.app.ui.common.Cargando

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(vm: FriendsViewModel, onAmigo: (String) -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    ui.mensaje?.let { res ->
        val texto = stringResource(res)
        LaunchedEffect(res, texto) { snackbar.showSnackbar(texto); vm.mensajeVisto() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_amigos)) }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = ui.busqueda, onValueChange = vm::buscar, singleLine = true,
                placeholder = { Text(stringResource(R.string.ami_buscar)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (ui.busqueda.isNotEmpty()) IconButton(onClick = { vm.buscar("") }) { Icon(Icons.Default.Close, null) }
                },
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                // Un código de amigo exacto (8 caracteres) se puede enviar directamente con el teclado
                keyboardActions = KeyboardActions(onSend = { if (ui.busqueda.trim().length == 8) vm.enviarPorCodigo(ui.busqueda) }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (ui.buscando) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))

            PullToRefreshBox(isRefreshing = false, onRefresh = { vm.refrescar() }, modifier = Modifier.weight(1f)) {
                when {
                    ui.resultados != null -> Resultados(ui, vm, onAmigo)
                    ui.cargando -> Cargando()
                    ui.contactos.isEmpty() -> Aviso(
                        Icons.Default.GroupAdd, stringResource(R.string.ami_vacio_titulo), stringResource(R.string.ami_vacio_texto),
                    )
                    else -> Contactos(ui, vm, onAmigo)
                }
            }
        }
    }
}

@Composable
private fun Resultados(ui: AmigosUi, vm: FriendsViewModel, onAmigo: (String) -> Unit) {
    val lista = ui.resultados.orEmpty()
    if (lista.isEmpty() && !ui.buscando) {
        Aviso(Icons.Default.Search, stringResource(R.string.ami_sin_resultados)); return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(lista, key = { it.id }) { r ->
            ListItem(
                leadingContent = { Avatar(r.nickname, r.avatarPath) },
                headlineContent = { Text(r.nickname) },
                trailingContent = {
                    when (r.relation) {
                        Relacion.NONE -> FilledTonalButton(onClick = { vm.enviar(r.id) }) { Text(stringResource(R.string.ami_anadir)) }
                        Relacion.SENT -> OutlinedButton(onClick = { vm.cancelar(r.id) }) { Text(stringResource(R.string.ami_enviada)) }
                        Relacion.RECEIVED -> FilledTonalButton(onClick = { vm.responder(r.id, true) }) { Text(stringResource(R.string.ami_aceptar)) }
                        Relacion.FRIEND -> TextButton(onClick = { onAmigo(r.id) }) { Text(stringResource(R.string.ami_es_amigo)) }
                    }
                },
            )
        }
    }
}

@Composable
private fun Contactos(ui: AmigosUi, vm: FriendsViewModel, onAmigo: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        if (ui.recibidas.isNotEmpty()) {
            item { Cabecera(stringResource(R.string.ami_recibidas)) }
            items(ui.recibidas, key = { "r" + it.id }) { c ->
                Fila(c) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { vm.responder(c.id, false) }) { Text(stringResource(R.string.ami_rechazar)) }
                        FilledTonalButton(onClick = { vm.responder(c.id, true) }) { Text(stringResource(R.string.ami_aceptar)) }
                    }
                }
            }
        }
        if (ui.enviadas.isNotEmpty()) {
            item { Cabecera(stringResource(R.string.ami_enviadas)) }
            items(ui.enviadas, key = { "e" + it.id }) { c ->
                Fila(c) { TextButton(onClick = { vm.cancelar(c.id) }) { Text(stringResource(R.string.ami_cancelar)) } }
            }
        }
        item { Cabecera(stringResource(R.string.ami_amigos, ui.amigos.size)) }
        items(ui.amigos, key = { "a" + it.id }) { c -> Fila(c, onClick = { onAmigo(c.id) }) {} }
    }
}

@Composable
private fun Cabecera(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp))
}

@Composable
private fun Fila(c: Contacto, onClick: (() -> Unit)? = null, acciones: @Composable () -> Unit) {
    ListItem(
        leadingContent = { Avatar(c.nickname, c.avatarPath) },
        headlineContent = { Text(c.nickname) },
        trailingContent = { Row(verticalAlignment = Alignment.CenterVertically) { acciones() } },
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    )
}
