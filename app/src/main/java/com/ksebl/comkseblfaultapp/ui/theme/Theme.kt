package com.ksebl.comkseblfaultapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9D5FF), // Light purple/lavender
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDD4), // Light orange/peach
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD6E8), // Light magenta/pink
    onTertiaryContainer = Color(0xFF831843),
    background = Background,
    onBackground = Color(0xFF1C1B1F),
    surface = Surface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFFFE4F5), // Soft pink
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF9F7AEA), // Purple outline
    error = Error,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF2D1B69), // Deep purple
    primaryContainer = Color(0xFF4C1D95), // Dark purple
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = Secondary,
    onSecondary = Color(0xFF431407), // Deep orange
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Color(0xFFFFDDD4),
    tertiary = Tertiary,
    onTertiary = Color(0xFF4A0E27), // Deep magenta
    tertiaryContainer = Color(0xFF831843),
    onTertiaryContainer = Color(0xFFFFD6E8),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF2D1B40), // Dark purple surface
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF3A2857), // Darker purple/magenta
    onSurfaceVariant = Color(0xFFE9D5FF),
    outline = Color(0xFFA78BFA), // Light purple outline
    error = Error,
    onError = Color(0xFF601410)
)

@Composable
fun ComkseblfaultappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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