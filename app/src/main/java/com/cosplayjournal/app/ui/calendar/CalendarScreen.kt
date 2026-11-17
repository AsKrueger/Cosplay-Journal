package com.cosplayjournal.app.ui.calendar

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.events.EventoConEstado
import com.cosplayjournal.app.domain.Filtro
import com.cosplayjournal.app.domain.Provincias
import com.cosplayjournal.app.domain.eventosDelDia
import com.cosplayjournal.app.ui.common.Aviso
import com.cosplayjournal.app.ui.common.Cargando
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(vm: CalendarViewModel, onEvento: (Long) -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var buscando by rememberSaveable { mutableStateOf(false) }
    var elegirProvincia by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    ui.error?.let { res ->
        val texto = stringResource(res)
        LaunchedEffect(res) {
            if (ui.hayEventos) snackbar.showSnackbar(texto)
            vm.errorVisto()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (buscando) {
                        TextField(
                            value = ui.consulta.texto, onValueChange = vm::buscar, singleLine = true,
                            placeholder = { Text(stringResource(R.string.cal_buscar)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else Text(stringResource(R.string.nav_calendario))
                },
                actions = {
                    IconButton(onClick = { if (buscando) vm.buscar(""); buscando = !buscando }) {
                        Icon(if (buscando) Icons.Default.Close else Icons.Default.Search, stringResource(R.string.cal_buscar))
                    }
                    if (ui.vista == Vista.LISTA) {
                        IconButton(onClick = { vm.vista(Vista.MES) }) {
                            Icon(Icons.Default.CalendarMonth, stringResource(R.string.cal_vista_mes))
                        }
                    } else {
                        IconButton(onClick = { vm.vista(Vista.LISTA) }) {
                            Icon(Icons.AutoMirrored.Filled.ViewList, stringResource(R.string.cal_vista_lista))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Filtros(ui, vm, onCercanos = {
                if (ui.consulta.miProvincia == null) elegirProvincia = true else vm.filtro(Filtro.CERCANOS)
            })
            if (ui.consulta.filtro == Filtro.CERCANOS && ui.consulta.miProvincia != null) {
                TextButton(onClick = { elegirProvincia = true }, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(stringResource(R.string.cal_cambiar_provincia, Provincias.nombre(ui.consulta.miProvincia!!) ?: ""))
                }
            }
            PullToRefreshBox(isRefreshing = ui.actualizando, onRefresh = vm::actualizar, modifier = Modifier.weight(1f)) {
                when {
                    ui.cargandoPrimeraVez -> Cargando()
                    !ui.hayEventos -> Aviso(
                        Icons.Default.WifiOff, stringResource(R.string.cal_sin_datos_titulo),
                        stringResource(R.string.cal_sin_datos_texto), accion = R.string.accion_reintentar, onAccion = vm::actualizar,
                    )
                    ui.vista == Vista.LISTA -> Lista(ui, vm, onEvento)
                    else -> VistaMes(ui, vm, onEvento)
                }
            }
        }
    }

    if (elegirProvincia) {
        DialogoProvincia(
            actual = ui.consulta.miProvincia,
            onElegir = { vm.miProvincia(it); elegirProvincia = false },
            onCerrar = { elegirProvincia = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Filtros(ui: CalendarioUi, vm: CalendarViewModel, onCercanos: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (ui.vista == Vista.LISTA) {
            SingleChoiceSegmentedButtonRow(Modifier.width(220.dp)) {
                listOf(false to R.string.cal_proximos, true to R.string.cal_pasados).forEachIndexed { i, (p, res) ->
                    SegmentedButton(
                        selected = ui.consulta.pasados == p, onClick = { vm.pasados(p) },
                        shape = SegmentedButtonDefaults.itemShape(i, 2),
                    ) { Text(stringResource(res)) }
                }
            }
        }
        listOf(
            Filtro.FAVORITOS to R.string.cal_filtro_favoritos,
            Filtro.CERCANOS to R.string.cal_filtro_cercanos,
            Filtro.VOY to R.string.cal_filtro_voy,
            Filtro.FUI to R.string.cal_filtro_fui,
        ).forEach { (f, res) ->
            FilterChip(
                selected = ui.consulta.filtro == f,
                onClick = { if (f == Filtro.CERCANOS && ui.consulta.filtro != f) onCercanos() else vm.filtro(f) },
                label = { Text(stringResource(res)) },
            )
        }
    }
}

@Composable
private fun Lista(ui: CalendarioUi, vm: CalendarViewModel, onEvento: (Long) -> Unit) {
    if (ui.lista.isEmpty()) {
        Aviso(Icons.Default.SearchOff, stringResource(R.string.cal_vacio_titulo), stringResource(R.string.cal_vacio_texto))
        return
    }
    val locale = Locale.getDefault()
    val porMes = ui.lista.groupBy { YearMonth.from(it.evento.inicio) }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
        porMes.forEach { (mes, eventos) ->
            item(key = "m$mes") {
                Text(
                    "${mes.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }} ${mes.year}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            items(eventos, key = { it.evento.id }) { e ->
                TarjetaEvento(e, ui.hoy, onClick = { onEvento(e.evento.id) }, onFavorito = { vm.favorito(e.evento.id) })
            }
        }
    }
}

@Composable
private fun VistaMes(ui: CalendarioUi, vm: CalendarViewModel, onEvento: (Long) -> Unit) {
    val locale = Locale.getDefault()
    val delDia = eventosDelDia(ui.delMes, ui.diaSeleccionado)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.mes(-1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }
                Text(
                    "${ui.mes.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }} ${ui.mes.year}",
                    style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { vm.mes(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
                TextButton(onClick = vm::irAHoy) { Text(stringResource(R.string.cal_hoy)) }
            }
        }
        item { Rejilla(ui) { vm.dia(it) } }
        item { Leyenda(Modifier.padding(vertical = 12.dp)) }
        item {
            Text(
                fechaLarga(ui.diaSeleccionado).replaceFirstChar { it.titlecase(locale) },
                style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 20.dp, top = 8.dp),
            )
        }
        if (delDia.isEmpty()) {
            item {
                Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.EventBusy, null, tint = MaterialTheme.colorScheme.outline)
                    Text(stringResource(R.string.cal_dia_vacio), color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        items(delDia, key = { it.evento.id }) { e ->
            TarjetaEvento(e, ui.hoy, onClick = { onEvento(e.evento.id) }, onFavorito = { vm.favorito(e.evento.id) })
        }
    }
}

/** Cuadrícula del mes; bajo cada día, una barra por evento con el color de su estado. */
@Composable
private fun Rejilla(ui: CalendarioUi, onDia: (LocalDate) -> Unit) {
    val locale = Locale.getDefault()
    val primero = ui.mes.atDay(1)
    val huecos = (primero.dayOfWeek.value - DayOfWeek.MONDAY.value)
    val dias = (1..ui.mes.lengthOfMonth()).map { ui.mes.atDay(it) }
    val celdas: List<LocalDate?> = List(huecos) { null } + dias
    Column(Modifier.padding(horizontal = 12.dp)) {
        Row {
            DayOfWeek.entries.forEach {
                Text(
                    it.getDisplayName(TextStyle.NARROW_STANDALONE, locale), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        celdas.chunked(7).forEach { semana ->
            Row {
                semana.forEach { dia -> Celda(dia, ui, onDia, Modifier.weight(1f)) }
                repeat(7 - semana.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Celda(dia: LocalDate?, ui: CalendarioUi, onDia: (LocalDate) -> Unit, modifier: Modifier) {
    if (dia == null) {
        Spacer(modifier.aspectRatio(0.8f)); return
    }
    val eventos: List<EventoConEstado> = eventosDelDia(ui.delMes, dia)
    val seleccionado = dia == ui.diaSeleccionado
    val esHoy = dia == ui.hoy
    Column(
        modifier.aspectRatio(0.8f).padding(2.dp).clip(RoundedCornerShape(12.dp))
            .background(if (seleccionado) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .then(if (esHoy) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)
            .clickable { onDia(dia) }
            .padding(top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "${dia.dayOfMonth}", style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (esHoy || eventos.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
        )
        Spacer(Modifier.height(4.dp))
        eventos.take(3).forEach { e ->
            Box(Modifier.padding(vertical = 1.dp).fillMaxWidth(0.7f).height(4.dp).clip(CircleShape).background(e.tono(ui.hoy).color()))
        }
    }
}

@Composable
private fun DialogoProvincia(actual: String?, onElegir: (String?) -> Unit, onCerrar: () -> Unit) {
    val context = LocalContext.current
    var sinUbicacion by remember { mutableStateOf(false) }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        val codigo = if (ok) provinciaPorUbicacion(context) else null
        if (codigo != null) onElegir(codigo) else sinUbicacion = true
    }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(R.string.cal_provincia_titulo)) },
        text = {
            Column {
                Text(stringResource(R.string.cal_provincia_texto))
                OutlinedButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            provinciaPorUbicacion(context)?.let(onElegir) ?: run { sinUbicacion = true }
                        } else permiso.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                ) {
                    Icon(Icons.Default.MyLocation, null)
                    Text(stringResource(R.string.cal_usar_ubicacion), Modifier.padding(start = 8.dp))
                }
                if (sinUbicacion) {
                    Text(stringResource(R.string.cal_ubicacion_no_disponible), color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(Provincias.TODAS.entries.sortedBy { it.value.first }, key = { it.key }) { (codigo, p) ->
                        ListItem(
                            headlineContent = {
                                Text(p.first, fontWeight = if (codigo == actual) FontWeight.Bold else FontWeight.Normal)
                            },
                            modifier = Modifier.clickable { onElegir(codigo) },
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.accion_cancelar)) } },
    )
}

/** Ubicación aproximada sin Google Play Services: última posición conocida de red o GPS. */
@SuppressLint("MissingPermission")
private fun provinciaPorUbicacion(context: Context): String? {
    val lm = context.getSystemService(LocationManager::class.java) ?: return null
    val pos = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER, LocationManager.GPS_PROVIDER)
        .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time } ?: return null
    // Fuera de España (aprox.) no tiene sentido elegir la provincia más cercana
    if (pos.latitude !in 27.0..44.5 || pos.longitude !in -19.0..5.0) return null
    return Provincias.masCercana(pos.latitude, pos.longitude)
}
