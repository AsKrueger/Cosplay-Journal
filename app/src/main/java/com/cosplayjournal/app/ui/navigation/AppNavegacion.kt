package com.cosplayjournal.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cosplayjournal.app.R
import com.cosplayjournal.app.ui.calendar.CalendarScreen
import com.cosplayjournal.app.ui.calendar.CalendarViewModel
import com.cosplayjournal.app.ui.calendar.EventDetailScreen
import com.cosplayjournal.app.ui.calendar.EventDetailViewModel
import com.cosplayjournal.app.ui.common.appViewModel
import com.cosplayjournal.app.ui.gallery.GalleryScreen
import com.cosplayjournal.app.ui.gallery.GalleryViewModel
import com.cosplayjournal.app.ui.profile.ProfileScreen
import com.cosplayjournal.app.ui.settings.SettingsScreen

/** Pestañas de la barra inferior. Cada fase añade la suya. */
enum class Pestaña(val ruta: String, @StringRes val titulo: Int, val icono: ImageVector, val iconoActivo: ImageVector) {
    CALENDARIO("calendario", R.string.nav_calendario, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    GALERIA("galeria", R.string.nav_galeria, Icons.Outlined.PhotoLibrary, Icons.Filled.PhotoLibrary),
    PERFIL("perfil", R.string.nav_perfil, Icons.Outlined.Person, Icons.Filled.Person),
}

object Rutas {
    const val AJUSTES = "ajustes"
    const val EVENTO = "evento/{id}"
    fun evento(id: Long) = "evento/$id"
}

@Composable
fun AppNavegacion(eventoInicial: Long? = null, nav: NavHostController = rememberNavController()) {
    // Al abrir desde un recordatorio se muestra directamente el evento
    LaunchedEffect(eventoInicial) { eventoInicial?.let { nav.navigate(Rutas.evento(it)) } }

    val entrada by nav.currentBackStackEntryAsState()
    val destino = entrada?.destination
    val enPestaña = Pestaña.entries.any { p -> destino?.hierarchy?.any { it.route == p.ruta } == true }

    Scaffold(
        bottomBar = {
            if (enPestaña && Pestaña.entries.size > 1) {
                NavigationBar {
                    Pestaña.entries.forEach { p ->
                        val activa = destino?.hierarchy?.any { it.route == p.ruta } == true
                        NavigationBarItem(
                            selected = activa,
                            onClick = {
                                nav.navigate(p.ruta) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (activa) p.iconoActivo else p.icono, null) },
                            label = { Text(stringResource(p.titulo)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            nav, startDestination = Pestaña.entries.first().ruta,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()).consumeWindowInsets(padding),
        ) {
            composable(Pestaña.CALENDARIO.ruta) {
                val vm = appViewModel { CalendarViewModel(it.eventos, it.settings) }
                CalendarScreen(vm, onEvento = { id -> nav.navigate(Rutas.evento(id)) })
            }
            composable(Rutas.EVENTO, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entrada ->
                val id = entrada.arguments!!.getLong("id")
                val vm = appViewModel(key = "evento$id") { EventDetailViewModel(id, it.eventos, it.avisos) }
                EventDetailScreen(vm, onAtras = nav::popBackStack)
            }
            composable(Pestaña.GALERIA.ruta) {
                val vm = appViewModel { c -> GalleryViewModel(c.galeria, c.eventos.eventos) }
                GalleryScreen(vm)
            }
            composable(Pestaña.PERFIL.ruta) { ProfileScreen(onAjustes = { nav.navigate(Rutas.AJUSTES) }) }
            composable(Rutas.AJUSTES) { SettingsScreen(onAtras = nav::popBackStack) }
        }
    }
}
