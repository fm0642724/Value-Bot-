package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.CrosshairConfig
import com.example.domain.CrosshairStyle

fun parseHexColor(hex: String, alpha: Float = 1f): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16).toInt()
        val r = (colorInt shr 16) and 0xFF
        val g = (colorInt shr 8) and 0xFF
        val b = colorInt and 0xFF
        Color(r, g, b, (alpha * 255).toInt())
    } catch (e: Exception) {
        Color.Red.copy(alpha = alpha)
    }
}

/**
 * High-precision canvas renderer for custom crosshair designs.
 */
@Composable
fun CrosshairCanvas(
    config: CrosshairConfig,
    modifier: Modifier = Modifier
) {
    val crosshairColor = parseHexColor(config.colorHex, config.opacity)
    val outlineColor = parseHexColor(config.outlineColorHex, config.opacity)

    Canvas(modifier = modifier.size(config.size.dp)) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val strokeW = config.thickness.dp.toPx()
        val outlineStrokeW = strokeW + 2.dp.toPx()
        val gap = config.centerGap.dp.toPx()
        val armLen = (w / 2f) - gap

        rotate(degrees = config.rotation, pivot = center) {
            when (config.style) {
                CrosshairStyle.DOT -> {
                    if (config.outline) {
                        drawCircle(outlineColor, radius = strokeW * 1.8f, center = center)
                    }
                    drawCircle(crosshairColor, radius = strokeW * 1.2f, center = center)
                }

                CrosshairStyle.PLUS -> {
                    drawCrosshairArms(center, gap, armLen, strokeW, outlineStrokeW, config.outline, crosshairColor, outlineColor)
                }

                CrosshairStyle.THIN_CROSS -> {
                    val thinW = (strokeW * 0.65f).coerceAtLeast(1.5f)
                    drawCrosshairArms(center, 0f, w / 2f, thinW, thinW + 2f, config.outline, crosshairColor, outlineColor)
                }

                CrosshairStyle.GAP_CROSS -> {
                    val wideGap = gap.coerceAtLeast(10.dp.toPx())
                    drawCrosshairArms(center, wideGap, armLen, strokeW, outlineStrokeW, config.outline, crosshairColor, outlineColor)
                }

                CrosshairStyle.CIRCLE -> {
                    val r = (w * 0.35f).coerceAtLeast(strokeW)
                    if (config.outline) {
                        drawCircle(outlineColor, radius = r, center = center, style = Stroke(outlineStrokeW))
                    }
                    drawCircle(crosshairColor, radius = r, center = center, style = Stroke(strokeW))
                }

                CrosshairStyle.CIRCLE_DOT -> {
                    val r = (w * 0.38f).coerceAtLeast(strokeW)
                    if (config.outline) {
                        drawCircle(outlineColor, radius = r, center = center, style = Stroke(outlineStrokeW))
                        drawCircle(outlineColor, radius = strokeW * 1.5f, center = center)
                    }
                    drawCircle(crosshairColor, radius = r, center = center, style = Stroke(strokeW))
                    drawCircle(crosshairColor, radius = strokeW * 1.0f, center = center)
                }

                CrosshairStyle.T_SHAPE -> {
                    // Left, Right, Bottom arms (no top arm)
                    if (config.outline) {
                        drawLine(outlineColor, Offset(center.x - gap, center.y), Offset(center.x - gap - armLen, center.y), outlineStrokeW, StrokeCap.Square)
                        drawLine(outlineColor, Offset(center.x + gap, center.y), Offset(center.x + gap + armLen, center.y), outlineStrokeW, StrokeCap.Square)
                        drawLine(outlineColor, Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + armLen), outlineStrokeW, StrokeCap.Square)
                    }
                    drawLine(crosshairColor, Offset(center.x - gap, center.y), Offset(center.x - gap - armLen, center.y), strokeW, StrokeCap.Square)
                    drawLine(crosshairColor, Offset(center.x + gap, center.y), Offset(center.x + gap + armLen, center.y), strokeW, StrokeCap.Square)
                    drawLine(crosshairColor, Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + armLen), strokeW, StrokeCap.Square)
                }

                CrosshairStyle.X_SHAPE -> {
                    val dGap = gap * 0.707f
                    val dArm = armLen * 0.707f
                    val dirs = listOf(Pair(-1f, -1f), Pair(1f, -1f), Pair(-1f, 1f), Pair(1f, 1f))
                    dirs.forEach { (dx, dy) ->
                        val start = Offset(center.x + dx * dGap, center.y + dy * dGap)
                        val end = Offset(center.x + dx * (dGap + dArm), center.y + dy * (dGap + dArm))
                        if (config.outline) drawLine(outlineColor, start, end, outlineStrokeW, StrokeCap.Square)
                        drawLine(crosshairColor, start, end, strokeW, StrokeCap.Square)
                    }
                }

                CrosshairStyle.SQUARE -> {
                    val half = w * 0.28f
                    val rect = Rect(center.x - half, center.y - half, center.x + half, center.y + half)
                    if (config.outline) {
                        drawRect(outlineColor, rect.topLeft, rect.size, style = Stroke(outlineStrokeW))
                    }
                    drawRect(crosshairColor, rect.topLeft, rect.size, style = Stroke(strokeW))
                }

                CrosshairStyle.TRIANGLE -> {
                    val path = Path().apply {
                        moveTo(center.x, center.y - w * 0.36f)
                        lineTo(center.x + w * 0.32f, center.y + w * 0.28f)
                        lineTo(center.x - w * 0.32f, center.y + w * 0.28f)
                        close()
                    }
                    if (config.outline) {
                        drawPath(path, outlineColor, style = Stroke(outlineStrokeW))
                    }
                    drawPath(path, crosshairColor, style = Stroke(strokeW))
                }

                CrosshairStyle.DOUBLE_RING -> {
                    val r1 = w * 0.40f
                    val r2 = w * 0.20f
                    if (config.outline) {
                        drawCircle(outlineColor, radius = r1, center = center, style = Stroke(outlineStrokeW))
                        drawCircle(outlineColor, radius = r2, center = center, style = Stroke(outlineStrokeW))
                    }
                    drawCircle(crosshairColor, radius = r1, center = center, style = Stroke(strokeW))
                    drawCircle(crosshairColor, radius = r2, center = center, style = Stroke(strokeW))
                }

                CrosshairStyle.SNIPER_CROSS -> {
                    val outerR = w * 0.42f
                    if (config.outline) {
                        drawCircle(outlineColor, radius = outerR, center = center, style = Stroke(outlineStrokeW))
                    }
                    drawCircle(crosshairColor, radius = outerR, center = center, style = Stroke(strokeW))
                    drawCrosshairArms(center, 0f, outerR, strokeW * 0.8f, outlineStrokeW * 0.8f, config.outline, crosshairColor, outlineColor)
                    drawCircle(crosshairColor, radius = strokeW * 1.1f, center = center)
                }
            }
        }
    }
}

private fun DrawScope.drawCrosshairArms(
    center: Offset,
    gap: Float,
    armLen: Float,
    strokeW: Float,
    outlineW: Float,
    hasOutline: Boolean,
    color: Color,
    outColor: Color
) {
    val leftStart = Offset(center.x - gap, center.y)
    val leftEnd = Offset(center.x - gap - armLen, center.y)
    val rightStart = Offset(center.x + gap, center.y)
    val rightEnd = Offset(center.x + gap + armLen, center.y)
    val topStart = Offset(center.x, center.y - gap)
    val topEnd = Offset(center.x, center.y - gap - armLen)
    val botStart = Offset(center.x, center.y + gap)
    val botEnd = Offset(center.x, center.y + gap + armLen)

    if (hasOutline) {
        drawLine(outColor, leftStart, leftEnd, outlineW, StrokeCap.Square)
        drawLine(outColor, rightStart, rightEnd, outlineW, StrokeCap.Square)
        drawLine(outColor, topStart, topEnd, outlineW, StrokeCap.Square)
        drawLine(outColor, botStart, botEnd, outlineW, StrokeCap.Square)
    }

    drawLine(color, leftStart, leftEnd, strokeW, StrokeCap.Square)
    drawLine(color, rightStart, rightEnd, strokeW, StrokeCap.Square)
    drawLine(color, topStart, topEnd, strokeW, StrokeCap.Square)
    drawLine(color, botStart, botEnd, strokeW, StrokeCap.Square)
}
