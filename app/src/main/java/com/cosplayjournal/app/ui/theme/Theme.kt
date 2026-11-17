package com.cosplayjournal.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Marca: violeta eléctrico + rosa neón, con cian como acento.
val Violeta = Color(0xFF8B3DFF)
val VioletaClaro = Color(0xFFC9A7FF)
val Rosa = Color(0xFFFF4FA3)
val Cian = Color(0xFF29D3E8)

/** Colores del calendario según el estado del usuario con cada evento. */
object EstadoColores {
    val PasadoNoFui = Color(0xFFE5484D)    // rojo
    val PasadoFui = Color(0xFF9B5CFF)      // morado
    val EnCurso = Color(0xFF1E7B45)        // verde oscuro
    val FuturoVoy = Color(0xFF5ED38C)      // verde claro
    val FuturoSinMarcar = Color(0xFF3B82F6) // azul
}

val DegradadoMarca = Brush.linearGradient(listOf(Violeta, Rosa))

private val Claro = lightColorScheme(
    primary = Violeta,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDDFF),
    onPrimaryContainer = Color(0xFF2A0066),
    secondary = Rosa,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E8),
    onSecondaryContainer = Color(0xFF3E0021),
    tertiary = Color(0xFF00838F),
    background = Color(0xFFFBF7FF),
    onBackground = Color(0xFF1C1726),
    surface = Color(0xFFFBF7FF),
    onSurface = Color(0xFF1C1726),
    surfaceVariant = Color(0xFFEDE5F7),
    onSurfaceVariant = Color(0xFF4B4458),
    surfaceContainer = Color(0xFFF3EDFB),
    surfaceContainerHigh = Color(0xFFEDE6F6),
    outline = Color(0xFF7C7389),
)

private val Oscuro = darkColorScheme(
    primary = VioletaClaro,
    onPrimary = Color(0xFF3A0094),
    primaryContainer = Color(0xFF5A16C9),
    onPrimaryContainer = Color(0xFFEBDDFF),
    secondary = Color(0xFFFF8CC0),
    onSecondary = Color(0xFF5E0035),
    secondaryContainer = Color(0xFF85004D),
    onSecondaryContainer = Color(0xFFFFD9E8),
    tertiary = Cian,
    background = Color(0xFF120E1F),
    onBackground = Color(0xFFEAE1F5),
    surface = Color(0xFF120E1F),
    onSurface = Color(0xFFEAE1F5),
    surfaceVariant = Color(0xFF2B2440),
    onSurfaceVariant = Color(0xFFCDC3DB),
    surfaceContainer = Color(0xFF1C1630),
    surfaceContainerHigh = Color(0xFF261F3B),
    outline = Color(0xFF968DA5),
)

private val Tipografia = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    )
}

private val Formas = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
)

@Composable
fun CosplayJournalTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) Oscuro else Claro,
        typography = Tipografia,
        shapes = Formas,
        content = content,
    )
}
