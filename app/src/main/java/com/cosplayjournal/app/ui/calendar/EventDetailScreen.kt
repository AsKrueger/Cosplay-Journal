package com.cosplayjournal.app.ui.calendar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.events.EventoEntity
import com.cosplayjournal.app.domain.Provincias
import com.cosplayjournal.app.domain.opcionesAsistencia
import com.cosplayjournal.app.ui.common.Aviso
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun EventDetailScreen(vm: EventDetailViewModel, onAtras: () -> Unit) {
    val e by vm.evento.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hoy = LocalDate.now()
    // En Android 13+ los avisos necesitan permiso; se pide la primera vez que marcas "Voy"
    val permisoAvisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    val actual = e ?: run {
        Box(Modifier.fillMaxSize()) {
            Aviso(Icons.Default.EventBusy, stringResource(R.string.det_no_encontrado))
            Atras(onAtras, Modifier.statusBarsPadding())
        }
        return
    }
    val ev = actual.evento
    val tono = actual.tono(hoy)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box {
            CartelGrande(ev.cartel)
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(0.6f to Color.Transparent, 1f to MaterialTheme.colorScheme.background)
                )
            )
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Atras(onAtras)
                IconButton(
                    onClick = { vm.favorito() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                ) {
                    Icon(
                        if (actual.estado.favorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        stringResource(R.string.cal_favorito),
                        tint = if (actual.estado.favorito) MaterialTheme.colorScheme.secondary else Color.White,
                    )
                }
            }
        }

        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                rangoFechas(ev.inicio, ev.fin).uppercase(), color = tono.color(),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(ev.nombre, style = MaterialTheme.typography.headlineMedium)

            Text(
                stringResource(if (ev.fin < hoy) R.string.det_fuiste else R.string.det_tu_plan),
                style = MaterialTheme.typography.titleMedium,
            )
            val opciones = opcionesAsistencia(ev.fin, hoy)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                opciones.forEachIndexed { i, a ->
                    SegmentedButton(
                        selected = actual.estado.asistencia == a,
                        onClick = {
                            if (a == Asistencia.GOING && actual.estado.asistencia != a && Build.VERSION.SDK_INT >= 33) {
                                permisoAvisos.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            vm.asistencia(a)
                        },
                        shape = SegmentedButtonDefaults.itemShape(i, opciones.size),
                    ) { Text(textoAsistencia(a)) }
                }
            }
            if (actual.estado.asistencia == Asistencia.GOING && ev.inicio > hoy) {
                Fila(Icons.Default.NotificationsActive, stringResource(R.string.det_aviso))
            }

            val lugar = listOfNotNull(ev.lugar, ev.direccion, ev.ciudad, ev.provincia?.let { Provincias.nombre(it) })
                .distinct().joinToString("\n")
            if (lugar.isNotBlank()) Fila(Icons.Default.Place, lugar)
            ev.precio?.let { Fila(Icons.Default.Euro, it) }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ev.web?.let { web ->
                    Accion(Icons.Default.Language, R.string.det_web, Modifier.weight(1f)) { abrir(context, Uri.parse(web)) }
                }
                if (lugar.isNotBlank()) {
                    Accion(Icons.Default.Directions, R.string.det_como_llegar, Modifier.weight(1f)) {
                        abrir(context, Uri.parse("geo:0,0?q=" + Uri.encode(lugar.replace("\n", ", "))))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Accion(Icons.Outlined.CalendarToday, R.string.det_calendario, Modifier.weight(1f)) { alCalendario(context, ev) }
                Accion(Icons.Default.Share, R.string.det_compartir, Modifier.weight(1f)) { compartir(context, ev) }
            }
            Box(Modifier.padding(bottom = 32.dp))
        }
    }
}

@Composable
private fun Atras(onAtras: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onAtras, modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f)),
    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.accion_atras), tint = Color.White) }
}

@Composable
private fun Fila(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icono, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp, top = 6.dp))
    }
}

@Composable
private fun Accion(icono: ImageVector, texto: Int, modifier: Modifier, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = modifier) {
        Icon(icono, null, Modifier.size(18.dp))
        Text(stringResource(texto), Modifier.padding(start = 6.dp))
    }
}

private fun abrir(context: Context, uri: Uri) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
}

private fun alCalendario(context: Context, ev: EventoEntity) {
    val zona = ZoneId.systemDefault()
    val i = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, ev.nombre)
        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, ev.inicio.atStartOfDay(zona).toInstant().toEpochMilli())
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, ev.fin.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli())
        .putExtra(CalendarContract.Events.EVENT_LOCATION, listOfNotNull(ev.lugar, ev.ciudad).joinToString(", "))
        .putExtra(CalendarContract.Events.DESCRIPTION, ev.web ?: "")
    runCatching { context.startActivity(i) }
}

private fun compartir(context: Context, ev: EventoEntity) {
    val texto = context.getString(R.string.det_compartir_texto, ev.nombre, rangoFechas(ev.inicio, ev.fin), ev.ciudad ?: "") +
        (ev.web?.let { "\n$it" } ?: "")
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, texto), null))
}

