package com.aistudio.gameboostai.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/**
 * Fullscreen transparent Canvas View with FLAG_NOT_TOUCHABLE.
 * Draws custom targeting crosshair centered or at user fractional coordinates (X%, Y%).
 * Zero touch interception: all taps pass directly through to Free Fire!
 */
class CrosshairOverlayView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF1E3C.toInt()
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 9f
        style = Paint.Style.STROKE
    }

    var posXFraction: Float = 0.5f
    var posYFraction: Float = 0.5f
    var radius: Float = 28f
    var centerGap: Float = 8f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width * posXFraction
        val cy = height * posYFraction

        // Outer Ring outline & stroke
        canvas.drawCircle(cx, cy, radius, outlinePaint)
        canvas.drawCircle(cx, cy, radius, paint)

        // Center dot
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, 4f, paint)
        paint.style = Paint.Style.STROKE

        // 4 cross lines
        val lineLen = 14f
        // Top
        canvas.drawLine(cx, cy - radius - lineLen, cx, cy - radius, paint)
        // Bottom
        canvas.drawLine(cx, cy + radius, cx, cy + radius + lineLen, paint)
        // Left
        canvas.drawLine(cx - radius - lineLen, cy, cx - radius, paint)
        // Right
        canvas.drawLine(cx + radius, cy, cx + radius + lineLen, cy, paint)
    }
}
