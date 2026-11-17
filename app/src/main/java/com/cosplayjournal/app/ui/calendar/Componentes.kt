package com.cosplayjournal.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.events.EventoConEstado
import com.cosplayjournal.app.domain.Tono
import com.cosplayjournal.app.domain.tono
import com.cosplayjournal.app.ui.theme.EstadoColores
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun Tono.color(): Color = when (this) {
    Tono.PASADO_NO_FUI -> EstadoColores.PasadoNoFui
    Tono.PASADO_FUI -> EstadoColores.PasadoFui
    Tono.EN_CURSO -> EstadoColores.EnCurso
    Tono.FUTURO_VOY -> EstadoColores.FuturoVoy
    Tono.FUTURO_SIN_MARCAR -> EstadoColores.FuturoSinMarcar
}

fun EventoConEstado.tono(hoy: LocalDate) = tono(evento.inicio, evento.fin, estado.asistencia, hoy)

/** "5–7 nov 2026" o "7 nov 2026". */
fun rangoFechas(inicio: LocalDate, fin: LocalDate, locale: Locale = Locale.getDefault()): String {
    val completo = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    return when {
        inicio == fin -> inicio.format(completo)
        inicio.month == fin.month && inicio.year == fin.year -> "${inicio.dayOfMonth}–${fin.format(completo)}"
        inicio.year == fin.year -> "${inicio.format(DateTimeFormatter.ofPattern("d MMM", locale))} – ${fin.format(completo)}"
        else -> "${inicio.format(completo)} – ${fin.format(completo)}"
    }.replace(".", "")
}

fun fechaLarga(d: LocalDate): String = d.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

@Composable
fun textoAsistencia(a: Asistencia): String = stringResource(
    when (a) {
        Asistencia.GOING -> R.string.det_voy
        Asistencia.NOT_GOING -> R.string.det_no_voy
        Asistencia.WENT -> R.string.det_fui
        Asistencia.MISSED -> R.string.det_no_fui
    }
)

@Composable
fun Cartel(url: String?, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = url, contentDescription = null, contentScale = ContentScale.Crop,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        error = {
            Box(Modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.outline)
            }
        },
    )
}

@Composable
fun TarjetaEvento(e: EventoConEstado, hoy: LocalDate, onClick: () -> Unit, onFavorito: () -> Unit) {
    val ev = e.evento
    val color = e.tono(hoy).color()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(Modifier.height(136.dp)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(color))
            Cartel(ev.cartel, Modifier.width(100.dp).fillMaxHeight())
            Column(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    rangoFechas(ev.inicio, ev.fin).uppercase(), style = MaterialTheme.typography.labelMedium,
                    color = color,
                )
                Text(ev.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                ev.ciudad?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Place, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.weight(1f))
                e.estado.asistencia?.let { a ->
                    Text(
                        textoAsistencia(a), style = MaterialTheme.typography.labelMedium, color = Color.White,
                        modifier = Modifier.clip(CircleShape).background(color).padding(horizontal = 10.dp, vertical = 2.dp),
                    )
                }
            }
            IconButton(onClick = onFavorito) {
                Icon(
                    if (e.estado.favorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    stringResource(R.string.cal_favorito),
                    tint = if (e.estado.favorito) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

/** Fila pequeña con un punto de color y su significado. */
@Composable
fun Leyenda(modifier: Modifier = Modifier) {
    val items = listOf(
        Tono.FUTURO_SIN_MARCAR to R.string.cal_leyenda_sin_marcar,
        Tono.FUTURO_VOY to R.string.cal_leyenda_voy,
        Tono.EN_CURSO to R.string.cal_leyenda_en_curso,
        Tono.PASADO_FUI to R.string.cal_leyenda_fui,
        Tono.PASADO_NO_FUI to R.string.cal_leyenda_no_fui,
    )
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        items.forEach { (t, res) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(t.color()))
                Text(stringResource(res), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
fun CartelGrande(url: String?, modifier: Modifier = Modifier) =
    Cartel(url, modifier.fillMaxWidth().aspectRatio(3f / 4f))
