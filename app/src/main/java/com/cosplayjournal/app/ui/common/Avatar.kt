package com.cosplayjournal.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.social.urlPrivada
import com.cosplayjournal.app.ui.theme.Rosa
import com.cosplayjournal.app.ui.theme.Violeta
import kotlin.math.absoluteValue

private val FONDOS = listOf(Violeta, Rosa, Color(0xFF00A3B8), Color(0xFFFF8A00), Color(0xFF3B82F6), Color(0xFF1E9E5A))

/** Foto de perfil o, si no hay, la inicial sobre un color fijo por nickname. */
@Composable
fun Avatar(nickname: String, avatarPath: String?, tamaño: Dp = 44.dp, modifier: Modifier = Modifier) {
    Box(
        modifier.size(tamaño).clip(CircleShape).background(FONDOS[nickname.hashCode().absoluteValue % FONDOS.size]),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            nickname.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold,
            fontSize = (tamaño.value * 0.42f).sp, style = MaterialTheme.typography.titleMedium,
        )
        urlPrivada("avatars", avatarPath)?.let { url ->
            AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(tamaño))
        }
    }
}
