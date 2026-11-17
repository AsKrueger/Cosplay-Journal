package com.cosplayjournal.app.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.social.Bloqueado
import com.cosplayjournal.app.ui.common.Avatar
import com.cosplayjournal.app.ui.common.Aviso
import com.cosplayjournal.app.ui.common.Cargando
import com.cosplayjournal.app.ui.common.appContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedScreen(onAtras: () -> Unit) {
    val social = appContainer().social
    val scope = rememberCoroutineScope()
    var lista by remember { mutableStateOf<List<Bloqueado>?>(null) }
    LaunchedEffect(Unit) { lista = runCatching { social.bloqueados() }.getOrDefault(emptyList()) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.ajustes_bloqueados)) },
            navigationIcon = { IconButton(onClick = onAtras) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.accion_atras)) } },
        )
    }) { padding ->
        val l = lista
        when {
            l == null -> Cargando(Modifier.padding(padding))
            l.isEmpty() -> Aviso(Icons.Default.Block, stringResource(R.string.bloq_vacio), modifier = Modifier.padding(padding))
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(l, key = { it.id }) { b ->
                    ListItem(
                        leadingContent = { Avatar(b.nickname, null) },
                        headlineContent = { Text(b.nickname) },
                        trailingContent = {
                            TextButton(onClick = {
                                scope.launch {
                                    runCatching { social.desbloquear(b.id) }.onSuccess { lista = lista?.minus(b) }
                                }
                            }) { Text(stringResource(R.string.bloq_desbloquear)) }
                        },
                    )
                }
            }
        }
    }
}
