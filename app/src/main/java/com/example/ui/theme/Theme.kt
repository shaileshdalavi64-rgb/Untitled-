package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Sma44Gold,
    onPrimary = BrandDarkBackground,
    primaryContainer = BrandDarkSurfaceVariant,
    onPrimaryContainer = Sma44Gold,
    secondary = BullishGreen,
    onSecondary = BrandDarkBackground,
    secondaryContainer = Color(0xFF132B20),
    onSecondaryContainer = BullishGreenLight,
    tertiary = BearishRed,
    onTertiary = BrandDarkBackground,
    tertiaryContainer = Color(0xFF331414),
    onTertiaryContainer = BearishRedLight,
    background = BrandDarkBackground,
    onBackground = TextPrimaryDark,
    surface = BrandDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = BrandDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = BrandDarkCardStroke,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB47D00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF3D6),
    onPrimaryContainer = Color(0xFF6B4700),
    secondary = Color(0xFF008738),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F8E2),
    onSecondaryContainer = Color(0xFF005221),
    tertiary = Color(0xFFD32F2F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEBEE),
    onTertiaryContainer = Color(0xFF8B0000),
    background = BrandLightBackground,
    onBackground = TextPrimaryLight,
    surface = BrandLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = BrandLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = BrandLightCardStroke,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark theme for financial terminal feel
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
