package com.cosplayjournal.app.ui.screens.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.ui.viewmodel.FriendsViewModel

/** Amigos: solicitudes recibidas, lista de amigos y envío de solicitud por nickname. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(viewModel: FriendsViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var nickname by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amigos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Añadir amigo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        viewModel.sendRequest(nickname)
                        nickname = ""
                    },
                    enabled = !state.busy && nickname.isNotBlank()
                ) { Text("Enviar") }
            }

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Text("Solicitudes recibidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (state.requests.isEmpty()) {
                Text("No tienes solicitudes pendientes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.requests.forEach { request ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(request.requester.nickname, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { viewModel.reject(request.id) }, enabled = !state.busy) { Text("Rechazar") }
                    Button(onClick = { viewModel.accept(request.id) }, enabled = !state.busy) { Text("Aceptar") }
                }
            }

            Text("Tus amigos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (state.friends.isEmpty()) {
                Text("Aún no tienes amigos añadidos", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.friends.forEach { friend ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(friend.nickname, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { viewModel.remove(friend.nickname) }, enabled = !state.busy) { Text("Quitar") }
                }
            }
        }
    }
}
