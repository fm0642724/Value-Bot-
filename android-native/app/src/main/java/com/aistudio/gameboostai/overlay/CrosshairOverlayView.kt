package com.aistudio.gameboostai.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.aistudio.gameboostai.data.CrosshairConfig
import com.aistudio.gameboostai.data.CrosshairStyle

/**
 * Transparent Fullscreen Hardware-Accelerated Canvas View.
 * Displayed in its own window with FLAG_NOT_TOUCHABLE so all touches pass
 * through directly to Free Fire without any touch interception or latency.
 */
class CrosshairOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var config: CrosshairConfig = CrosshairConfig()

    private val primaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    fun updateConfig(newConfig: CrosshairConfig) {
        this.config = newConfig
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!config.enabled) return

        val density = resources.displayMetrics.density
        val centerX = width * (config.posXPercent / 100f)
        val centerY = height * (config.posYPercent / 100f)

        val sizePx = config.size * density
        val strokePx = config.thickness * density
        val gapPx = config.centerGap * density
        val radiusPx = sizePx / 2f

        val baseColor = try { Color.parseColor(config.colorHex) } catch (e: Exception) { Color.RED }
        val alphaInt = (config.opacity * 255).toInt().coerceIn(0, 255)
        val mainColorWithAlpha = Color.argb(alphaInt, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))

        val outlineColor = try { Color.parseColor(config.outlineColorHex) } catch (e: Exception) { Color.BLACK }
        val outlineColorWithAlpha = Color.argb(alphaInt, Color.red(outlineColor), Color.green(outlineColor), Color.blue(outlineColor))

        primaryPaint.color = mainColorWithAlpha
        primaryPaint.strokeWidth = strokePx
        fillPaint.color = mainColorWithAlpha
        outlinePaint.color = outlineColorWithAlpha
        outlinePaint.strokeWidth = strokePx + (1.5f * density)

        canvas.save()
        if (config.rotation != 0f) {
            canvas.rotate(config.rotation, centerX, centerY)
        }

        when (config.style) {
            CrosshairStyle.DOT -> {
                if (config.outline) canvas.drawCircle(centerX, centerY, strokePx * 1.5f + 1f * density, outlinePaint)
                canvas.drawCircle(centerX, centerY, strokePx * 1.5f, fillPaint)
            }
            CrosshairStyle.PLUS -> {
                drawCross(canvas, centerX, centerY, radiusPx, 0f, strokePx, config.outline)
            }
            CrosshairStyle.THIN_CROSS -> {
                drawCross(canvas, centerX, centerY, radiusPx, 0f, strokePx * 0.7f, config.outline)
            }
            CrosshairStyle.GAP_CROSS -> {
                drawCross(canvas, centerX, centerY, radiusPx, gapPx, strokePx, config.outline)
            }
            CrosshairStyle.CIRCLE -> {
                if (config.outline) canvas.drawCircle(centerX, centerY, radiusPx, outlinePaint)
                canvas.drawCircle(centerX, centerY, radiusPx, primaryPaint)
            }
            CrosshairStyle.CIRCLE_DOT -> {
                if (config.outline) {
                    canvas.drawCircle(centerX, centerY, radiusPx, outlinePaint)
                    canvas.drawCircle(centerX, centerY, strokePx * 1.2f + 1f * density, outlinePaint)
                }
                canvas.drawCircle(centerX, centerY, radiusPx, primaryPaint)
                canvas.drawCircle(centerX, centerY, strokePx * 1.2f, fillPaint)
            }
            CrosshairStyle.T_SHAPE -> {
                drawTShape(canvas, centerX, centerY, radiusPx, gapPx, strokePx, config.outline)
            }
            CrosshairStyle.X_SHAPE -> {
                canvas.save()
                canvas.rotate(45f, centerX, centerY)
                drawCross(canvas, centerX, centerY, radiusPx, gapPx, strokePx, config.outline)
                canvas.restore()
            }
            CrosshairStyle.SQUARE -> {
                val half = radiusPx * 0.7f
                if (config.outline) canvas.drawRect(centerX - half, centerY - half, centerX + half, centerY + half, outlinePaint)
                canvas.drawRect(centerX - half, centerY - half, centerX + half, centerY + half, primaryPaint)
            }
            CrosshairStyle.TRIANGLE -> {
                drawTriangle(canvas, centerX, centerY, radiusPx, strokePx, config.outline)
            }
            CrosshairStyle.DOUBLE_RING -> {
                if (config.outline) {
                    canvas.drawCircle(centerX, centerY, radiusPx, outlinePaint)
                    canvas.drawCircle(centerX, centerY, radiusPx * 0.5f, outlinePaint)
                }
                canvas.drawCircle(centerX, centerY, radiusPx, primaryPaint)
                canvas.drawCircle(centerX, centerY, radiusPx * 0.5f, primaryPaint)
                canvas.drawCircle(centerX, centerY, strokePx, fillPaint)
            }
            CrosshairStyle.SNIPER_CROSS -> {
                drawSniperCross(canvas, centerX, centerY, radiusPx, strokePx, config.outline)
            }
        }

        canvas.restore()
    }

    private fun drawCross(canvas: Canvas, cx: Float, cy: Float, radius: Float, gap: Float, strokeWidth: Float, hasOutline: Boolean) {
        val paintToUse = primaryPaint.apply { this.strokeWidth = strokeWidth }
        if (hasOutline) {
            outlinePaint.strokeWidth = strokeWidth + 2f * resources.displayMetrics.density
            canvas.drawLine(cx, cy - gap, cx, cy - radius, outlinePaint)
            canvas.drawLine(cx, cy + gap, cx, cy + radius, outlinePaint)
            canvas.drawLine(cx - gap, cy, cx - radius, cy, outlinePaint)
            canvas.drawLine(cx + gap, cy, cx + radius, cy, outlinePaint)
        }
        canvas.drawLine(cx, cy - gap, cx, cy - radius, paintToUse)
        canvas.drawLine(cx, cy + gap, cx, cy + radius, paintToUse)
        canvas.drawLine(cx - gap, cy, cx - radius, cy, paintToUse)
        canvas.drawLine(cx + gap, cy, cx + radius, cy, paintToUse)
    }

    private fun drawTShape(canvas: Canvas, cx: Float, cy: Float, radius: Float, gap: Float, strokeWidth: Float, hasOutline: Boolean) {
        val paintToUse = primaryPaint.apply { this.strokeWidth = strokeWidth }
        if (hasOutline) {
            outlinePaint.strokeWidth = strokeWidth + 2f * resources.displayMetrics.density
            canvas.drawLine(cx, cy + gap, cx, cy + radius, outlinePaint)
            canvas.drawLine(cx - gap, cy, cx - radius, cy, outlinePaint)
            canvas.drawLine(cx + gap, cy, cx + radius, cy, outlinePaint)
        }
        canvas.drawLine(cx, cy + gap, cx, cy + radius, paintToUse)
        canvas.drawLine(cx - gap, cy, cx - radius, cy, paintToUse)
        canvas.drawLine(cx + gap, cy, cx + radius, cy, paintToUse)
    }

    private fun drawTriangle(canvas: Canvas, cx: Float, cy: Float, radius: Float, strokeWidth: Float, hasOutline: Boolean) {
        val path = Path().apply {
            moveTo(cx, cy - radius)
            lineTo(cx + radius * 0.866f, cy + radius * 0.5f)
            lineTo(cx - radius * 0.866f, cy + radius * 0.5f)
            close()
        }
        if (hasOutline) {
            outlinePaint.strokeWidth = strokeWidth + 2f * resources.displayMetrics.density
            canvas.drawPath(path, outlinePaint)
        }
        canvas.drawPath(path, primaryPaint.apply { this.strokeWidth = strokeWidth })
    }

    private fun drawSniperCross(canvas: Canvas, cx: Float, cy: Float, radius: Float, strokeWidth: Float, hasOutline: Boolean) {
        if (hasOutline) {
            outlinePaint.strokeWidth = strokeWidth + 2f * resources.displayMetrics.density
            canvas.drawCircle(cx, cy, radius * 0.85f, outlinePaint)
            canvas.drawLine(cx, cy - radius, cx, cy + radius, outlinePaint)
            canvas.drawLine(cx - radius, cy, cx + radius, cy, outlinePaint)
        }
        canvas.drawCircle(cx, cy, radius * 0.85f, primaryPaint.apply { this.strokeWidth = strokeWidth })
        canvas.drawLine(cx, cy - radius, cx, cy + radius, primaryPaint)
        canvas.drawLine(cx - radius, cy, cx + radius, cy, primaryPaint)
        canvas.drawCircle(cx, cy, strokeWidth * 1.5f, fillPaint)
    }
}
