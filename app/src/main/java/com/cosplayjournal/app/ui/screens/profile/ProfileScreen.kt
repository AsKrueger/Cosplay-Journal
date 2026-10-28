package com.cosplayjournal.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cosplayjournal.app.ui.viewmodel.ProfileViewModel

/**
 * Perfil: nickname y foto. Sin sesión muestra el acceso; con sesión, el perfil y el cambio de foto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onOpenFriends: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Perfil", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.loggedIn && state.profile != null) {
                ProfileHeader(
                    nickname = state.profile!!.nickname,
                    avatarUrl = state.profile!!.avatarUrl,
                    onSaveAvatar = { viewModel.changeAvatar(it) },
                    busy = state.busy
                )
                Button(onClick = onOpenFriends) { Text("Mis amigos") }
                OutlinedButton(onClick = { viewModel.logout() }) { Text("Cerrar sesión") }
            } else {
                AccessForm(
                    busy = state.busy,
                    onLogin = { email, password -> viewModel.login(email, password) },
                    onRegister = { nickname, email, password -> viewModel.register(nickname, email, password) }
                )
            }

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ProfileHeader(nickname: String, avatarUrl: String?, onSaveAvatar: (String) -> Unit, busy: Boolean) {
    var photo by remember(avatarUrl) { mutableStateOf(avatarUrl.orEmpty()) }

    Box(
        modifier = Modifier
            .size(120.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Tu foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(120.dp).clip(CircleShape)
            )
        } else {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp))
        }
    }
    Text(nickname, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(
        "Así te ven tus amigos. Tu email y tu teléfono no se comparten.",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = photo,
        onValueChange = { photo = it },
        label = { Text("Enlace de tu foto") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = { onSaveAvatar(photo) },
        enabled = !busy,
        modifier = Modifier.fillMaxWidth()
    ) { Text("Guardar foto") }
}

@Composable
private fun AccessForm(
    busy: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var registering by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Text(
        if (registering) "Crea tu cuenta" else "Entra en tu cuenta",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    if (registering) {
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Nickname (3 a 20 letras, números o _)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Contraseña") },
        singleLine = true,
        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = {
            if (registering) onRegister(nickname, email, password) else onLogin(email, password)
        },
        enabled = !busy && email.isNotBlank() && password.isNotBlank() && (!registering || nickname.isNotBlank()),
        modifier = Modifier.fillMaxWidth()
    ) { Text(if (registering) "Crear cuenta" else "Entrar") }

    TextButton(onClick = { registering = !registering }) {
        Text(if (registering) "Ya tengo cuenta" else "Crear una cuenta nueva")
    }
}
