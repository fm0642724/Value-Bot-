package com.aistudio.gameboostai.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import kotlin.math.hypot

/**
 * Draggable Circular Red Neon Floating Button with 8px tap-vs-drag threshold,
 * edge-snapping animation, and screen fraction position persistence.
 */
@SuppressLint("ClickableViewAccessibility")
class FloatingButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onButtonTapped: (() -> Unit)? = null
    var onPositionChanged: ((fractionX: Float, fractionY: Float) -> Unit)? = null

    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var screenWidth = 1080
    private var screenHeight = 2400

    private var touchStartX = 0f
    private var touchStartY = 0f
    private var initialX = 0
    private var initialY = 0
    private var isDragging = false
    private val thresholdPx = 8f * resources.displayMetrics.density

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * resources.displayMetrics.density
        color = Color.parseColor("#FF1E3C")
    }
    private val reticlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#FF1E3C")
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF1E3C")
    }

    fun attachToWindow(
        wm: WindowManager,
        params: WindowManager.LayoutParams,
        screenWidth: Int,
        screenHeight: Int
    ) {
        this.windowManager = wm
        this.layoutParams = params
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }

    fun updateScreenDimensions(width: Int, height: Int) {
        this.screenWidth = width
        this.screenHeight = height
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val radius = w / 2f
        bgPaint.shader = RadialGradient(
            radius, radius, radius,
            intArrayOf(Color.parseColor("#3B070F"), Color.parseColor("#120205")),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f
        val r = (width / 2f) - (2f * density)

        // Dark gradient background circle
        canvas.drawCircle(cx, cy, r, bgPaint)
        // Neon Red Outer Border
        canvas.drawCircle(cx, cy, r, borderPaint)

        // Reticle
        reticlePaint.strokeWidth = 2f * density
        val outerR = r * 0.65f
        val innerR = r * 0.32f
        canvas.drawCircle(cx, cy, outerR, reticlePaint)
        canvas.drawCircle(cx, cy, innerR, reticlePaint)
        canvas.drawCircle(cx, cy, 2.5f * density, fillPaint)

        val tickLen = r * 0.22f
        val tickGap = innerR * 0.8f
        canvas.drawLine(cx, cy - tickGap, cx, cy - outerR - tickLen, reticlePaint)
        canvas.drawLine(cx, cy + tickGap, cx, cy + outerR + tickLen, reticlePaint)
        canvas.drawLine(cx - tickGap, cy, cx - outerR - tickLen, cy, reticlePaint)
        canvas.drawLine(cx + tickGap, cy, cx + outerR + tickLen, cy, reticlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!::windowManager.isInitialized) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.rawX
                touchStartY = event.rawY
                initialX = layoutParams.x
                initialY = layoutParams.y
                isDragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - touchStartX
                val dy = event.rawY - touchStartY
                val dist = hypot(dx, dy)

                if (!isDragging && dist >= thresholdPx) {
                    isDragging = true
                }

                if (isDragging) {
                    layoutParams.x = (initialX + dx.toInt()).coerceIn(0, screenWidth - width)
                    layoutParams.y = (initialY + dy.toInt()).coerceIn(0, screenHeight - height)
                    try {
                        windowManager.updateViewLayout(this, layoutParams)
                    } catch (e: Exception) {}
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.rawX - touchStartX
                val dy = event.rawY - touchStartY
                val dist = hypot(dx, dy)

                if (!isDragging && dist < thresholdPx) {
                    performClick()
                    onButtonTapped?.invoke()
                } else if (isDragging) {
                    snapToNearestEdge()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun snapToNearestEdge() {
        val currentX = layoutParams.x
        val midPoint = (screenWidth - width) / 2
        val targetX = if (currentX < midPoint) 16 else (screenWidth - width - 16)

        val animator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 240
            interpolator = OvershootInterpolator(1.1f)
            addUpdateListener { anim ->
                layoutParams.x = anim.animatedValue as Int
                try {
                    windowManager.updateViewLayout(this@FloatingButtonView, layoutParams)
                } catch (e: Exception) {}
            }
        }
        animator.start()

        val fractionX = targetX.toFloat() / screenWidth.coerceAtLeast(1)
        val fractionY = layoutParams.y.toFloat() / screenHeight.coerceAtLeast(1)
        onPositionChanged?.invoke(fractionX, fractionY)
    }
}
