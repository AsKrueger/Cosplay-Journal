package com.cosplayjournal.app.ui.ads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.ads.AdsRepository
import com.cosplayjournal.app.data.ads.Anuncio
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Consulta los anuncios activos al abrir la app y cada 30 minutos, y rota entre ellos cada 20 s.
 * Sin anuncios (o sin conexión) no se muestra nada.
 */
class PublicidadViewModel(
    private val repo: AdsRepository,
    private val consulta: Long = 30 * 60_000L,
    private val rotacion: Long = 20_000L,
) : ViewModel() {
    private val _actual = MutableStateFlow<Anuncio?>(null)
    val actual: StateFlow<Anuncio?> = _actual.asStateFlow()
    private var anuncios: List<Anuncio> = emptyList()

    init {
        viewModelScope.launch {
            while (isActive) {
                anuncios = runCatching { repo.activos() }.getOrDefault(anuncios)
                if (anuncios.isEmpty()) _actual.value = null
                var restante = consulta
                var i = 0
                while (restante > 0 && anuncios.isNotEmpty()) {
                    _actual.value = anuncios[i % anuncios.size]
                    if (anuncios.size == 1) { delay(restante); break }
                    delay(rotacion); restante -= rotacion; i++
                }
                if (anuncios.isEmpty()) delay(consulta)
            }
        }
    }
}

@Composable
fun BarraPublicidad(vm: PublicidadViewModel) {
    val anuncio by vm.actual.collectAsStateWithLifecycle()
    val uri = LocalUriHandler.current
    AnimatedVisibility(anuncio != null, enter = expandVertically(), exit = shrinkVertically()) {
        val a = anuncio ?: return@AnimatedVisibility
        Box(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp)).height(56.dp)
                .clickable { runCatching { uri.openUri(a.destino) } }
        ) {
            AsyncImage(model = a.imagen, contentDescription = a.title, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(56.dp))
            // Obligatorio identificar la publicidad
            Row(
                Modifier.align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(bottomEnd = 8.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(stringResource(R.string.publicidad), color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
