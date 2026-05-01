package dev.gr1ff3n.mcnmt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MCNLightScheme = lightColorScheme(
    primary = MCNNavy,
    onPrimary = Color.White,
    primaryContainer = MCNNavy90,
    onPrimaryContainer = MCNNavy20,

    secondary = MCNOrange,
    onSecondary = Color.White,
    secondaryContainer = MCNOrange90,
    onSecondaryContainer = MCNOrange20,

    tertiary = MCNLightBlue,
    onTertiary = MCNLightBlue20,
    tertiaryContainer = MCNLightBlue90,
    onTertiaryContainer = MCNLightBlue20,

    background = LightSurface,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
)

private val MCNDarkScheme = darkColorScheme(
    primary = MCNNavyLight,
    onPrimary = MCNNavy20,
    primaryContainer = MCNNavy,
    onPrimaryContainer = MCNNavy90,

    secondary = MCNOrangeLight,
    onSecondary = MCNOrange20,
    secondaryContainer = Color(0xFF7A3E00),
    onSecondaryContainer = MCNOrange90,

    tertiary = MCNLightBlue,
    onTertiary = MCNLightBlue20,
    tertiaryContainer = MCNLightBlue20,
    onTertiaryContainer = MCNLightBlue90,

    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
)

@Composable
fun MCNMTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MCNDarkScheme else MCNLightScheme,
        typography = MileageTypography,
        content = content,
    )
}
