package com.cosplayjournal.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosplayjournal.app.R
import com.cosplayjournal.app.ui.common.appViewModel
import com.cosplayjournal.app.ui.theme.DegradadoMarca

const val URL_PRIVACIDAD = "https://github.com/AsKrueger/Cosplay-Journal/blob/main/docs/PRIVACIDAD.md"

@Composable
fun AuthScreen() {
    val vm = appViewModel { AuthViewModel(it.auth) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val uri = LocalUriHandler.current
    var verPassword by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(DegradadoMarca)) {
        Column(
            Modifier.fillMaxSize().systemBarsPadding().imePadding().verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Text(
                stringResource(R.string.app_tagline), color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (ui.confirmarEmail) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MarkEmailRead, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.auth_confirma_email), textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp))
                        Button(onClick = vm::irAEntrar) {
                            Text(stringResource(R.string.auth_entrar))
                        }
                    }
                    return@Card
                }
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(if (ui.registro) R.string.auth_crear_cuenta else R.string.auth_entrar),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    if (ui.registro) {
                        OutlinedTextField(
                            value = ui.nickname, onValueChange = vm::nickname, singleLine = true,
                            label = { Text(stringResource(R.string.auth_nickname)) },
                            isError = ui.estadoNickname in setOf(EstadoNickname.INVALIDO, EstadoNickname.OCUPADO),
                            supportingText = { Text(textoNickname(ui.estadoNickname)) },
                            trailingIcon = {
                                when (ui.estadoNickname) {
                                    EstadoNickname.COMPROBANDO -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                    EstadoNickname.LIBRE -> Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary)
                                    else -> {}
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    OutlinedTextField(
                        value = ui.email, onValueChange = vm::email, singleLine = true,
                        label = { Text(stringResource(R.string.auth_email)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = ui.password, onValueChange = vm::password, singleLine = true,
                        label = { Text(stringResource(R.string.auth_password)) },
                        supportingText = if (ui.registro) {
                            { Text(stringResource(R.string.auth_password_corta)) }
                        } else null,
                        visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { verPassword = !verPassword }) {
                                Icon(if (verPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (ui.registro) {
                        Casilla(ui.mayorDe14, vm::mayorDe14, stringResource(R.string.auth_edad))
                        Casilla(ui.aceptaPrivacidad, vm::aceptaPrivacidad, stringResource(R.string.auth_privacidad))
                        TextButton(onClick = { uri.openUri(URL_PRIVACIDAD) }) {
                            Text(stringResource(R.string.auth_ver_privacidad))
                        }
                    }
                    ui.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                    Button(
                        onClick = vm::enviar, enabled = ui.puedeEnviar,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        if (ui.enviando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text(stringResource(if (ui.registro) R.string.auth_crear_cuenta else R.string.auth_entrar))
                    }
                    TextButton(onClick = vm::cambiarModo, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(stringResource(if (ui.registro) R.string.auth_ya_tengo_cuenta else R.string.auth_no_tengo_cuenta))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun textoNickname(e: EstadoNickname): String = stringResource(
    when (e) {
        EstadoNickname.LIBRE -> R.string.auth_nickname_libre
        EstadoNickname.OCUPADO -> R.string.auth_nickname_ocupado
        EstadoNickname.INVALIDO -> R.string.auth_nickname_invalido
        else -> R.string.auth_nickname_ayuda
    }
)

@Composable
private fun Casilla(marcada: Boolean, onCambio: (Boolean) -> Unit, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = marcada, onCheckedChange = onCambio)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
