package dev.gr1ff3n.mcnmt.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The app's signature backdrop: a near-black base with a navy glow bleeding from
 * the top-left and a faint orange glow at the top-right. Drawn in a [drawBehind]
 * so the glow centers scale with the element size (resolution-independent).
 */
fun Modifier.appBackground(): Modifier = this
    .fillMaxSize()
    .drawBehind {
        drawRect(AppBgBase)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(AppBgGlowNavy.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(size.width * 0.25f, -size.height * 0.02f),
                radius = size.width * 1.15f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(AppBgGlowOrange, Color.Transparent),
                center = Offset(size.width * 1.0f, size.height * 0.05f),
                radius = size.width * 0.85f,
            ),
        )
    }
