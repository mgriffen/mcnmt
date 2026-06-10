package dev.gr1ff3n.mcnmt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import dev.gr1ff3n.mcnmt.ui.theme.GlassBorder
import dev.gr1ff3n.mcnmt.ui.theme.GlassFill
import dev.gr1ff3n.mcnmt.ui.theme.GlassFillStrong

/**
 * Translucent "glass" surface used throughout the sleek dark UI: a faint white
 * fill with a hairline border and a soft drop shadow, sitting over the app's
 * gradient backdrop. (Approximates frosted glass without a true backdrop blur,
 * which is costly/limited on Android — over the smooth gradient it reads the
 * same.)
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    strong: Boolean = false,
    cornerRadius: Int = 22,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    Box(
        modifier = modifier
            .shadow(elevation = 14.dp, shape = shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(if (strong) GlassFillStrong else GlassFill)
            .border(1.dp, GlassBorder, shape)
            .padding(contentPadding),
    ) {
        content()
    }
}
