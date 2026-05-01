package dev.gr1ff3n.mcnmt.ui.theme

import androidx.compose.ui.graphics.Color

// MCN brand colors (per MCN_GUIDELINES_V1.pdf)
val MCNNavy = Color(0xFF003E52)
val MCNOrange = Color(0xFFF77300)
val MCNLightBlue = Color(0xFFBDCDC3)

// Tonal variants — hand-derived for Material 3 ColorScheme.
// Light scheme uses the brand colors directly; dark scheme uses lighter
// tints because the brand navy is too dark for "primary on dark surface."
val MCNNavy90 = Color(0xFFCCE4EE)       // pale navy tint for light primaryContainer
val MCNNavy20 = Color(0xFF002A38)       // deep navy for onPrimaryContainer
val MCNNavyLight = Color(0xFF8FCDDD)    // dark-mode primary

val MCNOrange90 = Color(0xFFFFDCC0)     // pale orange tint for light secondaryContainer
val MCNOrange20 = Color(0xFF5C2A00)     // deep orange for onSecondaryContainer
val MCNOrangeLight = Color(0xFFFFB077)  // dark-mode secondary

val MCNLightBlue90 = Color(0xFFE7EFEB)
val MCNLightBlue20 = Color(0xFF3A4641)

// Surface family
val LightSurface = Color(0xFFFAFCFB)
val LightOnSurface = Color(0xFF1A1C1B)
val LightSurfaceVariant = Color(0xFFE6EBE9)
val LightOnSurfaceVariant = Color(0xFF454B49)

val DarkSurface = Color(0xFF0F1A1F)
val DarkOnSurface = Color(0xFFE0E5E7)
val DarkSurfaceVariant = Color(0xFF1B2A30)
val DarkOnSurfaceVariant = Color(0xFFC2CDD2)
