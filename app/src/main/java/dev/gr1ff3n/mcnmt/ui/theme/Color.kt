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

// ───────────── Sleek dark theme (dark-first) ─────────────
// Deep navy-black base with a navy glow at top and an orange action color.
val AppBgBase = Color(0xFF070F13)        // near-black base
val AppBgGlowNavy = Color(0xFF154150)    // top-left navy glow
val AppBgGlowOrange = Color(0x2EF77300)  // faint orange glow, top-right (~18% alpha)

val SleekSurface = Color(0xFF0E1A20)     // raised dark surface
val SleekText = Color(0xFFEAF2F4)        // primary text
val SleekTextDim = Color(0xFF8FA3AB)     // secondary text

val AccentOrange = Color(0xFFF77300)     // brand orange (actions)
val AccentOrangeBright = Color(0xFFFF9A44) // highlight for gradients/glow
val AccentSage = Color(0xFF9FD0BF)       // quiet success/accent (light-blue/sage)

// Glass card tokens (translucent fill + hairline border over the gradient).
val GlassFill = Color(0x0FFFFFFF)        // ~6% white
val GlassFillStrong = Color(0x17FFFFFF)  // ~9% white
val GlassBorder = Color(0x26FFFFFF)      // ~15% white
val FieldBorder = Color(0x40FFFFFF)      // ~25% white — visible form-field outline
