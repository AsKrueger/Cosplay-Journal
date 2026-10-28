package com.cosplayjournal.app.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.data.remote.RemoteConversation
import com.cosplayjournal.app.ui.viewmodel.ChatRoomViewModel
import com.cosplayjournal.app.ui.viewmodel.ChatsViewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

/** Lista de chats y grupos, con alta de chat privado y de grupo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(viewModel: ChatsViewModel, onOpenChat: (String) -> Unit, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var nickname by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var groupMembers by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Nuevo chat con un amigo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                            viewModel.openPrivate(nickname) { onOpenChat(it) }
                            nickname = ""
                        },
                        enabled = !state.busy && nickname.isNotBlank()
                    ) { Text("Abrir") }
                }
            }
            item {
                Text("Nuevo grupo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nombre del grupo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = groupMembers,
                    onValueChange = { groupMembers = it },
                    label = { Text("Amigos, separados por coma") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        viewModel.createGroup(groupName, groupMembers) { onOpenChat(it) }
                        groupName = ""
                        groupMembers = ""
                    },
                    enabled = !state.busy && groupName.isNotBlank() && groupMembers.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Crear grupo") }
            }
            state.message?.let {
                item { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
            item { Text("Tus conversaciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (state.conversations.isEmpty()) {
                item { Text("Aún no tienes conversaciones", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(state.conversations) { conversation ->
                ConversationRow(conversation, onClick = { onOpenChat(conversation.id) })
            }
        }
    }
}

@Composable
private fun ConversationRow(conversation: RemoteConversation, onClick: () -> Unit) {
    val title = if (conversation.type == "GROUP") {
        conversation.name ?: "Grupo"
    } else {
        conversation.memberNicknames.joinToString(" y ")
    }
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                if (conversation.type == "GROUP") "Grupo · " + conversation.memberNicknames.size + " miembros" else "Chat privado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Conversación: mensajes que se actualizan solos, con texto y foto por enlace. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(viewModel: ChatRoomViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var text by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var showPhoto by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (showPhoto) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = photoUrl,
                            onValueChange = { photoUrl = it },
                            label = { Text("Enlace de la foto") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                viewModel.sendPhoto(photoUrl)
                                photoUrl = ""
                                showPhoto = false
                            },
                            enabled = photoUrl.isNotBlank()
                        ) { Text("Enviar") }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showPhoto = !showPhoto }) { Text("Foto") }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Escribe un mensaje") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            viewModel.sendText(text)
                            text = ""
                        },
                        enabled = text.isNotBlank()
                    ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar") }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(message.senderNickname, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (message.kind == "IMAGE") {
                        AsyncImage(
                            model = message.body,
                            contentDescription = "Foto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(220.dp).clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Text(message.body)
                    }
                }
            }
        }
    }
}
