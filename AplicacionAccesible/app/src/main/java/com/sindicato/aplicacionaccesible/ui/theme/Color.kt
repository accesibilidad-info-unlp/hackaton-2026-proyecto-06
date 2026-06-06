package com.sindicato.aplicacionaccesible.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Standard Light
val LightPrimary = Color(0xFF0061A4)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightSecondary = Color(0xFF535F70)
val LightOnSecondary = Color(0xFFFFFFFF)

// Standard Dark
val DarkPrimary = Color(0xFF9ECAFF)
val DarkOnPrimary = Color(0xFF003258)
val DarkSecondary = Color(0xFFBBC7DB)
val DarkOnSecondary = Color(0xFF253140)

// Colorblind (High Contrast - Blue/Yellow)
val ColorblindPrimary = Color(0xFF004488) // Deep Blue
val ColorblindOnPrimary = Color(0xFFFFFFFF)
val ColorblindSecondary = Color(0xFFEEEE00) // Bright Yellow
val ColorblindOnSecondary = Color(0xFF000000)
val ColorblindBackground = Color(0xFFFFFFFF)
val ColorblindSurface = Color(0xFFFFFFFF)
val ColorblindOnBackground = Color(0xFF000000)
val ColorblindOnSurface = Color(0xFF000000)

// Colorblind-safe palette (High contrast and distinct hues)
val SafeRed = Color(0xFFD55E00)    // Vermillion
val SafeBlue = Color(0xFF0072B2)   // Blue
val SafeYellow = Color(0xFFF0E442) // Yellow
val SafeGreen = Color(0xFF009E73)  // Bluish Green
val SafeOrange = Color(0xFFE69F00) // Orange
val SafePurple = Color(0xFFCC79A7) // Reddish Purple

val SafeColors = listOf(SafeRed, SafeBlue, SafeYellow, SafeGreen, SafeOrange, SafePurple)


@Composable
fun getContrastColor(backgroundColor: Color): Color {
    // If luminance > 0.5, the color is "light", so use black text.
    // Otherwise, use white.
    return if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
}