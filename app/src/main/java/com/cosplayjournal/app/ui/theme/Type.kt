package com.cosplayjournal.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cosplayjournal.app.R

// Si decides descargar los archivos .ttf y ponerlos en res/font, usa este código:
// Val Anybody = FontFamily(Font(R.font.anybody_bold, FontWeight.Bold))
// Val BeVietnamPro = FontFamily(Font(R.font.bevietnampro_regular, FontWeight.Normal))
// Val SpaceGrotesk = FontFamily(Font(R.font.spacegrotesk_medium, FontWeight.Medium))

// Por ahora usaremos FontFamily.Default para que el código compile, 
// pero configurado para ser reemplazado fácilmente.

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default, // Reemplazar por Anybody
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default, // Reemplazar por Anybody
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default, // Reemplazar por Anybody
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Reemplazar por Be Vietnam Pro
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default, // Reemplazar por Space Grotesk
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
