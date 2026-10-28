package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// LIQUID GLASS DESIGN SYSTEM (iOS & Modern Glassmorphism Aesthetic)
// ============================================================================

val GlassSpecularWhiteBorder = Brush.verticalGradient(
    listOf(
        Color.White.copy(alpha = 0.28f),
        Color.White.copy(alpha = 0.06f)
    )
)

val GlassEmeraldGlowBorder = Brush.linearGradient(
    listOf(
        WhatsAppBadgeGreen.copy(alpha = 0.55f),
        Color.White.copy(alpha = 0.18f),
        Color(0xFF00A884).copy(alpha = 0.25f)
    )
)

val GlassLiquidSurfaceDark = Brush.verticalGradient(
    listOf(
        Color(0xFF1E2C38).copy(alpha = 0.72f),
        Color(0xFF0D171F).copy(alpha = 0.60f)
    )
)

val GlassLiquidSurfaceActive = Brush.verticalGradient(
    listOf(
        Color(0xFF005C4B).copy(alpha = 0.68f),
        Color(0xFF063B32).copy(alpha = 0.50f)
    )
)

val GlassAccentGlow = Color(0xFF25D366).copy(alpha = 0.18f)

/**
 * Reusable modifier that applies a frosted liquid glass effect with
 * specular rim reflections, translucent refraction, and top-edge bevel.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    surfaceBrush: Brush = GlassLiquidSurfaceDark,
    borderBrush: Brush = GlassSpecularWhiteBorder,
    borderWidth: Dp = 1.dp,
    hasTopSpecularHighlight: Boolean = true
): Modifier = this
    .clip(shape)
    .background(surfaceBrush)
    .then(
        if (hasTopSpecularHighlight) {
            Modifier.drawBehind {
                // Subtle liquid light reflection on the upper hemisphere
                val specularHeight = size.height * 0.35f
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = specularHeight
                    ),
                    size = size.copy(height = specularHeight)
                )
            }
        } else {
            Modifier
        }
    )
    .border(width = borderWidth, brush = borderBrush, shape = shape)

/**
 * Ambient backdrop with fluid, luminous emerald, cyan, and deep azure mesh orbs.
 * Sits beneath all screens so translucent glass materials refract ambient liquid light.
 */
@Composable
fun AmbientLiquidBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090E11))
            .drawBehind {
                val w = size.width
                val h = size.height

                // Top-right luminous emerald liquid orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WhatsAppBadgeGreen.copy(alpha = 0.14f),
                            Color(0xFF00A884).copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.88f, h * 0.12f),
                        radius = w * 0.75f
                    )
                )

                // Middle-left fluid cyan glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF06B6D4).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.08f, h * 0.48f),
                        radius = w * 0.65f
                    )
                )

                // Bottom-center liquid teal halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF005C4B).copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.50f, h * 0.90f),
                        radius = w * 0.80f
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * Reusable Liquid Glass Card component with interactive ripple and specular bevel.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    surfaceBrush: Brush = GlassLiquidSurfaceDark,
    borderBrush: Brush = GlassSpecularWhiteBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = WhatsAppBadgeGreen),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                surfaceBrush = surfaceBrush,
                borderBrush = borderBrush,
                borderWidth = borderWidth,
                hasTopSpecularHighlight = true
            )
            .then(clickableModifier)
    ) {
        content()
    }
}
