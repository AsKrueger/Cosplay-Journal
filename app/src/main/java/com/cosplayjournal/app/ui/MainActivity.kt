package com.cosplayjournal.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.cosplayjournal.app.ui.navigation.Screen
import com.cosplayjournal.app.ui.screens.cosplan.AddEditCosplanScreen
import com.cosplayjournal.app.ui.screens.cosplan.CosplanDetailScreen
import com.cosplayjournal.app.ui.screens.cosplan.CosplanListScreen
import com.cosplayjournal.app.ui.screens.cosplay.AddEditCosplayScreen
import com.cosplayjournal.app.ui.screens.cosplay.AddEditHandmadePartScreen
import com.cosplayjournal.app.ui.screens.cosplay.AddEditPurchasedItemScreen
import com.cosplayjournal.app.ui.screens.cosplay.CosplayDetailScreen
import com.cosplayjournal.app.ui.screens.cosplay.CosplayListScreen
import com.cosplayjournal.app.ui.screens.event.EventDetailScreen
import com.cosplayjournal.app.ui.screens.event.EventListScreen
import com.cosplayjournal.app.ui.theme.CosplayJournalTheme
import com.cosplayjournal.app.ui.viewmodel.CosplanViewModel
import com.cosplayjournal.app.ui.viewmodel.CosplanViewModelFactory
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModelFactory
import com.cosplayjournal.app.ui.viewmodel.EventViewModel
import com.cosplayjournal.app.ui.viewmodel.EventViewModelFactory

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
    val context = LocalContext.current
    val app = context.applicationContext as CosplayJournalApplication
    val repository = app.repository
    val eventRepository = app.eventRepository
    
    val cosplanViewModel: CosplanViewModel = viewModel(factory = CosplanViewModelFactory(repository))
    val cosplayViewModel: CosplayViewModel = viewModel(factory = CosplayViewModelFactory(repository))
    val eventViewModel: EventViewModel = viewModel(factory = EventViewModelFactory(eventRepository, repository))

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Events,
        Screen.Cosplans,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { screen.icon?.let { Icon(it, contentDescription = null) } },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00ACC1),
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == Screen.Events.route || currentRoute == Screen.Cosplans.route) {
                FloatingActionButton(
                    onClick = { 
                        if (currentRoute == Screen.Events.route) { /* Add Event */ }
                        else { navController.navigate("add_edit_cosplan") }
                    },
                    containerColor = Color(0xFF00ACC1),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Events.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { Text("Home Screen") }
            
            // EVENTS
            composable(Screen.Events.route) {
                EventListScreen(viewModel = eventViewModel, onEventClick = { id ->
                    navController.navigate("event_detail/$id")
                })
            }

            composable(
                route = "event_detail/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                EventDetailScreen(
                    eventViewModel = eventViewModel,
                    cosplanViewModel = cosplanViewModel,
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // COSPLANS
            composable(Screen.Cosplans.route) {
                CosplanListScreen(
                    viewModel = cosplanViewModel,
                    onCosplanClick = { id -> navController.navigate("cosplan_detail/$id") },
                    onAddCosplanClick = { navController.navigate("add_edit_cosplan") }
                )
            }

            composable(
                route = "cosplan_detail/{cosplanId}",
                arguments = listOf(navArgument("cosplanId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cosplanId = backStackEntry.arguments?.getLong("cosplanId") ?: return@composable
                CosplanDetailScreen(
                    viewModel = cosplanViewModel,
                    cosplanId = cosplanId,
                    onEditClick = { id -> navController.navigate("add_edit_cosplan?cosplanId=$id") },
                    onDeleteClick = {
                        val cosplan = cosplanViewModel.allCosplans.value.find { it.id == cosplanId }
                        cosplan?.let { 
                            cosplanViewModel.delete(it)
                            navController.popBackStack()
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onManageCosplaysClick = { id -> navController.navigate("cosplay_list/$id") }
                )
            }

            composable(
                route = "add_edit_cosplan?cosplanId={cosplanId}",
                arguments = listOf(navArgument("cosplanId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val cosplanId = backStackEntry.arguments?.getLong("cosplanId").takeIf { it != -1L }
                AddEditCosplanScreen(
                    viewModel = cosplanViewModel,
                    cosplanId = cosplanId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // COSPLAYS (Individual Character management)
            composable("cosplay_list/{cosplanId}", 
                arguments = listOf(navArgument("cosplanId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cosplanId = backStackEntry.arguments?.getLong("cosplanId")
                CosplayListScreen(
                    viewModel = cosplayViewModel,
                    cosplanId = cosplanId,
                    onCosplayClick = { id -> navController.navigate("cosplay_detail/$id") },
                    onAddCosplayClick = { id -> navController.navigate("add_edit_cosplay/$id") }
                )
            }

            composable(
                route = "cosplay_detail/{cosplayId}",
                arguments = listOf(navArgument("cosplayId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cosplayId = backStackEntry.arguments?.getLong("cosplayId") ?: return@composable
                CosplayDetailScreen(
                    viewModel = cosplayViewModel,
                    cosplayId = cosplayId,
                    onEditClick = { id -> /* TODO */ },
                    onAddHandmadePart = { id -> navController.navigate("add_handmade_part/$id") },
                    onAddPurchasedItem = { id -> navController.navigate("add_purchased_item/$id") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "add_edit_cosplay/{cosplanId}?cosplayId={cosplayId}",
                arguments = listOf(
                    navArgument("cosplanId") { type = NavType.LongType },
                    navArgument("cosplayId") { 
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val cosplanId = backStackEntry.arguments?.getLong("cosplanId") ?: return@composable
                val cosplayId = backStackEntry.arguments?.getLong("cosplayId").takeIf { it != -1L }
                AddEditCosplayScreen(
                    viewModel = cosplayViewModel,
                    cosplanId = cosplanId,
                    cosplayId = cosplayId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "add_handmade_part/{cosplayId}",
                arguments = listOf(navArgument("cosplayId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cosplayId = backStackEntry.arguments?.getLong("cosplayId") ?: return@composable
                AddEditHandmadePartScreen(
                    viewModel = cosplayViewModel,
                    cosplayId = cosplayId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "add_purchased_item/{cosplayId}",
                arguments = listOf(navArgument("cosplayId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cosplayId = backStackEntry.arguments?.getLong("cosplayId") ?: return@composable
                AddEditPurchasedItemScreen(
                    viewModel = cosplayViewModel,
                    cosplayId = cosplayId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) { Text("Profile Screen") }
        }
    }
}
