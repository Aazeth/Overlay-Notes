package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.Black,
    primaryContainer = AmberOnPrimaryContainer,
    onPrimaryContainer = AmberPrimaryContainer,
    secondary = Color(0xFF94A3B8),
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Color.White,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

private val LightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = AmberOnPrimary,
    primaryContainer = AmberPrimaryContainer,
    onPrimaryContainer = AmberOnPrimaryContainer,
    secondary = SlateSecondary,
    secondaryContainer = SlateSecondaryContainer,
    onSecondaryContainer = SlateOnSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
