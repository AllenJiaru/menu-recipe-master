package com.example.shiyu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = DinerColorLight,
    onSecondaryContainer = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = Color.White,
    outline = BorderColor,
    outlineVariant = DividerColor
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkKawaiiPalette.Primary,
    onPrimary = Color.White,
    primaryContainer = DarkKawaiiPalette.PrimaryLight,
    onPrimaryContainer = DarkKawaiiPalette.TextPrimary,
    secondary = DarkKawaiiPalette.Secondary,
    onSecondary = Color.White,
    secondaryContainer = DarkKawaiiPalette.DinerColorLight,
    onSecondaryContainer = DarkKawaiiPalette.TextPrimary,
    background = DarkKawaiiPalette.Background,
    onBackground = DarkKawaiiPalette.TextPrimary,
    surface = DarkKawaiiPalette.Surface,
    onSurface = DarkKawaiiPalette.TextPrimary,
    surfaceVariant = DarkKawaiiPalette.SurfaceGray,
    onSurfaceVariant = DarkKawaiiPalette.TextSecondary,
    error = DarkKawaiiPalette.Error,
    onError = Color.White,
    outline = DarkKawaiiPalette.BorderColor,
    outlineVariant = DarkKawaiiPalette.DividerColor
)

@Composable
fun ShiyuTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = ThemeManager(context)

    val currentThemeMode by themeManager.themeMode.collectAsState(initial = themeMode)

    val isDarkTheme = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
