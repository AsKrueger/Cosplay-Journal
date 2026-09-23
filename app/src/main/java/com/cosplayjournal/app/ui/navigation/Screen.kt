package com.cosplayjournal.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Cosplays : Screen("cosplays", "Cosplays", Icons.Default.Checkroom)
    object Calendar : Screen("events", "Calendario", Icons.Default.CalendarMonth)
    object Gallery : Screen("gallery", "Galería", Icons.Default.PhotoLibrary)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)

    // Sub-screens
    object Favorites : Screen("favorites", "Favoritos")
    object Settings : Screen("settings", "Ajustes")
    
    // Selection screen
    object CosplaySelection : Screen("cosplay_selection", "Selección")
    
    // List screens
    object CosplanList : Screen("cosplan_list", "Mis Cosplans")
    object CosplayList : Screen("cosplay_list_full", "Mis Cosplays")

    // Detail screens
    object CosplanDetail : Screen("cosplan_detail/{cosplanId}", "Detalle Cosplan")
    object CosplayDetail : Screen("cosplay_detail/{cosplayId}", "Detalle Cosplay")
    object EventDetail : Screen("event_detail/{eventId}", "Detalle Evento")
}
