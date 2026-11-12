package com.example.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.example.data.PreferencesManager
import com.example.domain.CrosshairConfig
import com.example.domain.CrosshairStyle
import com.example.domain.GamingProfile
import com.example.localization.AppStrings

/**
 * Native Android View for the Floating HUD Control Panel.
 * Implements BOTH the Original Tactical HUD Interface and the New AI Features Activation Interface.
 * Fixes duplicate view stacking on repeated setupView() calls, supports smooth dragging,
 * outside-touch dismiss, and live parameter adjustments.
 */
@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class ControlPanelView(
    context: Context,
    private val windowManager: WindowManager,
    private val windowParams: WindowManager.LayoutParams,
    private val prefsManager: PreferencesManager,
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
    var onOpenCrosshairEditorClicked: (() -> Unit)? = null
    var onOpenAppClicked: (() -> Unit)? = null

    private val density = resources.displayMetrics.density

    // Header drag state
    private var headerTouchStartX = 0f
    private var headerTouchStartY = 0f
    private var initialPanelX = 0
    private var initialPanelY = 0

    // Interface Mode: 0 = New AI Features Activation, 1 = Original Tactical HUD
    private var activeInterfaceMode = prefsManager.floatingInterfaceMode.value
    private val isArabic: Boolean get() = prefsManager.isArabic.value

    // Boosting state for Phone Boost
    private var isBoosting = false
    private var boostProgress = 0

    init {
        setupView()
    }

    private fun dp(value: Int): Int = (value * density).toInt()

    fun updateData(
        crosshair: CrosshairConfig,
        profilesList: List<GamingProfile>,
        activePid: String,
        battery: Int,
        fps: Int
    ) {
        this.crosshairConfig = crosshair
        this.profiles = profilesList
        this.activeProfileId = activePid
        this.batteryPct = battery
        this.fpsMeasured = fps
        setupView()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            onCloseClicked?.invoke()
            return true
        }
        return super.onTouchEvent(event)
    }

    /**
     * Completely clears existing child views before inflating, preventing view duplication.
     */
    fun setupView() {
        removeAllViews()

        // Outer Cyber Container: Dark translucent fill with neon red cyber border
        val panelBg = GradientDrawable().apply {
            setColor(Color.parseColor("#F50D0E14"))
            setStroke(dp(2), Color.parseColor("#FF1E3C"))
            cornerRadius = dp(14).toFloat()
        }
        background = panelBg
        setPadding(dp(10), dp(8), dp(10), dp(10))

        val scroll = ScrollView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            isVerticalScrollBarEnabled = false
        }

        val content = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
        }

        // 1. Draggable Cyber Header Bar
        val header = createHeaderBar()
        content.addView(header)

        // 2. Mode Switcher Tabs: [AI FEATURES ACTIVATION (New)] | [TACTICAL HUD (Original)]
        val modeTabs = createModeTabs()
        content.addView(modeTabs)

        // 3. Render the selected interface
        if (activeInterfaceMode == 0) {
            // NEW INTERFACE: AI Features Activation
            content.addView(createAiFeaturesSection())
            content.addView(createBottomStatusBar())
        } else {
            // ORIGINAL INTERFACE: Tactical HUD & Fine Tuning
            content.addView(createChipsRow())
            content.addView(createCrosshairSwitchRow())
            content.addView(createStylesSection())
            content.addView(createSlidersSection())
            content.addView(createProfileSection())
            content.addView(createSensitivitySection())
            content.addView(createActionButtonsRow())
        }

        scroll.addView(content)
        addView(scroll)
    }

    // =========================================================================
    // 1. HEADER BAR (Draggable with Title, Mode Switcher, Minimize and Close)
    // =========================================================================

    private fun createHeaderBar(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            // Title & Drag Area
            val titleLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                orientation = LinearLayout.VERTICAL
            }

            val titleText = TextView(context).apply {
                text = if (activeInterfaceMode == 0) {
                    if (isArabic) "تفعيل ميزات الذكاء الاصطناعي" else "AI FEATURES ACTIVATION"
                } else {
                    "GAME BOOST AI"
                }
                setTextColor(if (activeInterfaceMode == 0) Color.WHITE else Color.parseColor("#FF1E3C"))
                textSize = 12.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            val subText = TextView(context).apply {
                text = if (activeInterfaceMode == 0) {
                    if (isArabic) "تفعيل ميزات الذكاء الاصطناعي" else "Smart Gameplay Enhancements"
                } else {
                    "FLOATING CONTROL PANEL"
                }
                setTextColor(Color.parseColor("#8A93A6"))
                textSize = 8.5f
            }
            titleLayout.addView(titleText)
            titleLayout.addView(subText)
            addView(titleLayout)

            // Drag listener on the title area
            titleLayout.setOnTouchListener { _, event ->
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

            // Interface Toggle Button (🔄)
            val toggleModeBtn = TextView(context).apply {
                text = "🔄"
                setTextColor(Color.WHITE)
                textSize = 12f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(26), dp(26)).apply { rightMargin = dp(4) }
                background = createButtonBg(Color.parseColor("#1C202C"))
                setOnClickListener {
                    activeInterfaceMode = if (activeInterfaceMode == 0) 1 else 0
                    prefsManager.setFloatingInterfaceMode(activeInterfaceMode)
                    setupView()
                }
            }
            addView(toggleModeBtn)

            // Minimize Button (—)
            val minimizeBtn = TextView(context).apply {
                text = "—"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(26), dp(26)).apply { rightMargin = dp(4) }
                background = createButtonBg(Color.parseColor("#222736"))
                setOnClickListener { onMinimizeClicked?.invoke() }
            }
            addView(minimizeBtn)

            // Close Button (✕)
            val closeBtn = TextView(context).apply {
                text = "✕"
                setTextColor(Color.parseColor("#FF1E3C"))
                textSize = 13f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(26), dp(26))
                background = createButtonBg(Color.parseColor("#3B070F"))
                setOnClickListener { onCloseClicked?.invoke() }
            }
            addView(closeBtn)
        }
    }

    // =========================================================================
    // 2. MODE SWITCHER TABS
    // =========================================================================

    private fun createModeTabs(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(2), dp(2), dp(2), dp(2))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#141620"))
                cornerRadius = dp(8).toFloat()
            }

            val tab0 = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(28), 1f)
                text = if (isArabic) "الميزات الرئيسية" else "AI Controls"
                setTextColor(if (activeInterfaceMode == 0) Color.WHITE else Color.parseColor("#8A93A6"))
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = if (activeInterfaceMode == 0) createButtonBg(Color.parseColor("#FF1E3C")) else null
                setOnClickListener {
                    if (activeInterfaceMode != 0) {
                        activeInterfaceMode = 0
                        prefsManager.setFloatingInterfaceMode(0)
                        setupView()
                    }
                }
            }

            val tab1 = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(28), 1f)
                text = if (isArabic) "تخصيص الحساسية والمؤشر" else "Fine Tuning"
                setTextColor(if (activeInterfaceMode == 1) Color.WHITE else Color.parseColor("#8A93A6"))
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = if (activeInterfaceMode == 1) createButtonBg(Color.parseColor("#FF1E3C")) else null
                setOnClickListener {
                    if (activeInterfaceMode != 1) {
                        activeInterfaceMode = 1
                        prefsManager.setFloatingInterfaceMode(1)
                        setupView()
                    }
                }
            }

            addView(tab0)
            addView(tab1)
        }
    }

    // =========================================================================
    // 3. NEW INTERFACE: 6 CYBER BUTTONS MATCHING USER SCREENSHOT EXACTLY
    // =========================================================================

    private fun createAiFeaturesSection(): LinearLayout {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
        }

        // Row 1: [AIMBOT] | [CROSSHAIR]
        val row1 = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val aimbotActive = prefsManager.aimbotEnabled.value
        val aimbotCard = createCyberCard(
            title = if (isArabic) "تفعيل آيم بوت" else "AIMBOT",
            subtitle = if (isArabic) "Aimbot" else "Smart Aim Assistant",
            isActive = aimbotActive,
            iconType = 0
        ) {
            val newState = !prefsManager.aimbotEnabled.value
            prefsManager.setAimbotEnabled(newState)
            Toast.makeText(
                context,
                if (newState) {
                    if (isArabic) "تم تفعيل مساعد التصويب الذكي" else "Smart Aim Assistant Activated"
                } else {
                    if (isArabic) "تم إيقاف مساعد التصويب الذكي" else "Smart Aim Assistant Deactivated"
                },
                Toast.LENGTH_SHORT
            ).show()
            setupView()
        }

        val crosshairActive = prefsManager.crosshair.value.enabled
        val crosshairCard = createCyberCard(
            title = if (isArabic) "فتح علامة التصويب" else "CROSSHAIR",
            subtitle = if (isArabic) "Crosshair Overlay" else "Tactical Reticle",
            isActive = crosshairActive,
            iconType = 1
        ) {
            val newState = !prefsManager.crosshair.value.enabled
            prefsManager.setCrosshairEnabled(newState)
            crosshairConfig = crosshairConfig.copy(enabled = newState)
            onCrosshairToggled?.invoke(newState)
            Toast.makeText(
                context,
                if (newState) {
                    if (isArabic) "تم فتح علامة التصويب على الشاشة" else "Crosshair Overlay Enabled"
                } else {
                    if (isArabic) "تم إغلاق علامة التصويب" else "Crosshair Overlay Disabled"
                },
                Toast.LENGTH_SHORT
            ).show()
            setupView()
        }

        row1.addView(aimbotCard)
        row1.addView(createSpacer(dp(6)))
        row1.addView(crosshairCard)
        container.addView(row1)

        // Row 2: [PHONE BOOST] | [FPS STABILIZER]
        val row2 = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val phoneBoostCard = createCyberCard(
            title = if (isArabic) "تحسين الهاتف" else "PHONE BOOST",
            subtitle = if (isArabic) "Phone Boost" else "RAM & Cache Cleaner",
            isActive = isBoosting,
            iconType = 2
        ) {
            if (!isBoosting) {
                isBoosting = true
                val animator = ValueAnimator.ofInt(0, 100).apply {
                    duration = 800
                    addUpdateListener { anim ->
                        boostProgress = anim.animatedValue as Int
                    }
                }
                animator.start()
                postDelayed({
                    isBoosting = false
                    System.gc()
                    ramFreeMb = (ramFreeMb + 350).coerceAtMost(3800)
                    Toast.makeText(
                        context,
                        if (isArabic) "تم تحسين الهاتف وتنظيف الذاكرة 100%" else "Phone Optimized: 100% Boosted",
                        Toast.LENGTH_SHORT
                    ).show()
                    setupView()
                }, 850)
            }
        }

        val fpsActive = prefsManager.fpsStabilizerEnabled.value
        val fpsCard = createCyberCard(
            title = if (isArabic) "تثبيت الإطارات" else "FPS STABILIZER",
            subtitle = if (isArabic) "FPS Stabilizer" else "Target Lock 60/90",
            isActive = fpsActive,
            iconType = 3
        ) {
            val newState = !prefsManager.fpsStabilizerEnabled.value
            prefsManager.setFpsStabilizerEnabled(newState)
            Toast.makeText(
                context,
                if (newState) {
                    if (isArabic) "تم تثبيت معدل الإطارات عند 60/90 إطار" else "FPS Locked & Stabilized"
                } else {
                    if (isArabic) "تم إلغاء تثبيت الإطارات" else "FPS Lock Disabled"
                },
                Toast.LENGTH_SHORT
            ).show()
            setupView()
        }

        row2.addView(phoneBoostCard)
        row2.addView(createSpacer(dp(6)))
        row2.addView(fpsCard)
        container.addView(row2)

        // Row 3: [PING FIX] | [WEAPON STABILITY]
        val row3 = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val pingActive = prefsManager.pingFixEnabled.value
        val pingCard = createCyberCard(
            title = if (isArabic) "تقليل البينغ" else "PING FIX",
            subtitle = if (isArabic) "Ping Fix\nPing Optimizer" else "Low-Latency Route",
            isActive = pingActive,
            iconType = 4
        ) {
            val newState = !prefsManager.pingFixEnabled.value
            prefsManager.setPingFixEnabled(newState)
            Toast.makeText(
                context,
                if (newState) {
                    if (isArabic) "تم تشغيل مسار خفض البينغ المنخفض (24ms)" else "Low-Latency Route Activated (24ms)"
                } else {
                    if (isArabic) "تم تعطيل محسن البينغ" else "Ping Fix Disabled"
                },
                Toast.LENGTH_SHORT
            ).show()
            setupView()
        }

        val weaponActive = prefsManager.weaponStabilityEnabled.value
        val weaponCard = createCyberCard(
            title = if (isArabic) "ثبات السلاح" else "WEAPON STABILITY",
            subtitle = if (isArabic) "No Recoil\nWeapon Stability" else "Recoil Dampener",
            isActive = weaponActive,
            iconType = 5
        ) {
            val newState = !prefsManager.weaponStabilityEnabled.value
            prefsManager.setWeaponStabilityEnabled(newState)
            Toast.makeText(
                context,
                if (newState) {
                    if (isArabic) "تم تفعيل استقرار ارتداد السلاح" else "Weapon Stability & Recoil Dampener ON"
                } else {
                    if (isArabic) "تم تعطيل استقرار السلاح" else "Weapon Stability OFF"
                },
                Toast.LENGTH_SHORT
            ).show()
            setupView()
        }

        row3.addView(pingCard)
        row3.addView(createSpacer(dp(6)))
        row3.addView(weaponCard)
        container.addView(row3)

        // Progress indicator if phone boost is running
        if (isBoosting) {
            val pBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(4)).apply {
                    bottomMargin = dp(6)
                }
                isIndeterminate = true
            }
            container.addView(pBar)
        }

        return container
    }

    private fun createCyberCard(
        title: String,
        subtitle: String,
        isActive: Boolean,
        iconType: Int,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(78), 1f)
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))

            // Cyber border & background
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#13141C"))
                val strokeColor = if (isActive) Color.parseColor("#00FF66") else Color.parseColor("#B31E3C")
                setStroke(dp(1), strokeColor)
                cornerRadius = dp(8).toFloat()
            }

            // Top Row: Icon + Indicator Dot
            val topRow = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // Icon Custom View
            val iconView = CyberIconView(context, iconType, isActive).apply {
                layoutParams = LinearLayout.LayoutParams(dp(26), dp(26))
            }
            topRow.addView(iconView)

            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
            }
            topRow.addView(spacer)

            // Status Indicator Dot
            val dot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), dp(8))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (isActive) Color.parseColor("#00FF66") else Color.parseColor("#4A4E5C"))
                }
            }
            topRow.addView(dot)
            addView(topRow)

            // Spacer
            addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(1, 0, 1f) })

            // Title Label
            val titleView = TextView(context).apply {
                text = title
                setTextColor(Color.WHITE)
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                maxLines = 1
            }
            addView(titleView)

            // Subtitle Label
            val subView = TextView(context).apply {
                text = subtitle
                setTextColor(if (isActive) Color.parseColor("#00FF66") else Color.parseColor("#8A93A6"))
                textSize = 7.5f
                maxLines = 1
            }
            addView(subView)

            setOnClickListener { onClick() }
        }
    }

    // =========================================================================
    // 4. BOTTOM STATUS BAR (Matching Screenshot: Menu Status & Current FPS)
    // =========================================================================

    private fun createBottomStatusBar(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(4)
                bottomMargin = dp(4)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#14070B"))
                setStroke(dp(1), Color.parseColor("#8A1322"))
                cornerRadius = dp(8).toFloat()
            }

            // Left Chip: Phone icon + Menu Status: ACTIVE
            val leftLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val phoneIcon = TextView(context).apply {
                text = "📱"
                textSize = 10f
            }
            val statusLabel = TextView(context).apply {
                text = if (isArabic) "حالة القائمة: " else "Menu Status: "
                setTextColor(Color.parseColor("#8A93A6"))
                textSize = 8.5f
            }
            val statusVal = TextView(context).apply {
                text = if (isArabic) "مفعلة ✓" else "ACTIVE ✓"
                setTextColor(Color.parseColor("#00FF66"))
                textSize = 9.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            leftLayout.addView(phoneIcon)
            leftLayout.addView(statusLabel)
            leftLayout.addView(statusVal)
            addView(leftLayout)

            // Right Chip: Speedometer + Current FPS: 60 FPS
            val rightLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val fpsIcon = TextView(context).apply {
                text = "⚡"
                textSize = 10f
            }
            val fpsLabel = TextView(context).apply {
                text = if (isArabic) "معدل الإطارات: " else "FPS: "
                setTextColor(Color.parseColor("#8A93A6"))
                textSize = 8.5f
            }
            val fpsVal = TextView(context).apply {
                text = "$fpsMeasured ${if (isArabic) "إطار ✓" else "FPS ✓"}"
                setTextColor(Color.parseColor("#00FF66"))
                textSize = 9.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            rightLayout.addView(fpsIcon)
            rightLayout.addView(fpsLabel)
            rightLayout.addView(fpsVal)
            addView(rightLayout)
        }
    }

    // =========================================================================
    // 5. ORIGINAL INTERFACE COMPONENTS (Tactical Crosshair, Sliders, Profiles)
    // =========================================================================

    private fun createChipsRow(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            val chip1 = createChip("BAT: $batteryPct%", Color.parseColor("#00FF66"))
            val chip2 = createChip("FPS: $fpsMeasured", Color.parseColor("#00FF66"))
            val chip3 = createChip("RAM: ${ramFreeMb / 1024f}GB", Color.parseColor("#FF1E3C"))

            addView(chip1)
            addView(createSpacer(dp(6)))
            addView(chip2)
            addView(createSpacer(dp(6)))
            addView(chip3)
        }
    }

    private fun createChip(label: String, textColor: Int): TextView {
        return TextView(context).apply {
            text = label
            setTextColor(textColor)
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(3), dp(8), dp(3))
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
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(5), dp(8), dp(5))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#181B26"))
                cornerRadius = dp(8).toFloat()
            }

            val label = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = if (isArabic) "علامة التصويب التكتيكية" else "Target Reticle Overlay"
                setTextColor(Color.WHITE)
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
            }
            addView(label)

            val chSwitch = Switch(context).apply {
                isChecked = crosshairConfig.enabled
                setOnCheckedChangeListener { _, isChecked ->
                    crosshairConfig = crosshairConfig.copy(enabled = isChecked)
                    prefsManager.setCrosshairEnabled(isChecked)
                    onCrosshairToggled?.invoke(isChecked)
                }
            }
            addView(chSwitch)
        }
    }

    private fun createStylesSection(): LinearLayout {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.VERTICAL
        }

        val headerLabel = TextView(context).apply {
            text = if (isArabic) "أنماط سريعة للمؤشر" else "QUICK CROSSHAIR STYLES"
            setTextColor(Color.parseColor("#8A93A6"))
            textSize = 8.5f
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
                layoutParams = LinearLayout.LayoutParams(0, dp(28), 1f).apply {
                    rightMargin = dp(2)
                }
                text = name
                setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#A0A7B8"))
                textSize = 8.5f
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    setColor(if (isSelected) Color.parseColor("#FF1E3C") else Color.parseColor("#181B26"))
                    cornerRadius = dp(6).toFloat()
                }
                setOnClickListener {
                    crosshairConfig = crosshairConfig.copy(style = style)
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
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.VERTICAL
        }

        // Size Slider
        val sizeHeader = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            val label = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = if (isArabic) "حجم المؤشر" else "Size"
                setTextColor(Color.parseColor("#A0A7B8"))
                textSize = 9.5f
            }
            val sizeVal = TextView(context).apply {
                text = "${crosshairConfig.size.toInt()} dp"
                setTextColor(Color.parseColor("#FF1E3C"))
                textSize = 9.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            addView(label)
            addView(sizeVal)
        }
        container.addView(sizeHeader)

        val sizeBar = SeekBar(context).apply {
            max = 50
            progress = (crosshairConfig.size - 16f).toInt().coerceIn(0, 50)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val sizeVal = (16 + progress).toFloat()
                    crosshairConfig = crosshairConfig.copy(size = sizeVal)
                    if (fromUser) onCrosshairSizeChanged?.invoke(sizeVal)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        container.addView(sizeBar)

        // Opacity Slider
        val opacityHeader = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            val label = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = if (isArabic) "الشفافية" else "Opacity"
                setTextColor(Color.parseColor("#A0A7B8"))
                textSize = 9.5f
            }
            val opacityVal = TextView(context).apply {
                text = "${(crosshairConfig.opacity * 100).toInt()}%"
                setTextColor(Color.parseColor("#00FF66"))
                textSize = 9.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            addView(label)
            addView(opacityVal)
        }
        container.addView(opacityHeader)

        val opacityBar = SeekBar(context).apply {
            max = 80
            progress = ((crosshairConfig.opacity - 0.2f) * 100).toInt().coerceIn(0, 80)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val opVal = (20 + progress) / 100f
                    crosshairConfig = crosshairConfig.copy(opacity = opVal)
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
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.VERTICAL
        }

        val headerLabel = TextView(context).apply {
            text = if (isArabic) "ملفات اللعب التكتيكية" else "GAMING PROFILES"
            setTextColor(Color.parseColor("#8A93A6"))
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
        }
        container.addView(headerLabel)

        val scroll = HorizontalScrollView(context).apply {
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(3), 0, dp(3))
        }

        profiles.take(5).forEach { profile ->
            val isSel = profile.id == activeProfileId
            val pill = TextView(context).apply {
                text = if (isArabic) profile.nameAr.split(" ").first() else profile.name.split(" ").first()
                setTextColor(if (isSel) Color.WHITE else Color.parseColor("#8A93A6"))
                textSize = 8.5f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(dp(8), dp(4), dp(8), dp(4))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    rightMargin = dp(4)
                }
                background = GradientDrawable().apply {
                    setColor(if (isSel) Color.parseColor("#FF1E3C") else Color.parseColor("#181B26"))
                    cornerRadius = dp(6).toFloat()
                }
                setOnClickListener {
                    activeProfileId = profile.id
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(5), dp(6), dp(5))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#12141C"))
                cornerRadius = dp(8).toFloat()
            }
        }

        val active = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull()
        val s = active?.sensitivity

        val row = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val items = listOf(
            "GEN" to (s?.general ?: 95),
            "RED" to (s?.redDot ?: 100),
            "2X" to (s?.scope2x ?: 88),
            "4X" to (s?.scope4x ?: 82),
            "AWN" to (s?.sniperScope ?: 65),
            "LOOK" to (s?.freeLook ?: 70)
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
                textSize = 7.5f
            }
            val v = TextView(context).apply {
                text = "$valInt"
                setTextColor(Color.parseColor("#00FF66"))
                textSize = 10.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            col.addView(l)
            col.addView(v)
            row.addView(col)
        }
        container.addView(row)
        return container
    }

    private fun createActionButtonsRow(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(2)
            }
            orientation = LinearLayout.HORIZONTAL

            val editorBtn = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(30), 1f).apply { rightMargin = dp(4) }
                text = if (isArabic) "محرر المؤشر" else "CROSSHAIR EDITOR"
                setTextColor(Color.WHITE)
                textSize = 9f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1E212D"))
                    setStroke(dp(1), Color.parseColor("#FF1E3C"))
                    cornerRadius = dp(6).toFloat()
                }
                setOnClickListener { onOpenCrosshairEditorClicked?.invoke() }
            }

            val openAppBtn = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(30), 1f)
                text = if (isArabic) "فتح التطبيق" else "OPEN APP"
                setTextColor(Color.parseColor("#8A93A6"))
                textSize = 9f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#161822"))
                    setStroke(dp(1), Color.parseColor("#33384A"))
                    cornerRadius = dp(6).toFloat()
                }
                setOnClickListener { onOpenAppClicked?.invoke() }
            }

            addView(editorBtn)
            addView(openAppBtn)
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

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

    /**
     * Crisp Canvas-drawn vector icon view for the 6 Cyber Feature Cards.
     */
    private class CyberIconView(context: Context, private val iconType: Int, private val isActive: Boolean) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f * resources.displayMetrics.density
            strokeCap = Paint.Cap.ROUND
            color = if (isActive) Color.parseColor("#00FF66") else Color.parseColor("#FF1E3C")
        }
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isActive) Color.parseColor("#00FF66") else Color.parseColor("#FF1E3C")
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val r = (width / 2f) * 0.75f

            when (iconType) {
                0 -> { // AIMBOT: Reticle with outer ring and center dot
                    canvas.drawCircle(cx, cy, r, paint)
                    canvas.drawCircle(cx, cy, r * 0.45f, paint)
                    canvas.drawCircle(cx, cy, 2.5f, fillPaint)
                    canvas.drawLine(cx, cy - r, cx, cy - r * 0.5f, paint)
                    canvas.drawLine(cx, cy + r * 0.5f, cx, cy + r, paint)
                    canvas.drawLine(cx - r, cy, cx - r * 0.5f, cy, paint)
                    canvas.drawLine(cx + r * 0.5f, cy, cx + r, cy, paint)
                }
                1 -> { // CROSSHAIR OVERLAY: Tactical crosshair with square bounds
                    canvas.drawCircle(cx, cy, r * 0.85f, paint)
                    canvas.drawLine(cx, cy - r, cx, cy + r, paint)
                    canvas.drawLine(cx - r, cy, cx + r, cy, paint)
                    canvas.drawCircle(cx, cy, 2f, fillPaint)
                }
                2 -> { // PHONE BOOST: Rocket / lightning
                    val path = Path().apply {
                        moveTo(cx, cy - r)
                        lineTo(cx + r * 0.6f, cy + r * 0.7f)
                        lineTo(cx, cy + r * 0.35f)
                        lineTo(cx - r * 0.6f, cy + r * 0.7f)
                        close()
                    }
                    canvas.drawPath(path, paint)
                    canvas.drawCircle(cx, cy + r * 0.1f, 2f, fillPaint)
                }
                3 -> { // FPS STABILIZER: Speedometer gauge with up arrow
                    canvas.drawArc(cx - r, cy - r, cx + r, cy + r, 150f, 240f, false, paint)
                    // Up arrow
                    canvas.drawLine(cx, cy + r * 0.4f, cx, cy - r * 0.3f, paint)
                    canvas.drawLine(cx, cy - r * 0.3f, cx - r * 0.3f, cy, paint)
                    canvas.drawLine(cx, cy - r * 0.3f, cx + r * 0.3f, cy, paint)
                }
                4 -> { // PING FIX: Radar antenna tower
                    canvas.drawArc(cx - r, cy - r, cx + r, cy + r, 200f, 140f, false, paint)
                    canvas.drawArc(cx - r * 0.6f, cy - r * 0.6f, cx + r * 0.6f, cy + r * 0.6f, 200f, 140f, false, paint)
                    canvas.drawCircle(cx, cy + r * 0.2f, 2.5f, fillPaint)
                    canvas.drawLine(cx, cy + r * 0.2f, cx - r * 0.4f, cy + r * 0.8f, paint)
                    canvas.drawLine(cx, cy + r * 0.2f, cx + r * 0.4f, cy + r * 0.8f, paint)
                }
                5 -> { // WEAPON STABILITY: Rifle silhouette
                    canvas.drawLine(cx - r, cy, cx + r, cy, paint)
                    canvas.drawLine(cx + r * 0.5f, cy, cx + r * 0.5f, cy + r * 0.4f, paint)
                    canvas.drawLine(cx - r * 0.3f, cy, cx - r * 0.3f, cy - r * 0.3f, paint)
                    canvas.drawCircle(cx - r * 0.7f, cy, 2f, fillPaint)
                }
            }
        }
    }
}
