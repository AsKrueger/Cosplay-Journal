package com.cosplayjournal.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00ACC1),
    secondary = Color(0xFF4DB6AC),
    tertiary = Color(0xFFFB8C00),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00ACC1),
    secondary = Color(0xFF4DB6AC),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun CosplayJournalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
