package com.cosplayjournal.app.ui.updates

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.updates.ActualizacionesRepository
import kotlinx.coroutines.launch

class ActualizacionViewModel(private val repo: ActualizacionesRepository) : ViewModel() {
    val aviso = repo.aviso
    val disponible = repo.disponible

    init {
        viewModelScope.launch { repo.comprobar() }
    }

    fun descartar() = viewModelScope.launch { repo.descartarAviso() }
}

/** Diálogo «Hay una versión nueva». Solo aparece una vez por versión. */
@Composable
fun AvisoActualizacion(vm: ActualizacionViewModel) {
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    val uri = LocalUriHandler.current
    val v = aviso ?: return
    AlertDialog(
        onDismissRequest = { vm.descartar() },
        icon = { Icon(Icons.Default.SystemUpdate, null) },
        title = { Text(stringResource(R.string.act_titulo, v.version)) },
        text = { Text(stringResource(R.string.act_texto)) },
        confirmButton = {
            TextButton(onClick = { uri.openUri(v.url); vm.descartar() }) { Text(stringResource(R.string.act_descargar)) }
        },
        dismissButton = {
            TextButton(onClick = { vm.descartar() }) { Text(stringResource(R.string.act_ahora_no)) }
        },
    )
}
