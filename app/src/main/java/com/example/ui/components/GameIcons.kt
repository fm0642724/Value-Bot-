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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedPrimary

/**
 * Large Crosshair Target Reticle icon for "AIM BOOST" hero button.
 */
@Composable
fun AimReticleIcon(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val outerRadius = this.size.width * 0.44f
        val innerRadius = this.size.width * 0.22f
        val strokeW = 3.dp.toPx()

        // Outer ring
        drawCircle(
            color = color,
            radius = outerRadius,
            center = center,
            style = Stroke(width = strokeW)
        )

        // Inner ring
        drawCircle(
            color = color,
            radius = innerRadius,
            center = center,
            style = Stroke(width = strokeW * 0.8f)
        )

        // Center dot
        drawCircle(
            color = color,
            radius = strokeW * 1.2f,
            center = center
        )

        // 4 Cross lines extending outward
        val lineLen = this.size.width * 0.16f
        val gap = innerRadius * 0.7f

        // Top
        drawLine(
            color = color,
            start = Offset(center.x, center.y - gap),
            end = Offset(center.x, center.y - outerRadius - lineLen * 0.5f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        // Bottom
        drawLine(
            color = color,
            start = Offset(center.x, center.y + gap),
            end = Offset(center.x, center.y + outerRadius + lineLen * 0.5f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        // Left
        drawLine(
            color = color,
            start = Offset(center.x - gap, center.y),
            end = Offset(center.x - outerRadius - lineLen * 0.5f, center.y),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        // Right
        drawLine(
            color = color,
            start = Offset(center.x + gap, center.y),
            end = Offset(center.x + outerRadius + lineLen * 0.5f, center.y),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Digital Brain / AI Head outline icon for "SMART SENSITIVITY".
 */
@Composable
fun SmartBrainIcon(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 2.dp.toPx()

        // Head profile and brain lobes
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.85f)
            lineTo(w * 0.40f, h * 0.85f)
            lineTo(w * 0.42f, h * 0.75f)
            lineTo(w * 0.55f, h * 0.72f)
            lineTo(w * 0.60f, h * 0.60f)
            // Nose & mouth
            lineTo(w * 0.75f, h * 0.58f)
            lineTo(w * 0.68f, h * 0.48f)
            lineTo(w * 0.76f, h * 0.40f)
            // Forehead
            lineTo(w * 0.68f, h * 0.24f)
            // Cranium curve
            cubicTo(
                w * 0.60f, h * 0.12f,
                w * 0.35f, h * 0.12f,
                w * 0.22f, h * 0.30f
            )
            // Back of head
            cubicTo(
                w * 0.15f, h * 0.45f,
                w * 0.15f, h * 0.70f,
                w * 0.25f, h * 0.85f
            )
            close()
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Internal digital brain circuitry lines
        drawLine(
            color = color,
            start = Offset(w * 0.35f, h * 0.32f),
            end = Offset(w * 0.48f, h * 0.32f),
            strokeWidth = strokeW
        )
        drawLine(
            color = color,
            start = Offset(w * 0.48f, h * 0.32f),
            end = Offset(w * 0.55f, h * 0.42f),
            strokeWidth = strokeW
        )
        drawLine(
            color = color,
            start = Offset(w * 0.30f, h * 0.45f),
            end = Offset(w * 0.42f, h * 0.45f),
            strokeWidth = strokeW
        )
        drawLine(
            color = color,
            start = Offset(w * 0.35f, h * 0.58f),
            end = Offset(w * 0.48f, h * 0.58f),
            strokeWidth = strokeW
        )
        // Circuit nodes
        drawCircle(color = color, radius = strokeW * 0.9f, center = Offset(w * 0.48f, h * 0.32f))
        drawCircle(color = color, radius = strokeW * 0.9f, center = Offset(w * 0.55f, h * 0.42f))
        drawCircle(color = color, radius = strokeW * 0.9f, center = Offset(w * 0.42f, h * 0.45f))
    }
}

/**
 * Microchip / CPU Analyzer icon for "DEVICE ANALYZER".
 */
@Composable
fun DeviceChipIcon(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 2.dp.toPx()

        // Outer square frame with corner brackets
        val pad = w * 0.20f
        val rect = Rect(pad, pad, w - pad, h - pad)

        // Corner brackets of chip
        val cLen = w * 0.16f
        // Top-left
        drawLine(color, Offset(rect.left, rect.top + cLen), Offset(rect.left, rect.top), strokeW)
        drawLine(color, Offset(rect.left, rect.top), Offset(rect.left + cLen, rect.top), strokeW)
        // Top-right
        drawLine(color, Offset(rect.right - cLen, rect.top), Offset(rect.right, rect.top), strokeW)
        drawLine(color, Offset(rect.right, rect.top), Offset(rect.right, rect.top + cLen), strokeW)
        // Bottom-left
        drawLine(color, Offset(rect.left, rect.bottom - cLen), Offset(rect.left, rect.bottom), strokeW)
        drawLine(color, Offset(rect.left, rect.bottom), Offset(rect.left + cLen, rect.bottom), strokeW)
        // Bottom-right
        drawLine(color, Offset(rect.right - cLen, rect.bottom), Offset(rect.right, rect.bottom), strokeW)
        drawLine(color, Offset(rect.right, rect.bottom), Offset(rect.right, rect.bottom - cLen), strokeW)

        // Center reticle / core
        val center = Offset(w / 2f, h / 2f)
        val coreR = w * 0.12f
        drawCircle(color, radius = coreR, center = center, style = Stroke(strokeW))
        drawCircle(color, radius = strokeW * 0.9f, center = center)

        // Pins extending from top, bottom, left, right
        val pinLen = w * 0.14f
        for (i in -1..1) {
            val offset = i * (w * 0.15f)
            // Top pins
            drawLine(color, Offset(center.x + offset, rect.top), Offset(center.x + offset, rect.top - pinLen), strokeW)
            // Bottom pins
            drawLine(color, Offset(center.x + offset, rect.bottom), Offset(center.x + offset, rect.bottom + pinLen), strokeW)
            // Left pins
            drawLine(color, Offset(rect.left, center.y + offset), Offset(rect.left - pinLen, center.y + offset), strokeW)
            // Right pins
            drawLine(color, Offset(rect.right, center.y + offset), Offset(rect.right + pinLen, center.y + offset), strokeW)
        }
    }
}

/**
 * Floating Panel / PIP Tiles icon for "FLOATING PANEL".
 */
@Composable
fun FloatingPanelIcon(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 2.dp.toPx()

        // Background panel
        val p1W = w * 0.55f
        val p1H = h * 0.42f
        val p1X = w * 0.15f
        val p1Y = h * 0.18f

        drawRoundRect(
            color = color.copy(alpha = 0.5f),
            topLeft = Offset(p1X, p1Y),
            size = Size(p1W, p1H),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = Stroke(strokeW)
        )

        // Foreground overlapping panel
        val p2W = w * 0.60f
        val p2H = h * 0.46f
        val p2X = w * 0.28f
        val p2Y = h * 0.38f

        drawRoundRect(
            color = color,
            topLeft = Offset(p2X, p2Y),
            size = Size(p2W, p2H),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = Stroke(strokeW)
        )

        // Inner header line and slider toggle inside foreground panel
        drawLine(
            color = color,
            start = Offset(p2X + p2W * 0.18f, p2Y + p2H * 0.35f),
            end = Offset(p2X + p2W * 0.82f, p2Y + p2H * 0.35f),
            strokeWidth = strokeW * 0.8f
        )
        drawCircle(
            color = color,
            radius = strokeW * 1.2f,
            center = Offset(p2X + p2W * 0.40f, p2Y + p2H * 0.35f)
        )

        // Lower indicator
        drawLine(
            color = color,
            start = Offset(p2X + p2W * 0.18f, p2Y + p2H * 0.68f),
            end = Offset(p2X + p2W * 0.60f, p2Y + p2H * 0.68f),
            strokeWidth = strokeW * 0.8f
        )
    }
}

/**
 * Performance Monitor / Ascending Bar Chart icon for "PERFORMANCE MONITOR".
 */
@Composable
fun PerformanceChartIcon(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 2.dp.toPx()

        // 3 Vertical Ascending Bars (filled with subtle gradient or stroke)
        val barW = w * 0.16f
        val baseY = h * 0.85f

        // Bar 1 (Short)
        val b1H = h * 0.30f
        drawRect(
            color = color,
            topLeft = Offset(w * 0.18f, baseY - b1H),
            size = Size(barW, b1H)
        )

        // Bar 2 (Medium)
        val b2H = h * 0.48f
        drawRect(
            color = color,
            topLeft = Offset(w * 0.42f, baseY - b2H),
            size = Size(barW, b2H)
        )

        // Bar 3 (Tall)
        val b3H = h * 0.68f
        drawRect(
            color = color,
            topLeft = Offset(w * 0.66f, baseY - b3H),
            size = Size(barW, b3H)
        )

        // Upward Trend Arrow
        val arrowPath = Path().apply {
            moveTo(w * 0.18f, baseY - b1H * 0.7f)
            lineTo(w * 0.46f, baseY - b2H * 0.9f)
            lineTo(w * 0.74f, baseY - b3H * 0.95f)
            lineTo(w * 0.88f, h * 0.18f)
        }
        drawPath(
            path = arrowPath,
            color = color,
            style = Stroke(width = strokeW * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Arrowhead
        val headPath = Path().apply {
            moveTo(w * 0.70f, h * 0.18f)
            lineTo(w * 0.88f, h * 0.18f)
            lineTo(w * 0.88f, h * 0.36f)
        }
        drawPath(
            path = headPath,
            color = color,
            style = Stroke(width = strokeW * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Smartphone Device Icon for the left HUD card.
 */
@Composable
fun PhoneStatusIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = NeonGreenAccent
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 2.dp.toPx()

        val phW = w * 0.62f
        val phH = h * 0.88f
        val phX = (w - phW) / 2f
        val phY = (h - phH) / 2f

        drawRoundRect(
            color = color,
            topLeft = Offset(phX, phY),
            size = Size(phW, phH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = Stroke(strokeW)
        )

        // Screen area
        drawRoundRect(
            color = color.copy(alpha = 0.25f),
            topLeft = Offset(phX + phW * 0.12f, phY + phH * 0.14f),
            size = Size(phW * 0.76f, phH * 0.68f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )

        // Top speaker notch
        drawLine(
            color = color,
            start = Offset(phX + phW * 0.38f, phY + phH * 0.07f),
            end = Offset(phX + phW * 0.62f, phY + phH * 0.07f),
            strokeWidth = strokeW * 0.8f
        )
    }
}

/**
 * FPS Speedometer Gauge icon for the right HUD card.
 */
@Composable
fun FpsGaugeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 2.dp.toPx()
        val center = Offset(w / 2f, h * 0.60f)
        val radius = w * 0.42f

        // Gauge Arc (from 140 to 40 degrees)
        drawArc(
            color = color,
            startAngle = 150f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )

        // Pointer Needle
        val needleLen = radius * 0.75f
        val needleAngle = Math.toRadians(310.0)
        val endX = (center.x + needleLen * Math.cos(needleAngle)).toFloat()
        val endY = (center.y + needleLen * Math.sin(needleAngle)).toFloat()

        drawLine(
            color = color,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = strokeW * 1.1f,
            cap = StrokeCap.Round
        )

        // Center pivot
        drawCircle(color = color, radius = strokeW * 1.3f, center = center)
    }
}

/**
 * Neon Lightning Bolt Icon for "OPTIMIZE PHONE".
 */
@Composable
fun LightningBoltIcon(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.58f, h * 0.08f)
            lineTo(w * 0.22f, h * 0.54f)
            lineTo(w * 0.52f, h * 0.54f)
            lineTo(w * 0.38f, h * 0.92f)
            lineTo(w * 0.82f, h * 0.44f)
            lineTo(w * 0.52f, h * 0.44f)
            close()
        }

        drawPath(
            path = path,
            color = color
        )
    }
}

/**
 * Crosshair Editor Target Icon for "CROSSHAIR EDITOR".
 */
@Composable
fun CrosshairEditorIcon(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val strokeW = 2.dp.toPx()

        // Outer ring
        drawCircle(
            color = color,
            radius = w * 0.40f,
            center = center,
            style = Stroke(strokeW)
        )

        // 4 crosshairs
        drawLine(
            color = color,
            start = Offset(center.x, h * 0.08f),
            end = Offset(center.x, h * 0.32f),
            strokeWidth = strokeW
        )
        drawLine(
            color = color,
            start = Offset(center.x, h * 0.68f),
            end = Offset(center.x, h * 0.92f),
            strokeWidth = strokeW
        )
        drawLine(
            color = color,
            start = Offset(w * 0.08f, center.y),
            end = Offset(w * 0.32f, center.y),
            strokeWidth = strokeW
        )
        drawLine(
            color = color,
            start = Offset(w * 0.68f, center.y),
            end = Offset(w * 0.92f, center.y),
            strokeWidth = strokeW
        )

        // Center dot
        drawCircle(
            color = color,
            radius = strokeW * 1.2f,
            center = center
        )
    }
}

/**
 * VIP Crown Icon.
 */
@Composable
fun VipCrownIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 1.8.dp.toPx()

        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.78f)
            lineTo(w * 0.85f, h * 0.78f)
            lineTo(w * 0.90f, h * 0.32f)
            lineTo(w * 0.68f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.22f)
            lineTo(w * 0.32f, h * 0.52f)
            lineTo(w * 0.10f, h * 0.32f)
            close()
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeW,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Crown tip jewels
        drawCircle(color = color, radius = strokeW * 0.9f, center = Offset(w * 0.10f, h * 0.32f))
        drawCircle(color = color, radius = strokeW * 0.9f, center = Offset(w * 0.50f, h * 0.22f))
        drawCircle(color = color, radius = strokeW * 0.9f, center = Offset(w * 0.90f, h * 0.32f))
    }
}

/**
 * Phone Boost Icon with rocket / broom for "PHONE BOOST".
 */
@Composable
fun PhoneRocketBoostIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 1.8.dp.toPx()

        // Phone outline tilted
        val pw = w * 0.50f
        val ph = h * 0.78f
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.10f, h * 0.12f),
            size = Size(pw, ph),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = Stroke(strokeW)
        )
        // Screen
        drawRoundRect(
            color = color.copy(alpha = 0.2f),
            topLeft = Offset(w * 0.16f, h * 0.20f),
            size = Size(pw * 0.76f, ph * 0.65f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )

        // Rocket booster launching out from bottom right
        val rPath = Path().apply {
            moveTo(w * 0.75f, h * 0.25f)
            lineTo(w * 0.88f, h * 0.55f)
            lineTo(w * 0.78f, h * 0.62f)
            lineTo(w * 0.68f, h * 0.85f)
            lineTo(w * 0.58f, h * 0.70f)
            lineTo(w * 0.50f, h * 0.72f)
            lineTo(w * 0.56f, h * 0.50f)
            close()
        }
        drawPath(
            path = rPath,
            color = color,
            style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Booster flame
        drawLine(color, Offset(w * 0.60f, h * 0.78f), Offset(w * 0.64f, h * 0.94f), strokeW * 1.2f)
        drawLine(color, Offset(w * 0.66f, h * 0.82f), Offset(w * 0.72f, h * 0.96f), strokeW)
    }
}

/**
 * FPS Stabilizer Gauge Icon with green up-arrow.
 */
@Composable
fun FpsStabilizerGaugeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = NeonRedPrimary,
    accentColor: Color = NeonGreenAccent
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 1.8.dp.toPx()
        val center = Offset(w * 0.42f, h * 0.62f)
        val radius = w * 0.36f

        // Speedometer Arc
        drawArc(
            color = color,
            startAngle = 150f,
            sweepAngle = 210f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )

        // Pointer Needle
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x + radius * 0.55f, center.y - radius * 0.55f),
            strokeWidth = strokeW * 1.2f,
            cap = StrokeCap.Round
        )
        drawCircle(color = color, radius = strokeW * 1.3f, center = center)

        // Green Boost Up Arrow
        val arrowPath = Path().apply {
            moveTo(w * 0.76f, h * 0.18f)
            lineTo(w * 0.88f, h * 0.36f)
            lineTo(w * 0.81f, h * 0.36f)
            lineTo(w * 0.81f, h * 0.65f)
            lineTo(w * 0.71f, h * 0.65f)
            lineTo(w * 0.71f, h * 0.36f)
            lineTo(w * 0.64f, h * 0.36f)
            close()
        }
        drawPath(path = arrowPath, color = accentColor)
    }
}

/**
 * Radio Ping Radar Tower Icon for "PING FIX".
 */
@Composable
fun PingRadarTowerIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 1.8.dp.toPx()

        // Tower Legs (A-frame)
        val top = Offset(w * 0.50f, h * 0.38f)
        drawLine(color, top, Offset(w * 0.28f, h * 0.90f), strokeW, cap = StrokeCap.Round)
        drawLine(color, top, Offset(w * 0.72f, h * 0.90f), strokeW, cap = StrokeCap.Round)
        // Crossbars
        drawLine(color, Offset(w * 0.36f, h * 0.65f), Offset(w * 0.64f, h * 0.65f), strokeW * 0.9f)
        drawLine(color, Offset(w * 0.31f, h * 0.80f), Offset(w * 0.69f, h * 0.80f), strokeW * 0.9f)
        // Transmitter Tip
        drawCircle(color, radius = strokeW * 1.3f, center = top)
        drawLine(color, top, Offset(top.x, h * 0.26f), strokeW, cap = StrokeCap.Round)

        // Left Arc waves
        drawArc(
            color = color,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.12f, h * 0.08f),
            size = Size(w * 0.40f, h * 0.40f),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )
        // Right Arc waves
        drawArc(
            color = color,
            startAngle = 315f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.48f, h * 0.08f),
            size = Size(w * 0.40f, h * 0.40f),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )
    }
}

/**
 * Weapon Stability / Sniper Crosshair Icon for "WEAPON STABILITY".
 */
@Composable
fun WeaponSniperStabilityIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = NeonRedPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 1.8.dp.toPx()
        val center = Offset(w / 2f, h / 2f)

        // Outer reticle circle
        drawCircle(color, radius = w * 0.38f, center = center, style = Stroke(strokeW))
        // Cross lines
        drawLine(color, Offset(center.x, h * 0.12f), Offset(center.x, h * 0.32f), strokeW)
        drawLine(color, Offset(center.x, h * 0.68f), Offset(center.x, h * 0.88f), strokeW)
        drawLine(color, Offset(w * 0.12f, center.y), Offset(w * 0.32f, center.y), strokeW)
        drawLine(color, Offset(w * 0.68f, center.y), Offset(w * 0.88f, center.y), strokeW)

        // Diagonal Sniper Rifle Silhouette across the reticle
        val riflePath = Path().apply {
            moveTo(w * 0.18f, h * 0.82f)
            lineTo(w * 0.32f, h * 0.68f)
            lineTo(w * 0.45f, h * 0.55f)
            lineTo(w * 0.78f, h * 0.22f)
            lineTo(w * 0.85f, h * 0.15f)
            lineTo(w * 0.88f, h * 0.18f)
            lineTo(w * 0.55f, h * 0.52f)
            lineTo(w * 0.48f, h * 0.65f)
            lineTo(w * 0.32f, h * 0.82f)
            close()
        }
        drawPath(path = riflePath, color = color)
        // Scope top knob
        drawCircle(color, radius = strokeW * 1.2f, center = center)
    }
}

