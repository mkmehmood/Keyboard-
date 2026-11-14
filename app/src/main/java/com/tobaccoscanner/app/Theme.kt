package com.tobaccoscanner.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF7A4B1E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCC0),
    onPrimaryContainer = Color(0xFF2B1600),
    secondary = Color(0xFF6F5B49),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF8DEC9),
    onSecondaryContainer = Color(0xFF271909),
    tertiary = Color(0xFF3D7A3A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBFF0B8),
    onTertiaryContainer = Color(0xFF002203),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFF8F4),
    onBackground = Color(0xFF201A15),
    surface = Color(0xFFFFF8F4),
    onSurface = Color(0xFF201A15),
    surfaceVariant = Color(0xFFF2DFD1),
    onSurfaceVariant = Color(0xFF51443A),
    outline = Color(0xFF84746A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE8B98A),
    onPrimary = Color(0xFF452A0A),
    primaryContainer = Color(0xFF5E3811),
    onPrimaryContainer = Color(0xFFFFDCC0),
    secondary = Color(0xFFDBC2AD),
    onSecondary = Color(0xFF3E2D1F),
    secondaryContainer = Color(0xFF564435),
    onSecondaryContainer = Color(0xFFF8DEC9),
    tertiary = Color(0xFFA4D39E),
    onTertiary = Color(0xFF0A390E),
    tertiaryContainer = Color(0xFF235024),
    onTertiaryContainer = Color(0xFFBFF0B8),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF18120C),
    onBackground = Color(0xFFEDE0D8),
    surface = Color(0xFF18120C),
    onSurface = Color(0xFFEDE0D8),
    surfaceVariant = Color(0xFF51443A),
    onSurfaceVariant = Color(0xFFD5C3B7),
    outline = Color(0xFF9D8E82),
)

@Composable
fun TobaccoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
