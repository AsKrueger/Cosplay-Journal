package com.cosplayjournal.app.ui.screens.event

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.ui.calendar.underlineFor
import com.cosplayjournal.app.ui.viewmodel.EventViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(viewModel: EventViewModel, onEventClick: (String) -> Unit) {
    val events by viewModel.events.collectAsState()
    val userEventData by viewModel.userEventData.collectAsState()
    val loadError by viewModel.loadError.collectAsState()

    // Recarga cada vez que se muestra el calendario, para recoger los eventos importados desde el backend
    LaunchedEffect(Unit) { viewModel.loadEvents() }

    // Locale para español
    val spanishLocale = remember { Locale("es", "ES") }

    // Elevamos el estado del mes para que la lista pueda reaccionar a él
    var currentViewMonth by remember { mutableStateOf(YearMonth.now()) }

    // Filtramos los eventos basándonos en el mes que se está visualizando en el calendario
    val filteredEvents = remember(events, currentViewMonth) {
        events.filter { event ->
            try {
                val start = LocalDate.parse(event.startDate)
                val end = LocalDate.parse(event.endDate)
                val eventStartMonth = YearMonth.from(start)
                val eventEndMonth = YearMonth.from(end)

                // El evento es relevante si su rango de fechas toca el mes seleccionado
                !(currentViewMonth.isBefore(eventStartMonth) || currentViewMonth.isAfter(eventEndMonth))
            } catch (e: Exception) {
                false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendario", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            CalendarSection(
                events = events,
                currentMonth = currentViewMonth,
                onMonthChange = { currentViewMonth = it },
                locale = spanishLocale
            )

            Spacer(modifier = Modifier.height(16.dp))

            val monthName = currentViewMonth.month.getDisplayName(TextStyle.FULL, spanishLocale)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }

            Text(
                text = "Eventos en $monthName ${currentViewMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (filteredEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    if (loadError) {
                        Text(
                            "No se pudo conectar con el servidor. Toca para reintentar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable { viewModel.loadEvents() }
                        )
                    } else {
                        Text(
                            "No hay eventos programados para este mes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredEvents) { event ->
                        val data = userEventData.find { it.eventId == event.id }
                        EventCard(
                            event = event,
                            status = data?.status ?: "NONE",
                            underlineColor = underlineFor(event.startDate, event.endDate, data?.status ?: "NONE"),
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarSection(
    events: List<Event>,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    locale: Locale
) {
    val today = remember { LocalDate.now() }
    val maxMonth = remember { YearMonth.now().plusMonths(12) }
    val minMonth = remember { YearMonth.now().minusMonths(12) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentMonth > minMonth) onMonthChange(currentMonth.minusMonths(1)) },
                enabled = currentMonth > minMonth
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior")
            }

            val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, locale)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

            Text(
                text = "$monthName ${currentMonth.year}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = { if (currentMonth < maxMonth) onMonthChange(currentMonth.plusMonths(1)) },
                enabled = currentMonth < maxMonth
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Iniciales de los días en español empezando por Domingo: D, L, M, X, J, V, S
            listOf("D", "L", "M", "X", "J", "V", "S").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }

        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7

        val days = mutableListOf<String>()
        repeat(firstDayOfMonth) { days.add("") }
        for (i in 1..daysInMonth) { days.add(i.toString()) }

        val weeks = days.chunked(7)

        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day.isNotEmpty()) {
                            val date = currentMonth.atDay(day.toInt())
                            val isToday = date == today

                            val hasEvent = events.any { event ->
                                try {
                                    val start = LocalDate.parse(event.startDate)
                                    val end = LocalDate.parse(event.endDate)
                                    !date.isBefore(start) && !date.isAfter(end)
                                } catch (e: Exception) {
                                    false
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day,
                                        color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (hasEvent && !isToday) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(4.dp)
                                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
                if (week.size < 7) {
                    repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.width(40.dp).height(4.dp).background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally))
    }
}

@Composable
fun EventCard(
    event: Event,
    status: String,
    underlineColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    // "NONE" es el estado por defecto de un evento sin selección: se muestra como planificando
    val statusText = when(status) {
        "ATTENDING" -> "VOY"
        "ATTENDED" -> "FUI"
        "INTERESTED" -> "ME INTERESA"
        "PLANNING", "NONE" -> "PLANIFICANDO"
        else -> status
    }
    val statusColor = when(status) {
        "ATTENDING", "ATTENDED" -> MaterialTheme.colorScheme.primaryContainer
        "INTERESTED" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val statusTextColor = when(status) {
        "ATTENDING", "ATTENDED" -> MaterialTheme.colorScheme.onPrimaryContainer
        "INTERESTED" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        // Subrayado de color según el estado del evento en el calendario
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).drawWithContent {
            drawContent()
            drawRect(
                color = underlineColor,
                topLeft = Offset(0f, size.height - UNDERLINE_HEIGHT.toPx()),
                size = Size(size.width, UNDERLINE_HEIGHT.toPx())
            )
        },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(text = event.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = event.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "${event.startDate} - ${event.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = {
                val text = "${event.name}\n${event.startDate} - ${event.endDate}\n${event.city}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir evento"))
            }) {
                Icon(Icons.Default.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Grosor del subrayado de color de cada tarjeta de evento. */
private val UNDERLINE_HEIGHT = 4.dp
