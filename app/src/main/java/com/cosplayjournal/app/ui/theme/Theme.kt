package com.cosplayjournal.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandPinkLight,
    onPrimary = Color.Black,
    secondary = BrandVioletLight,
    onSecondary = Color.Black,
    tertiary = BrandAmber,
    onTertiary = Color.Black,
    background = Color(0xFF141216),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1B21),
    onSurface = Color(0xFFE6E1E5),
    outline = Color(0xFF5C5260)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPink,
    onPrimary = Color.White,
    secondary = BrandViolet,
    onSecondary = Color.White,
    tertiary = BrandAmber,
    onTertiary = Color.Black,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    outline = OutlineLight
)

/** Modo claro por defecto. El oscuro existe, pero no sigue al sistema: hay que pasar darkTheme = true. */
@Composable
fun CosplayJournalTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
