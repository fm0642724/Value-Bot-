package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.GamingBorder
import com.example.ui.theme.GamingBorderRedGlow
import com.example.ui.theme.GamingSurface
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedPrimary

/**
 * Custom polygonal chamfered shape for sci-fi gaming cards and buttons.
 */
class ChamferedShape(
    private val chamferSize: Dp,
    private val chamferTopLeft: Boolean = true,
    private val chamferTopRight: Boolean = true,
    private val chamferBottomRight: Boolean = true,
    private val chamferBottomLeft: Boolean = true
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val c = with(density) { chamferSize.toPx() }
        val path = Path().apply {
            if (chamferTopLeft) {
                moveTo(c, 0f)
            } else {
                moveTo(0f, 0f)
            }

            if (chamferTopRight) {
                lineTo(size.width - c, 0f)
                lineTo(size.width, c)
            } else {
                lineTo(size.width, 0f)
            }

            if (chamferBottomRight) {
                lineTo(size.width, size.height - c)
                lineTo(size.width - c, size.height)
            } else {
                lineTo(size.width, size.height)
            }

            if (chamferBottomLeft) {
                lineTo(c, size.height)
                lineTo(0f, size.height - c)
            } else {
                lineTo(0f, size.height)
            }

            if (chamferTopLeft) {
                lineTo(0f, c)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Cyberpunk HUD container with chamfered edges and glowing border accents.
 */
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    chamferSize: Dp = 10.dp,
    borderColor: Color = GamingBorder,
    accentColor: Color = NeonRedPrimary,
    backgroundColor: Color = GamingSurface,
    showCornerAccents: Boolean = true,
    testTag: String = "cyber_card",
    content: @Composable BoxScope.() -> Unit
) {
    val shape = remember(chamferSize) {
        ChamferedShape(
            chamferSize = chamferSize,
            chamferTopLeft = true,
            chamferTopRight = true,
            chamferBottomRight = true,
            chamferBottomLeft = true
        )
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.85f),
                        GamingBackground
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        borderColor,
                        accentColor.copy(alpha = 0.5f),
                        borderColor
                    )
                ),
                shape = shape
            )
            .drawBehind {
                if (showCornerAccents) {
                    val c = chamferSize.toPx()
                    // Draw red corner notches
                    val strokeW = 2.dp.toPx()
                    // Top Right notch
                    drawLine(
                        color = accentColor,
                        start = Offset(size.width - c * 1.5f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeW
                    )
                    drawLine(
                        color = accentColor,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, c * 1.5f),
                        strokeWidth = strokeW
                    )

                    // Bottom Left notch
                    drawLine(
                        color = accentColor,
                        start = Offset(0f, size.height - c * 1.5f),
                        end = Offset(0f, size.height),
                        strokeWidth = strokeW
                    )
                    drawLine(
                        color = accentColor,
                        start = Offset(0f, size.height),
                        end = Offset(c * 1.5f, size.height),
                        strokeWidth = strokeW
                    )
                }
            },
        content = content
    )
}

/**
 * Animated Pulse / ECG Waveform for the FPS HUD Card.
 */
@Composable
fun PulseWaveCanvas(
    modifier: Modifier = Modifier,
    color: Color = NeonGreenAccent
) {
    val transition = rememberInfiniteTransition(label = "pulse_trans")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h * 0.6f

        val path = Path()
        path.moveTo(0f, midY)

        val cycleWidth = w * 0.8f
        val offset = phase * cycleWidth

        var x = 0f
        while (x <= w) {
            val localX = (x + offset) % cycleWidth
            val yOffset = when {
                localX in (cycleWidth * 0.35f)..(cycleWidth * 0.40f) -> -h * 0.25f
                localX in (cycleWidth * 0.40f)..(cycleWidth * 0.45f) -> h * 0.35f
                localX in (cycleWidth * 0.45f)..(cycleWidth * 0.50f) -> -h * 0.5f
                localX in (cycleWidth * 0.50f)..(cycleWidth * 0.55f) -> h * 0.2f
                else -> 0f
            }
            if (x == 0f) path.moveTo(x, midY + yOffset) else path.lineTo(x, midY + yOffset)
            x += 4f
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Custom sci-fi switch matching the toggle in the screenshot.
 */
@Composable
fun CyberSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "floating_panel_switch"
) {
    val trackWidth = 52.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp

    Box(
        modifier = modifier
            .testTag(testTag)
            .width(trackWidth)
            .height(trackHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (checked) NeonGreenAccent.copy(alpha = 0.3f) else Color(0xFF22242B)
            )
            .border(
                width = 1.5.dp,
                color = if (checked) NeonGreenAccent else Color(0xFF4A4E5C),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 20.dp, color = NeonGreenAccent)
            ) {
                onCheckedChange(!checked)
            }
            .padding(horizontal = 3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    if (checked) NeonGreenAccent else Color(0xFFD6D7DC)
                )
                .border(
                    width = 1.dp,
                    color = if (checked) Color.White else Color(0xFF888A96),
                    shape = CircleShape
                )
        )
    }
}
