package com.cosplayjournal.app.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.profile.Perfil
import com.cosplayjournal.app.ui.common.appContainer
import com.cosplayjournal.app.ui.theme.DegradadoMarca

/** Cabecera del perfil propio: avatar, nickname y código de amigo. */
@Composable
fun CabeceraPerfil(perfil: Perfil?, onAjustes: () -> Unit) {
    val context = LocalContext.current
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(DegradadoMarca).statusBarsPadding().padding(20.dp)
    ) {
        IconButton(onClick = onAjustes, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Settings, stringResource(R.string.perfil_ajustes), tint = Color.White)
        }
        Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    perfil?.nickname?.take(1)?.uppercase() ?: "",
                    style = MaterialTheme.typography.headlineLarge, color = Color.White,
                )
            }
            Text(
                perfil?.nickname ?: "…", style = MaterialTheme.typography.headlineSmall, color = Color.White,
                modifier = Modifier.padding(top = 12.dp),
            )
            perfil?.codigoAmigo?.let { codigo ->
                Card(Modifier.padding(top = 12.dp)) {
                    Row(
                        Modifier.padding(start = 16.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
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

@Composable
fun ProfileScreen(onAjustes: () -> Unit) {
    val c = appContainer()
    val perfil by produceState<Perfil?>(null) {
        value = c.auth.userIdActual()?.let { runCatching { c.perfiles.miPerfil(it) }.getOrNull() }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CabeceraPerfil(perfil, onAjustes)
    }
}

private fun copiar(context: Context, codigo: String) {
    val cm = context.getSystemService(ClipboardManager::class.java)
    cm.setPrimaryClip(ClipData.newPlainText("codigo", codigo))
    Toast.makeText(context, R.string.perfil_codigo_copiado, Toast.LENGTH_SHORT).show()
}

private fun compartir(context: Context, codigo: String) {
    val i = Intent(Intent.ACTION_SEND).setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, context.getString(R.string.perfil_compartir_codigo, codigo))
    context.startActivity(Intent.createChooser(i, null))
}
