package com.cosplayjournal.app.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosplayjournal.app.R
import com.cosplayjournal.app.data.auth.EstadoSesion
import com.cosplayjournal.app.ui.auth.AuthScreen
import com.cosplayjournal.app.ui.common.Aviso
import com.cosplayjournal.app.ui.common.Cargando
import com.cosplayjournal.app.ui.common.appContainer
import com.cosplayjournal.app.ui.navigation.AppNavegacion
import com.cosplayjournal.app.ui.theme.CosplayJournalTheme

// AppCompatActivity: necesaria para cambiar el idioma desde Ajustes en Android 12 o anterior
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CosplayJournalTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Raiz(intent.getLongExtra("evento", -1).takeIf { it > 0 })
                }
            }
        }
    }
}

@Composable
private fun Raiz(eventoInicial: Long?) {
    val c = appContainer()
    if (!c.configurado) {
        Aviso(Icons.Default.CloudOff, stringResource(R.string.config_falta_titulo), stringResource(R.string.config_falta_texto))
        return
    }
    val estado by c.auth.estado.collectAsStateWithLifecycle(EstadoSesion.Cargando)
    when (estado) {
        EstadoSesion.Cargando -> Cargando()
        EstadoSesion.SinSesion -> AuthScreen()
        is EstadoSesion.Conectado -> AppNavegacion(eventoInicial)
    }
}
