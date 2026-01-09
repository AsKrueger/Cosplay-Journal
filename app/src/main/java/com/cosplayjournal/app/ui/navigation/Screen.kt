package com.cosplayjournal.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Log", Icons.Default.ListAlt)
    object Events : Screen("events", "Events", Icons.Default.CalendarMonth)
    object Cosplans : Screen("cosplans", "Stash", Icons.Default.PhotoLibrary)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    // Sub-screens
    object Favorites : Screen("favorites", "Favorites")
    object Settings : Screen("settings", "Settings")
    
    // Detail screens
    object CosplanDetail : Screen("cosplan_detail/{cosplanId}", "Cosplan Detail")
    object CosplayDetail : Screen("cosplay_detail/{cosplayId}", "Cosplay Detail")
    object EventDetail : Screen("event_detail/{eventId}", "Event Detail")
}
