package com.cosplayjournal.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosplayjournal.app.data.chat.ResumenChat
import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.social.Contacto
import com.cosplayjournal.app.data.social.Relacion
import com.cosplayjournal.app.ui.chat.ChatListScreen
import com.cosplayjournal.app.ui.chat.ChatRoomScreen
import com.cosplayjournal.app.ui.chat.ChatRoomViewModel
import com.cosplayjournal.app.ui.chat.ChatsViewModel
import java.time.LocalDate
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch
import com.cosplayjournal.app.data.ErrorApp
import com.cosplayjournal.app.data.clasificar
import com.cosplayjournal.app.ui.common.appContainer
import com.cosplayjournal.app.ui.common.texto
import com.cosplayjournal.app.ui.gallery.AccionFoto
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
import com.cosplayjournal.app.ui.friends.FriendProfileScreen
import com.cosplayjournal.app.ui.friends.FriendProfileViewModel
import com.cosplayjournal.app.ui.friends.FriendsScreen
import com.cosplayjournal.app.ui.friends.FriendsViewModel
import com.cosplayjournal.app.ui.profile.ProfileScreen
import com.cosplayjournal.app.ui.profile.ProfileViewModel
import com.cosplayjournal.app.ui.settings.BlockedScreen
import com.cosplayjournal.app.ui.settings.SettingsScreen

/** Pestañas de la barra inferior. Cada fase añade la suya. */
enum class Pestaña(val ruta: String, @StringRes val titulo: Int, val icono: ImageVector, val iconoActivo: ImageVector) {
    CALENDARIO("calendario", R.string.nav_calendario, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    GALERIA("galeria", R.string.nav_galeria, Icons.Outlined.PhotoLibrary, Icons.Filled.PhotoLibrary),
    CHATS("chats", R.string.nav_chats, Icons.AutoMirrored.Outlined.Chat, Icons.AutoMirrored.Filled.Chat),
    AMIGOS("amigos", R.string.nav_amigos, Icons.Outlined.People, Icons.Filled.People),
    PERFIL("perfil", R.string.nav_perfil, Icons.Outlined.Person, Icons.Filled.Person),
}

object Rutas {
    const val AJUSTES = "ajustes"
    const val BLOQUEADOS = "bloqueados"
    const val AMIGO = "amigo/{id}"
    const val CHAT = "chat/{id}"
    fun chat(id: String) = "chat/$id"
    fun amigo(id: String) = "amigo/$id"
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
                GalleryScreen(vm, accionesExtra = listOf(accionEscaparate()))
            }
            composable(Pestaña.CHATS.ruta) {
                val c = appContainer()
                val vm = appViewModel { ChatsViewModel(it.chats, it.social) }
                val eventos by c.eventos.eventos.collectAsStateWithLifecycle(emptyList())
                val hoy = LocalDate.now()
                val misEventos = eventos.filter { it.estado.asistencia == Asistencia.GOING && it.evento.fin >= hoy }
                ChatListScreen(vm, misEventos, onChat = { id -> nav.navigate(Rutas.chat(id)) })
            }
            composable(Rutas.CHAT) { entrada ->
                val id = entrada.arguments!!.getString("id")!!
                val c = appContainer()
                val vm = appViewModel(key = "chat$id") { ChatRoomViewModel(id, it.chats, it.auth.userIdActual()) }
                val resumen by produceState<ResumenChat?>(null, id) { value = runCatching { c.chats.misChats().firstOrNull { it.id == id } }.getOrNull() }
                val amigos by produceState(emptyList<Contacto>()) {
                    value = runCatching { c.social.contactos().filter { it.relacion == Relacion.FRIEND } }.getOrDefault(emptyList())
                }
                ChatRoomScreen(vm, resumen, amigos, onAtras = nav::popBackStack, onEvento = { e -> nav.navigate(Rutas.evento(e)) })
            }
            composable(Pestaña.AMIGOS.ruta) {
                val vm = appViewModel { FriendsViewModel(it.social) }
                FriendsScreen(vm, onAmigo = { id -> nav.navigate(Rutas.amigo(id)) })
            }
            composable(Rutas.AMIGO) { entrada ->
                val id = entrada.arguments!!.getString("id")!!
                val vm = appViewModel(key = "amigo$id") { FriendProfileViewModel(id, it.social, it.eventos.eventos) }
                val c = appContainer()
                val scope = rememberCoroutineScope()
                FriendProfileScreen(
                    vm, onAtras = nav::popBackStack, onEvento = { e -> nav.navigate(Rutas.evento(e)) },
                    onEscribir = { scope.launch { runCatching { c.chats.abrirDirecto(id) }.onSuccess { nav.navigate(Rutas.chat(it)) } } },
                )
            }
            composable(Pestaña.PERFIL.ruta) {
                val vm = appViewModel { c -> ProfileViewModel(c.perfiles, c.social, c.eventos.eventos, userId = { c.auth.userIdActual() }) }
                ProfileScreen(vm, onAjustes = { nav.navigate(Rutas.AJUSTES) }, onEvento = { e -> nav.navigate(Rutas.evento(e)) })
            }
            composable(Rutas.BLOQUEADOS) { BlockedScreen(onAtras = nav::popBackStack) }
            composable(Rutas.AJUSTES) { SettingsScreen(onAtras = nav::popBackStack, onBloqueados = { nav.navigate(Rutas.BLOQUEADOS) }) }
        }
    }
}

/** Sube la foto abierta en el visor al escaparate del perfil. */
@Composable
private fun accionEscaparate(): AccionFoto {
    val c = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return AccionFoto(Icons.Filled.Star, R.string.esc_anadir) { foto ->
        Toast.makeText(context, R.string.esc_subiendo, Toast.LENGTH_SHORT).show()
        scope.launch {
            val r = runCatching { c.social.añadirAlEscaparate(foto.foto.uri, foto.etiquetas.eventoId, null) }
            val err = r.exceptionOrNull()?.let { clasificar(it) }
            val msg = when {
                err == null -> R.string.esc_anadida
                err is ErrorApp.Servidor && err.codigo == "showcase_full" -> R.string.esc_lleno
                else -> err.texto()
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }
}
