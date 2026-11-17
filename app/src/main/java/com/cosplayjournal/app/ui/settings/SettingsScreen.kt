package com.cosplayjournal.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosplayjournal.app.BuildConfig
import com.cosplayjournal.app.R
import com.cosplayjournal.app.ui.auth.URL_PRIVACIDAD
import com.cosplayjournal.app.ui.common.appViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onAtras: () -> Unit, onBloqueados: () -> Unit) {
    val context = LocalContext.current
    val vm = appViewModel { c ->
        SettingsViewModel(
            c.settings, c.auth, c.comentarios,
            object : AlmacenLocal {
                override suspend fun tamañoCache() = c.imagenes.tamañoCache()
                override suspend fun borrarCache() = c.imagenes.borrarCache()
                override suspend fun borrarDatos() = c.borrarDatosLocales()
            },
            BuildConfig.VERSION_NAME,
            borrarArchivos = { c.social.borrarMisArchivos() },
        )
    }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val uri = LocalUriHandler.current
    val snackbar = remember { SnackbarHostState() }
    var dialogo by rememberSaveable { mutableStateOf<String?>(null) }

    ui.mensaje?.let { res ->
        val texto = stringResource(res)
        LaunchedEffect(res) {
            snackbar.showSnackbar(texto)
            vm.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ajustes_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.accion_atras))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Seccion(R.string.ajustes_seccion_app)
            Opcion(Icons.Default.Language, stringResource(R.string.ajustes_idioma), nombreIdioma()) { dialogo = "idioma" }

            Seccion(R.string.ajustes_seccion_datos)
            ListItem(
                leadingContent = { Icon(Icons.Default.OfflinePin, null) },
                headlineContent = { Text(stringResource(R.string.ajustes_offline)) },
                supportingContent = { Text(stringResource(R.string.ajustes_offline_texto)) },
                trailingContent = { Switch(ui.imagenesSinConexion, onCheckedChange = vm::imagenesSinConexion) },
                modifier = Modifier.clickable { vm.imagenesSinConexion(!ui.imagenesSinConexion) },
            )
            Opcion(
                Icons.Default.CleaningServices, stringResource(R.string.ajustes_borrar_cache),
                ui.bytesCache?.let { stringResource(R.string.ajustes_cache_ocupa, Formatter.formatShortFileSize(context, it)) },
                onClick = vm::borrarCache,
            )
            Opcion(Icons.Default.DeleteSweep, stringResource(R.string.ajustes_borrar_datos), null) { dialogo = "datos" }

            Seccion(R.string.ajustes_seccion_ayuda)
            Opcion(Icons.Default.Feedback, stringResource(R.string.ajustes_comentarios),
                stringResource(R.string.ajustes_comentarios_texto)) { dialogo = "comentario" }
            Opcion(Icons.Default.Policy, stringResource(R.string.ajustes_privacidad), null) { uri.openUri(URL_PRIVACIDAD) }
            Opcion(Icons.Default.Info, stringResource(R.string.ajustes_version, BuildConfig.VERSION_NAME), null, onClick = null)

            Seccion(R.string.ajustes_seccion_cuenta)
            Opcion(Icons.Default.Block, stringResource(R.string.ajustes_bloqueados), null, onClick = onBloqueados)
            Opcion(Icons.AutoMirrored.Filled.Logout, stringResource(R.string.ajustes_cerrar_sesion), null, onClick = vm::cerrarSesion)
            Opcion(Icons.Default.DeleteForever, stringResource(R.string.ajustes_borrar_cuenta), null,
                peligro = true) { dialogo = "cuenta" }
        }
    }

    when (dialogo) {
        "idioma" -> DialogoIdioma { dialogo = null }
        "datos" -> Confirmar(R.string.ajustes_borrar_datos, R.string.ajustes_borrar_datos_texto, R.string.accion_aceptar,
            onCerrar = { dialogo = null }) { vm.borrarDatos() }
        "cuenta" -> Confirmar(R.string.ajustes_borrar_cuenta, R.string.ajustes_borrar_cuenta_texto,
            R.string.ajustes_borrar_cuenta_confirmar, onCerrar = { dialogo = null }) { vm.borrarCuenta() }
        "comentario" -> DialogoComentario(
            onCerrar = { dialogo = null },
            onEnviar = { vm.enviarComentario(it, Locale.getDefault().toLanguageTag()); dialogo = null },
            onEmail = {
                dialogo = null
                val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${BuildConfig.FEEDBACK_EMAIL}"))
                    .putExtra(Intent.EXTRA_SUBJECT, "Cosplay Journal ${BuildConfig.VERSION_NAME}")
                runCatching { context.startActivity(i) }
            },
        )
    }
}

@Composable
private fun Seccion(res: Int) {
    Text(
        stringResource(res), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 4.dp),
    )
}

@Composable
private fun Opcion(icono: ImageVector, titulo: String, sub: String?, peligro: Boolean = false, onClick: (() -> Unit)?) {
    val color = if (peligro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    ListItem(
        leadingContent = { Icon(icono, null, tint = color) },
        headlineContent = { Text(titulo, color = color) },
        supportingContent = sub?.let { { Text(it) } },
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    )
}

/** Idioma de la app: el del sistema, español o inglés. */
private val IDIOMAS = listOf("" to R.string.ajustes_idioma_sistema, "es" to R.string.ajustes_idioma_es, "en" to R.string.ajustes_idioma_en)

@Composable
private fun nombreIdioma(): String {
    val actual = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    return stringResource(IDIOMAS.firstOrNull { it.first.isNotEmpty() && actual.startsWith(it.first) }?.second
        ?: R.string.ajustes_idioma_sistema)
}

@Composable
private fun DialogoIdioma(onCerrar: () -> Unit) {
    val actual = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(R.string.ajustes_idioma)) },
        text = {
            Column {
                IDIOMAS.forEach { (tag, res) ->
                    val elegir = {
                        AppCompatDelegate.setApplicationLocales(
                            if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)
                        )
                        onCerrar()
                    }
                    ListItem(
                        leadingContent = {
                            RadioButton(selected = if (tag.isEmpty()) actual.isEmpty() else actual.startsWith(tag), onClick = elegir)
                        },
                        headlineContent = { Text(stringResource(res)) },
                        modifier = Modifier.clickable(onClick = elegir),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.accion_cerrar)) } },
    )
}

@Composable
private fun Confirmar(titulo: Int, texto: Int, boton: Int, onCerrar: () -> Unit, onOk: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(titulo)) },
        text = { Text(stringResource(texto)) },
        confirmButton = {
            TextButton(onClick = { onOk(); onCerrar() }) {
                Text(stringResource(boton), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.accion_cancelar)) } },
    )
}

@Composable
private fun DialogoComentario(onCerrar: () -> Unit, onEnviar: (String) -> Unit, onEmail: () -> Unit) {
    var texto by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(R.string.ajustes_comentarios)) },
        text = {
            Column {
                OutlinedTextField(
                    value = texto, onValueChange = { texto = it.take(4000) },
                    placeholder = { Text(stringResource(R.string.ajustes_comentarios_hint)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                )
                if (BuildConfig.FEEDBACK_EMAIL.isNotBlank()) {
                    TextButton(onClick = onEmail) { Text(stringResource(R.string.ajustes_comentarios_email)) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onEnviar(texto) }, enabled = texto.isNotBlank()) {
                Text(stringResource(R.string.accion_enviar))
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.accion_cancelar)) } },
    )
}
