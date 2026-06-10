package dev.gr1ff3n.mcnmt.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.gr1ff3n.mcnmt.R

/** Poppins — rounded geometric sans matching the MCN wordmark (bundled, OFL). */
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private val default = Typography()

val MileageTypography = Typography(
    displaySmall = default.displaySmall.copy(fontFamily = Poppins, fontWeight = FontWeight.Bold),
    headlineMedium = default.headlineMedium.copy(fontFamily = Poppins, fontWeight = FontWeight.SemiBold),
    headlineSmall = default.headlineSmall.copy(fontFamily = Poppins, fontWeight = FontWeight.SemiBold),
    titleLarge = default.titleLarge.copy(fontFamily = Poppins, fontWeight = FontWeight.SemiBold),
    titleMedium = default.titleMedium.copy(fontFamily = Poppins, fontWeight = FontWeight.SemiBold),
    titleSmall = default.titleSmall.copy(fontFamily = Poppins, fontWeight = FontWeight.Medium),
    bodyLarge = default.bodyLarge.copy(fontFamily = Poppins),
    bodyMedium = default.bodyMedium.copy(fontFamily = Poppins),
    bodySmall = default.bodySmall.copy(fontFamily = Poppins),
    labelLarge = default.labelLarge.copy(fontFamily = Poppins, fontWeight = FontWeight.Medium),
    labelMedium = default.labelMedium.copy(fontFamily = Poppins, fontWeight = FontWeight.Medium),
    labelSmall = default.labelSmall.copy(fontFamily = Poppins, fontWeight = FontWeight.Medium),
)

/** Oversized number style for hero stats (miles, dollars). */
val StatNumber = TextStyle(
    fontFamily = Poppins,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
)
