package dev.gr1ff3n.mcnmt.ui.theme

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

// Sleek dark scheme (dark-first). Orange is the action color; navy anchors
// app bars; surfaces are deep navy-black to sit on the gradient backdrop.
private val MCNDarkScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F2A35),
    onPrimaryContainer = SleekText,

    secondary = AccentSage,
    onSecondary = Color(0xFF0A1A14),
    secondaryContainer = Color(0xFF13312A),
    onSecondaryContainer = AccentSage,

    tertiary = MCNNavyLight,
    onTertiary = MCNNavy20,
    tertiaryContainer = Color(0xFF0F2A35),
    onTertiaryContainer = SleekText,

    background = AppBgBase,
    onBackground = SleekText,
    surface = SleekSurface,
    onSurface = SleekText,
    surfaceVariant = Color(0xFF14242B),
    onSurfaceVariant = SleekTextDim,
    outline = GlassBorder,
)

@Composable
fun MCNMTTheme(
    // Dark-first: default to the sleek dark scheme regardless of system setting.
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MCNDarkScheme else MCNLightScheme,
        typography = MileageTypography,
        content = content,
    )
}
