package com.aistudio.gameboostai.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.aistudio.gameboostai.data.CrosshairConfig
import com.aistudio.gameboostai.data.CrosshairStyle
import com.aistudio.gameboostai.data.GamingProfile

/**
 * Native Android View for the Floating HUD Control Panel.
 * Contains draggable header, minimize/close buttons, crosshair controls,
 * 6 style selectors, sliders, profile switcher, sensitivity stats, and hardware chips.
 */
@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class ControlPanelView(
    context: Context,
    private val windowManager: WindowManager,
    private val windowParams: WindowManager.LayoutParams,
    private var crosshairConfig: CrosshairConfig,
    private var profiles: List<GamingProfile>,
    private var activeProfileId: String,
    private var batteryPct: Int = 72,
    private var fpsMeasured: Int = 60,
    private var ramFreeMb: Long = 2800L
) : FrameLayout(context) {

    var onCloseClicked: (() -> Unit)? = null
    var onMinimizeClicked: (() -> Unit)? = null
    var onCrosshairToggled: ((enabled: Boolean) -> Unit)? = null
    var onCrosshairStyleSelected: ((style: CrosshairStyle) -> Unit)? = null
    var onCrosshairSizeChanged: ((sizeDp: Float) -> Unit)? = null
    var onCrosshairOpacityChanged: ((opacity: Float) -> Unit)? = null
    var onProfileSelected: ((profileId: String) -> Unit)? = null

    private val density = resources.displayMetrics.density
    private var headerTouchStartX = 0f
    private var headerTouchStartY = 0f
    private var initialPanelX = 0
    private var initialPanelY = 0

    private lateinit var crosshairSwitch: Switch
    private lateinit var sizeValueText: TextView
    private lateinit var opacityValueText: TextView

    init {
        setupView()
    }

    private fun dp(value: Int): Int = (value * density).toInt()

    private fun setupView() {
        val panelBg = GradientDrawable().apply {
            setColor(Color.parseColor("#F20F1118"))
            setStroke(dp(2), Color.parseColor("#FF1E3C"))
            cornerRadius = dp(14).toFloat()
        }
        background = panelBg
        setPadding(dp(12), dp(10), dp(12), dp(12))

        val scroll = ScrollView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            isVerticalScrollBarEnabled = false
        }

        val content = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
        }

        content.addView(createHeaderBar())
        content.addView(createChipsRow())
        content.addView(createCrosshairSwitchRow())
        content.addView(createStylesSection())
        content.addView(createSlidersSection())
        content.addView(createProfileSection())
        content.addView(createSensitivitySection())

        scroll.addView(content)
        addView(scroll)
    }

    private fun createHeaderBar(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            val titleLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                orientation = LinearLayout.VERTICAL
            }

            val titleText = TextView(context).apply {
                text = "GAME BOOST AI"
                setTextColor(Color.parseColor("#FF1E3C"))
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
            }
            val subText = TextView(context).apply {
                text = "FLOATING CONTROL PANEL"
                setTextColor(Color.parseColor("#8A93A6"))
                textSize = 9f
            }
            titleLayout.addView(titleText)
            titleLayout.addView(subText)
            addView(titleLayout)

            // Header dragging handler
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        headerTouchStartX = event.rawX
                        headerTouchStartY = event.rawY
                        initialPanelX = windowParams.x
                        initialPanelY = windowParams.y
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - headerTouchStartX).toInt()
                        val dy = (event.rawY - headerTouchStartY).toInt()
                        windowParams.x = initialPanelX + dx
                        windowParams.y = initialPanelY + dy
                        try {
                            windowManager.updateViewLayout(this@ControlPanelView, windowParams)
                        } catch (e: Exception) {}
                        true
                    }
                    else -> false
                }
            }

            val minimizeBtn = TextView(context).apply {
                text = "—"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(28), dp(28)).apply { rightMargin = dp(4) }
                background = createButtonBg(Color.parseColor("#222736"))
                setOnClickListener { onMinimizeClicked?.invoke() }
            }
            addView(minimizeBtn)

            val closeBtn = TextView(context).apply {
                text = "✕"
                setTextColor(Color.parseColor("#FF1E3C"))
                textSize = 13f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(28), dp(28))
                background = createButtonBg(Color.parseColor("#3B070F"))
                setOnClickListener { onCloseClicked?.invoke() }
            }
            addView(closeBtn)
        }
    }

    private fun createChipsRow(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            addView(createChip("BAT: $batteryPct%", Color.parseColor("#00FF66")))
            addView(createSpacer(dp(6)))
            addView(createChip("FPS: $fpsMeasured", Color.parseColor("#00FF66")))
            addView(createSpacer(dp(6)))
            addView(createChip("RAM: ${ramFreeMb / 1024f}GB", Color.parseColor("#FF1E3C")))
        }
    }

    private fun createChip(label: String, textColor: Int): TextView {
        return TextView(context).apply {
            text = label
            setTextColor(textColor)
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#151824"))
                setStroke(dp(1), Color.parseColor("#2A3144"))
                cornerRadius = dp(6).toFloat()
            }
        }
    }

    private fun createCrosshairSwitchRow(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#181B26"))
                cornerRadius = dp(8).toFloat()
            }

            val label = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "Target Reticle Overlay"
                setTextColor(Color.WHITE)
                textSize = 11.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            addView(label)

            crosshairSwitch = Switch(context).apply {
                isChecked = crosshairConfig.enabled
                setOnCheckedChangeListener { _, isChecked -> onCrosshairToggled?.invoke(isChecked) }
            }
            addView(crosshairSwitch)
        }
    }

    private fun createStylesSection(): LinearLayout {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            orientation = LinearLayout.VERTICAL
        }

        val headerLabel = TextView(context).apply {
            text = "QUICK CROSSHAIR STYLES"
            setTextColor(Color.parseColor("#8A93A6"))
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
        }
        container.addView(headerLabel)

        val styles = listOf(
            CrosshairStyle.DOT to "Dot",
            CrosshairStyle.PLUS to "Plus",
            CrosshairStyle.CIRCLE_DOT to "Circle+",
            CrosshairStyle.GAP_CROSS to "Gap",
            CrosshairStyle.T_SHAPE to "T-Shape",
            CrosshairStyle.SNIPER_CROSS to "Sniper"
        )

        val row = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(4)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        styles.forEach { (style, name) ->
            val isSelected = crosshairConfig.style == style
            val btn = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(30), 1f).apply { rightMargin = dp(3) }
                text = name
                setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#A0A7B8"))
                textSize = 9f
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    setColor(if (isSelected) Color.parseColor("#FF1E3C") else Color.parseColor("#181B26"))
                    cornerRadius = dp(6).toFloat()
                }
                setOnClickListener {
                    onCrosshairStyleSelected?.invoke(style)
                    setupView()
                }
            }
            row.addView(btn)
        }
        container.addView(row)
        return container
    }

    private fun createSlidersSection(): LinearLayout {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            orientation = LinearLayout.VERTICAL
        }

        val sizeHeader = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            val label = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "Size"
                setTextColor(Color.parseColor("#A0A7B8"))
                textSize = 10f
            }
            sizeValueText = TextView(context).apply {
                text = "${crosshairConfig.size.toInt()} dp"
                setTextColor(Color.parseColor("#FF1E3C"))
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
            }
            addView(label)
            addView(sizeValueText)
        }
        container.addView(sizeHeader)

        val sizeBar = SeekBar(context).apply {
            max = 50
            progress = (crosshairConfig.size - 16f).toInt().coerceIn(0, 50)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val sizeVal = (16 + progress).toFloat()
                    sizeValueText.text = "${sizeVal.toInt()} dp"
                    if (fromUser) onCrosshairSizeChanged?.invoke(sizeVal)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        container.addView(sizeBar)

        val opacityHeader = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            val label = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "Opacity"
                setTextColor(Color.parseColor("#A0A7B8"))
                textSize = 10f
            }
            opacityValueText = TextView(context).apply {
                text = "${(crosshairConfig.opacity * 100).toInt()}%"
                setTextColor(Color.parseColor("#00FF66"))
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
            }
            addView(label)
            addView(opacityValueText)
        }
        container.addView(opacityHeader)

        val opacityBar = SeekBar(context).apply {
            max = 80
            progress = ((crosshairConfig.opacity - 0.2f) * 100).toInt().coerceIn(0, 80)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val opVal = (20 + progress) / 100f
                    opacityValueText.text = "${(opVal * 100).toInt()}%"
                    if (fromUser) onCrosshairOpacityChanged?.invoke(opVal)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        container.addView(opacityBar)

        return container
    }

    private fun createProfileSection(): LinearLayout {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            orientation = LinearLayout.VERTICAL
        }

        val headerLabel = TextView(context).apply {
            text = "GAMING PROFILES"
            setTextColor(Color.parseColor("#8A93A6"))
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
        }
        container.addView(headerLabel)

        val scroll = HorizontalScrollView(context).apply {
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4), 0, dp(4))
        }

        profiles.take(5).forEach { profile ->
            val isSel = profile.id == activeProfileId
            val pill = TextView(context).apply {
                text = profile.name.split(" ").first()
                setTextColor(if (isSel) Color.WHITE else Color.parseColor("#8A93A6"))
                textSize = 9f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(dp(10), dp(5), dp(10), dp(5))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    rightMargin = dp(5)
                }
                background = GradientDrawable().apply {
                    setColor(if (isSel) Color.parseColor("#FF1E3C") else Color.parseColor("#181B26"))
                    cornerRadius = dp(6).toFloat()
                }
                setOnClickListener {
                    onProfileSelected?.invoke(profile.id)
                    setupView()
                }
            }
            row.addView(pill)
        }
        scroll.addView(row)
        container.addView(scroll)
        return container
    }

    private fun createSensitivitySection(): LinearLayout {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#12141C"))
                cornerRadius = dp(8).toFloat()
            }
        }

        val active = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull()

        val row = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val items = listOf(
            "GEN" to (active?.general ?: 95),
            "RED" to (active?.redDot ?: 100),
            "2X" to (active?.scope2x ?: 88),
            "4X" to (active?.scope4x ?: 82),
            "AWN" to (active?.sniper ?: 65),
            "LOOK" to (active?.freeLook ?: 70)
        )

        items.forEach { (lbl, valInt) ->
            val col = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }
            val l = TextView(context).apply {
                text = lbl
                setTextColor(Color.parseColor("#7A8294"))
                textSize = 8f
            }
            val v = TextView(context).apply {
                text = "$valInt"
                setTextColor(Color.parseColor("#00FF66"))
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
            }
            col.addView(l)
            col.addView(v)
            row.addView(col)
        }
        container.addView(row)
        return container
    }

    private fun createButtonBg(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(6).toFloat()
        }
    }

    private fun createSpacer(widthPx: Int): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(widthPx, 1)
        }
    }
}
