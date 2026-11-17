package com.cosplayjournal.app.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cosplayjournal.app.AppContainer
import com.cosplayjournal.app.CosplayJournalApp
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.ErrorApp

@Composable
fun appContainer(): AppContainer = (LocalContext.current.applicationContext as CosplayJournalApp).container

/** Crea un ViewModel con acceso al contenedor de dependencias. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(key: String? = null, crossinline crear: (AppContainer) -> VM): VM {
    val c = appContainer()
    return viewModel(key = key, factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = crear(c) as T
    })
}

@StringRes
fun ErrorApp.texto(): Int = when (this) {
    ErrorApp.SinConexion -> R.string.error_sin_conexion
    is ErrorApp.Servidor -> if (codigo == "rate_limited") R.string.error_demasiado_rapido else R.string.error_generico
    else -> R.string.error_generico
}

@Composable
fun Cargando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

/** Estado vacío o de error con un icono, un texto y una acción opcional. */
@Composable
fun Aviso(
    icono: ImageVector,
    titulo: String,
    texto: String? = null,
    modifier: Modifier = Modifier,
    @StringRes accion: Int? = null,
    onAccion: () -> Unit = {},
) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icono, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
        Text(titulo, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        texto?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        accion?.let { TextButton(onClick = onAccion) { Text(stringResource(it)) } }
    }
}
