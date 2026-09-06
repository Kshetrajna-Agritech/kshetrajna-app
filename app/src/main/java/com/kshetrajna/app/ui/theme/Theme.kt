package com.kshetrajna.app.ui.theme

import android.app.Activity
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
    primary = AgriGreenSecondary,
    onPrimary = AgriOnGreenContainer,
    primaryContainer = AgriGreenPrimary,
    onPrimaryContainer = AgriGreenContainer,
    secondary = AgriGreenSecondary,
    onSecondary = AgriOnGreenContainer,
    tertiary = AgriAmber,
    onTertiary = AgriOnAmberContainer,
    tertiaryContainer = AgriAmberContainer,
    onTertiaryContainer = AgriOnAmberContainer,
    background = AgriDarkBackground,
    onBackground = AgriLightBackground,
    surface = AgriDarkSurface,
    onSurface = AgriLightBackground,
    surfaceVariant = AgriDarkSurfaceVariant,
    onSurfaceVariant = AgriLightSurfaceVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = AgriGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = AgriGreenContainer,
    onPrimaryContainer = AgriOnGreenContainer,
    secondary = AgriGreenSecondary,
    onSecondary = Color.White,
    tertiary = AgriAmber,
    onTertiary = Color.White,
    tertiaryContainer = AgriAmberContainer,
    onTertiaryContainer = AgriOnAmberContainer,
    background = AgriLightBackground,
    onBackground = AgriOnGreenContainer,
    surface = AgriLightSurface,
    onSurface = AgriOnGreenContainer,
    surfaceVariant = AgriLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF2C3E30),
)

@Composable
fun KshetrajnaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}