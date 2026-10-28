package com.cosplayjournal.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cosplayjournal.app.CosplayJournalApplication
import com.cosplayjournal.app.ui.components.EmptyState
import com.cosplayjournal.app.ui.navigation.Screen
import com.cosplayjournal.app.ui.screens.event.EventDetailScreen
import com.cosplayjournal.app.ui.screens.event.EventListScreen
import com.cosplayjournal.app.ui.screens.profile.ProfileScreen
import com.cosplayjournal.app.ui.theme.CosplayJournalTheme
import com.cosplayjournal.app.ui.viewmodel.EventViewModel
import com.cosplayjournal.app.ui.viewmodel.EventViewModelFactory
import com.cosplayjournal.app.ui.viewmodel.ProfileViewModel
import com.cosplayjournal.app.ui.viewmodel.ProfileViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CosplayJournalTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as CosplayJournalApplication

    val eventViewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(app.eventRepository, app.userEventRepository)
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(app.authRepository)
    )

    val tabs = listOf(Screen.Calendar, Screen.Gallery, Screen.Profile)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = tabs.any { it.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    tabs.forEach { screen ->
                        NavigationBarItem(
                            icon = { screen.icon?.let { Icon(it, contentDescription = null) } },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Calendar.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Calendar.route) {
                EventListScreen(viewModel = eventViewModel, onEventClick = { id ->
                    navController.navigate("event_detail/$id")
                })
            }

            composable(Screen.Gallery.route) {
                EmptyState(
                    icon = Icons.Default.PhotoLibrary,
                    title = "Tu galería",
                    description = "Aquí verás tus fotos, clasificadas por evento y por grupal."
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = profileViewModel)
            }

            composable(Screen.EventDetail.route, arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                EventDetailScreen(
                    eventViewModel = eventViewModel,
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
