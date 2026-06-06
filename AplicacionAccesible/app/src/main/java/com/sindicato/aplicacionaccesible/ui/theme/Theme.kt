package com.sindicato.aplicacionaccesible.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class AppTheme {
    LIGHT, DARK, COLORBLIND;

}

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary
)

private val ColorblindColorScheme = lightColorScheme(
    primary = ColorblindPrimary,
    onPrimary = ColorblindOnPrimary,
    secondary = ColorblindSecondary,
    onSecondary = ColorblindOnSecondary,
    background = ColorblindBackground,
    surface = ColorblindSurface,
    onBackground = ColorblindOnBackground,
    onSurface = ColorblindOnSurface
)

val Typo = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun AplicacionAccesibleTheme(
    appTheme: AppTheme = if (isSystemInDarkTheme()) AppTheme.DARK else AppTheme.LIGHT,
    content: @Composable () -> Unit
) {
    val colors = when (appTheme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.COLORBLIND -> ColorblindColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typo,
        content = content
    )
}
