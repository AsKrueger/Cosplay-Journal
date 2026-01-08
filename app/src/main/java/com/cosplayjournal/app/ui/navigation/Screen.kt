package com.cosplayjournal.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Cosplans : Screen("cosplans", "Cosplans", Icons.Default.List)
    object Cosplays : Screen("cosplays", "Cosplays", Icons.Default.Star)
    object Events : Screen("events", "Events", Icons.Default.DateRange)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    // Detail screens
    object CosplanDetail : Screen("cosplan_detail/{id}", "Cosplan Detail")
    object CosplayDetail : Screen("cosplay_detail/{id}", "Cosplay Detail")
    object EventDetail : Screen("event_detail/{id}", "Event Detail")
}
