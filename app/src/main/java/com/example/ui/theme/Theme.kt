package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val JarvisDarkColorScheme = darkColorScheme(
    primary = HudCyan,
    onPrimary = HudBgDark,
    primaryContainer = HudCyanDeep,
    onPrimaryContainer = HudOnDark,
    secondary = HudArcBlue,
    onSecondary = Color.White,
    secondaryContainer = HudSurfaceVariantDark,
    onSecondaryContainer = HudOnDark,
    tertiary = HudEmerald,
    onTertiary = HudBgDark,
    background = HudBgDark,
    onBackground = HudOnDark,
    surface = HudSurfaceDark,
    onSurface = HudOnDark,
    surfaceVariant = HudSurfaceVariantDark,
    onSurfaceVariant = HudOnSurfaceVariantDark,
    outline = HudOutlineDark,
    error = HudCoral,
    onError = Color.White
)

private val JarvisLightColorScheme = lightColorScheme(
    primary = HudPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFF6FC),
    onPrimaryContainer = HudOnLight,
    secondary = HudSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = HudSurfaceVariantLight,
    onSecondaryContainer = HudOnLight,
    tertiary = Color(0xFF00894B),
    onTertiary = Color.White,
    background = HudBgLight,
    onBackground = HudOnLight,
    surface = HudSurfaceLight,
    onSurface = HudOnLight,
    surfaceVariant = HudSurfaceVariantLight,
    onSurfaceVariant = HudOnSurfaceVariantLight,
    outline = HudOutlineLight,
    error = Color(0xFFD32F2F),
    onError = Color.White
)

val JarvisShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Immutable
data class JarvisExtendedColors(
    val statusSuccess: Color,
    val statusWarning: Color,
    val hudGlow: Color,
    val hudGridTint: Color
)

val LocalJarvisExtendedColors = staticCompositionLocalOf {
    JarvisExtendedColors(
        statusSuccess = HudEmerald,
        statusWarning = HudAmber,
        hudGlow = HudCyan,
        hudGridTint = HudOutlineDark
    )
}

object JarvisThemeExtras {
    val colors: JarvisExtendedColors
        @Composable get() = LocalJarvisExtendedColors.current
}

@Composable
fun JarvisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) JarvisDarkColorScheme else JarvisLightColorScheme
    val extendedColors = if (darkTheme) {
        JarvisExtendedColors(
            statusSuccess = HudEmerald,
            statusWarning = HudAmber,
            hudGlow = HudCyan,
            hudGridTint = HudOutlineDark
        )
    } else {
        JarvisExtendedColors(
            statusSuccess = Color(0xFF00894B),
            statusWarning = Color(0xFFEF6C00),
            hudGlow = HudPrimaryLight,
            hudGridTint = HudOutlineLight
        )
    }

    CompositionLocalProvider(LocalJarvisExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = JarvisTypography,
            shapes = JarvisShapes,
            content = content
        )
    }
}
