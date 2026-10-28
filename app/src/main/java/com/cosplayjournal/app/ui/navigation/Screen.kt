package com.cosplayjournal.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

/** Destinos de la app: tres pestañas principales y el detalle de evento. */
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Calendar : Screen("events", "Calendario", Icons.Default.CalendarMonth)
    object Gallery : Screen("gallery", "Galería", Icons.Default.PhotoLibrary)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
    object EventDetail : Screen("event_detail/{eventId}", "Detalle del evento")
}
