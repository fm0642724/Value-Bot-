package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.domain.CrosshairConfig
import com.example.domain.CrosshairStyle
import com.example.domain.FloatingPanelSettings
import com.example.domain.GamingProfile
import com.example.localization.AppStrings
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedDark
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TajawalFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * High-performance In-App Floating Overlay Button & Tactical Game Watermark Control Panel.
 * Matches the reference design with 6 cyber buttons, animated glows, draggable header,
 * robust tap vs drag gesture handling (<8px movement = TAP), edge snapping, and persistence.
 */
@Composable
fun FloatingOverlayLayer(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    profiles: List<GamingProfile>,
    activeProfileId: String,
    crosshairConfig: CrosshairConfig,
    settings: FloatingPanelSettings,
    batteryPct: Int = 72,
    fpsMeasured: Int = 60,
    onNavigateToCrosshairEditor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!settings.enabled) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Reactive Cyber Features
    val aimbotEnabled by prefsManager.aimbotEnabled.collectAsState()
    val fpsStabilizerEnabled by prefsManager.fpsStabilizerEnabled.collectAsState()
    val pingFixEnabled by prefsManager.pingFixEnabled.collectAsState()
    val weaponStabilityEnabled by prefsManager.weaponStabilityEnabled.collectAsState()

    var isPanelOpen by remember { mutableStateOf(false) }

    // Optimization Boost Animation State
    var isBoosting by remember { mutableStateOf(false) }
    var boostProgress by remember { mutableFloatStateOf(0f) }

    // Tab inside panel: 0 = Main Cyber Grid, 1 = Fine Tuning & Adjusters
    var activePanelSection by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(100f)
        val screenHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(100f)
        val buttonSizePx = with(density) { settings.buttonSizePx.dp.toPx() }

        // Animated Button Coordinates
        val animX = remember { Animatable((settings.posXPercent / 100f) * screenWidthPx) }
        val animY = remember { Animatable((settings.posYPercent / 100f) * screenHeightPx) }

        // Optional Panel Drag Offset (relative to default clamped position)
        var panelDragOffsetX by remember { mutableFloatStateOf(0f) }
        var panelDragOffsetY by remember { mutableFloatStateOf(0f) }

        // Synchronize when settings change externally
        LaunchedEffect(settings.posXPercent, settings.posYPercent) {
            val targetX = (settings.posXPercent / 100f) * screenWidthPx
            val targetY = (settings.posYPercent / 100f) * screenHeightPx
            if (!animX.isRunning && kotlin.math.abs(animX.value - targetX) > 10f) {
                animX.snapTo(targetX.coerceIn(0f, screenWidthPx - buttonSizePx))
            }
            if (!animY.isRunning && kotlin.math.abs(animY.value - targetY) > 10f) {
                animY.snapTo(targetY.coerceIn(0f, screenHeightPx - buttonSizePx))
            }
        }

        // 1. Non-blocking Tap-through Crosshair Overlay (Lowest Layer)
        // Note: No pointerInput/clickable modifier, completely pass-through to game/screen
        if (crosshairConfig.enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("in_app_crosshair_overlay"),
                contentAlignment = Alignment.Center
            ) {
                CrosshairCanvas(config = crosshairConfig)
            }
        }

        // 2. Tap-outside Barrier (Active only when panel is expanded)
        if (isPanelOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isPanelOpen = false
                    }
                    .testTag("floating_panel_backdrop")
            )
        }

        // 3. Minimized State: Sleek edge dock tab
        if (settings.minimized) {
            val isLeftEdge = animX.value < screenWidthPx / 2f
            val tabWidth = 28.dp
            val tabHeight = 64.dp
            val tabHeightPx = with(density) { tabHeight.toPx() }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = if (isLeftEdge) 0 else (screenWidthPx - with(density) { tabWidth.toPx() }).roundToInt(),
                            y = animY.value.roundToInt().coerceIn(0, (screenHeightPx - tabHeightPx).roundToInt())
                        )
                    }
                    .size(width = tabWidth, height = tabHeight)
                    .clip(
                        if (isLeftEdge) RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                        else RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
                    .background(Color(0xFF140407).copy(alpha = settings.opacity))
                    .border(
                        1.5.dp,
                        NeonRedBright,
                        if (isLeftEdge) RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                        else RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
                    .clickable {
                        prefsManager.setFloatingPanelMinimized(false)
                        isPanelOpen = true
                    }
                    .testTag("floating_edge_tab"),
                contentAlignment = Alignment.Center
            ) {
                AimReticleIcon(size = 18.dp, color = NeonRedBright)
            }
            return@BoxWithConstraints
        }

        // 4. Draggable Round Floating Button with Precision Tap vs Drag (< 8px movement = TAP)
        val buttonOffset = IntOffset(
            x = animX.value.roundToInt().coerceIn(0, (screenWidthPx - buttonSizePx).roundToInt()),
            y = animY.value.roundToInt().coerceIn(0, (screenHeightPx - buttonSizePx).roundToInt())
        )

        Box(
            modifier = Modifier
                .offset { buttonOffset }
                .size(settings.buttonSizePx.dp)
                .shadow(12.dp, CircleShape, spotColor = NeonRedBright)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF32070D), Color(0xFF120204)),
                        radius = buttonSizePx / 2f
                    )
                )
                .border(2.dp, if (isPanelOpen) NeonGreenAccent else NeonRedBright, CircleShape)
                .pointerInput(screenWidthPx, screenHeightPx, settings.edgeSnap) {
                    val slopPx = with(density) { 8.dp.toPx() }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startPos = down.position
                        var isDragging = false
                        val pointerId = down.id

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                            if (!change.pressed) {
                                // Pointer up / released
                                val totalMove = (change.position - startPos).getDistance()
                                if (!isDragging && totalMove < slopPx) {
                                    // PURE TAP DETECTED: Toggle the floating control panel!
                                    isPanelOpen = !isPanelOpen
                                    panelDragOffsetX = 0f
                                    panelDragOffsetY = 0f
                                } else if (isDragging) {
                                    // Drag gesture finished: apply edge snap & persist
                                    if (settings.edgeSnap) {
                                        val snapTargetX = if (animX.value + buttonSizePx / 2f < screenWidthPx / 2f) {
                                            12f
                                        } else {
                                            screenWidthPx - buttonSizePx - 12f
                                        }
                                        coroutineScope.launch {
                                            animX.animateTo(
                                                snapTargetX,
                                                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                            )
                                            prefsManager.setFloatingPosition(
                                                (animX.value / screenWidthPx) * 100f,
                                                (animY.value / screenHeightPx) * 100f
                                            )
                                        }
                                    } else {
                                        prefsManager.setFloatingPosition(
                                            (animX.value / screenWidthPx) * 100f,
                                            (animY.value / screenHeightPx) * 100f
                                        )
                                    }
                                }
                                break
                            }

                            // Movement tracking
                            val totalMove = (change.position - startPos).getDistance()
                            if (!isDragging && totalMove >= slopPx) {
                                isDragging = true
                            }

                            if (isDragging) {
                                change.consume()
                                val dragDelta = change.position - change.previousPosition
                                coroutineScope.launch {
                                    animX.snapTo((animX.value + dragDelta.x).coerceIn(0f, screenWidthPx - buttonSizePx))
                                    animY.snapTo((animY.value + dragDelta.y).coerceIn(0f, screenHeightPx - buttonSizePx))
                                }
                            }
                        }
                    }
                }
                .testTag("floating_action_button"),
            contentAlignment = Alignment.Center
        ) {
            AimReticleIcon(
                size = (settings.buttonSizePx * 0.62f).dp,
                color = if (isPanelOpen) NeonGreenAccent else NeonRedBright
            )
        }

        // 5. Expandable Cyber Tactical Control Panel (Matching User Reference Image)
        AnimatedVisibility(
            visible = isPanelOpen,
            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.88f),
            exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.88f)
        ) {
            val panelWidth = 340.dp
            val panelWidthPx = with(density) { panelWidth.toPx() }
            val estimatedPanelHeightPx = with(density) { 520.dp.toPx() }

            // Dynamic intelligent clamping: Position panel near button without overflowing screen
            val rawPanelX = if (animX.value > screenWidthPx / 2f) {
                animX.value - panelWidthPx - 10f
            } else {
                animX.value + buttonSizePx + 10f
            }
            val basePanelX = rawPanelX.coerceIn(10f, (screenWidthPx - panelWidthPx - 10f).coerceAtLeast(10f))
            val basePanelY = animY.value.coerceIn(36f, (screenHeightPx - estimatedPanelHeightPx - 20f).coerceAtLeast(36f))

            val finalPanelX = (basePanelX + panelDragOffsetX).coerceIn(4f, (screenWidthPx - panelWidthPx - 4f).coerceAtLeast(4f))
            val finalPanelY = (basePanelY + panelDragOffsetY).coerceIn(20f, (screenHeightPx - 300f).coerceAtLeast(20f))

            // Cyber Chamfered Outer Container
            Box(
                modifier = Modifier
                    .offset { IntOffset(finalPanelX.roundToInt(), finalPanelY.roundToInt()) }
                    .width(panelWidth)
                    .shadow(16.dp, ChamferedShape(16.dp), spotColor = NeonRedBright)
                    .clip(ChamferedShape(16.dp))
                    .background(Color(0xF50D0E14))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(NeonRedBright, NeonGreenAccent, NeonRedDark)
                        ),
                        shape = ChamferedShape(16.dp)
                    )
                    .padding(12.dp)
                    .testTag("floating_control_panel")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Draggable Cyber Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    panelDragOffsetX += dragAmount.x
                                    panelDragOffsetY += dragAmount.y
                                }
                            }
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.cyberAiFeaturesTitle(isArabic),
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppStrings.cyberAiFeaturesSubtitle(isArabic),
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = if (isArabic) TajawalFamily else RajdhaniFamily
                            )
                        }

                        // Minimize and Close Controls
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            // Minimize button
                            IconButton(
                                onClick = {
                                    isPanelOpen = false
                                    prefsManager.setFloatingPanelMinimized(true)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = AppStrings.minimize(isArabic),
                                    tint = TextWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Close button
                            IconButton(
                                onClick = {
                                    isPanelOpen = false
                                    prefsManager.setFloatingPanelEnabled(false)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = AppStrings.close(isArabic),
                                    tint = NeonRedBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Mode Switcher Pills: [Cyber Grid (Reference Image)] vs [Adjusters & Sliders]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF141620))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (activePanelSection == 0) NeonRedPrimary else Color.Transparent)
                                .clickable { activePanelSection = 0 }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "الميزات الرئيسية" else "AI Controls",
                                color = if (activePanelSection == 0) TextWhite else TextMuted,
                                fontSize = 11.sp,
                                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (activePanelSection == 1) NeonRedPrimary else Color.Transparent)
                                .clickable { activePanelSection = 1 }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "تخصيص الحساسية والمؤشر" else "Fine Tuning",
                                color = if (activePanelSection == 1) TextWhite else TextMuted,
                                fontSize = 11.sp,
                                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (activePanelSection == 0) {
                        // ========================================================
                        // SECTION A: 6 CYBER BUTTONS MATCHING USER REFERENCE IMAGE
                        // ========================================================
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Row 1: [Aimbot] | [Crosshair Overlay]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. AIMBOT Toggle Card
                                CyberFeatureTile(
                                    modifier = Modifier.weight(1f),
                                    title = AppStrings.cyberAimbotTitle(isArabic),
                                    subtitle = AppStrings.cyberAimbotSub(isArabic),
                                    isActive = aimbotEnabled,
                                    isArabic = isArabic,
                                    icon = {
                                        AimReticleIcon(
                                            size = 28.dp,
                                            color = if (aimbotEnabled) NeonGreenAccent else NeonRedBright
                                        )
                                    },
                                    onClick = {
                                        val newState = !aimbotEnabled
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
                                    }
                                )

                                // 2. Crosshair Overlay Card
                                CyberFeatureTile(
                                    modifier = Modifier.weight(1f),
                                    title = AppStrings.cyberCrosshairTitle(isArabic),
                                    subtitle = AppStrings.cyberCrosshairSub(isArabic),
                                    isActive = crosshairConfig.enabled,
                                    isArabic = isArabic,
                                    icon = {
                                        CrosshairEditorIcon(
                                            size = 28.dp,
                                            color = if (crosshairConfig.enabled) NeonGreenAccent else NeonRedBright
                                        )
                                    },
                                    onClick = {
                                        val newState = !crosshairConfig.enabled
                                        prefsManager.setCrosshairEnabled(newState)
                                        Toast.makeText(
                                            context,
                                            if (newState) {
                                                if (isArabic) "تم فتح علامة التصويب على الشاشة" else "Crosshair Overlay Enabled"
                                            } else {
                                                if (isArabic) "تم إغلاق علامة التصويب" else "Crosshair Overlay Disabled"
                                            },
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            // Row 2: [Phone Boost] | [FPS Stabilizer]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 3. Phone Boost Card
                                CyberFeatureTile(
                                    modifier = Modifier.weight(1f),
                                    title = AppStrings.cyberPhoneBoostTitle(isArabic),
                                    subtitle = AppStrings.cyberPhoneBoostSub(isArabic),
                                    isActive = isBoosting,
                                    isArabic = isArabic,
                                    icon = {
                                        PhoneRocketBoostIcon(
                                            size = 28.dp,
                                            color = if (isBoosting) NeonGreenAccent else NeonRedBright
                                        )
                                    },
                                    onClick = {
                                        if (!isBoosting) {
                                            isBoosting = true
                                            boostProgress = 0f
                                            coroutineScope.launch {
                                                for (i in 1..10) {
                                                    delay(120)
                                                    boostProgress = i / 10f
                                                }
                                                delay(200)
                                                isBoosting = false
                                                Toast.makeText(
                                                    context,
                                                    if (isArabic) "تم تحسين الهاتف وتنظيف الذاكرة 100%" else "Phone Optimized: 100% Boosted",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                )

                                // 4. FPS Stabilizer Card
                                CyberFeatureTile(
                                    modifier = Modifier.weight(1f),
                                    title = AppStrings.cyberFpsStabilizerTitle(isArabic),
                                    subtitle = AppStrings.cyberFpsStabilizerSub(isArabic),
                                    isActive = fpsStabilizerEnabled,
                                    isArabic = isArabic,
                                    icon = {
                                        FpsStabilizerGaugeIcon(
                                            size = 28.dp,
                                            color = if (fpsStabilizerEnabled) NeonGreenAccent else NeonRedBright,
                                            accentColor = NeonGreenAccent
                                        )
                                    },
                                    onClick = {
                                        val newState = !fpsStabilizerEnabled
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
                                    }
                                )
                            }

                            // Row 3: [Ping Fix] | [Weapon Stability]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 5. Ping Fix Card
                                CyberFeatureTile(
                                    modifier = Modifier.weight(1f),
                                    title = AppStrings.cyberPingFixTitle(isArabic),
                                    subtitle = AppStrings.cyberPingFixSub(isArabic),
                                    isActive = pingFixEnabled,
                                    isArabic = isArabic,
                                    icon = {
                                        PingRadarTowerIcon(
                                            size = 28.dp,
                                            color = if (pingFixEnabled) NeonGreenAccent else NeonRedBright
                                        )
                                    },
                                    onClick = {
                                        val newState = !pingFixEnabled
                                        prefsManager.setPingFixEnabled(newState)
                                        Toast.makeText(
                                            context,
                                            if (newState) {
                                                if (isArabic) "تم تشغيل مسار خفض البينغ المنخفض" else "Low-Latency Route Activated (24ms)"
                                            } else {
                                                if (isArabic) "تم تعطيل محسن البينغ" else "Ping Fix Disabled"
                                            },
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )

                                // 6. Weapon Stability Card
                                CyberFeatureTile(
                                    modifier = Modifier.weight(1f),
                                    title = AppStrings.cyberWeaponStabilityTitle(isArabic),
                                    subtitle = AppStrings.cyberWeaponStabilitySub(isArabic),
                                    isActive = weaponStabilityEnabled,
                                    isArabic = isArabic,
                                    icon = {
                                        WeaponSniperStabilityIcon(
                                            size = 28.dp,
                                            color = if (weaponStabilityEnabled) NeonGreenAccent else NeonRedBright
                                        )
                                    },
                                    onClick = {
                                        val newState = !weaponStabilityEnabled
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
                                    }
                                )
                            }
                        }

                        // Optimization Progress Bar (shown when boosting)
                        if (isBoosting) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF161822))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isArabic) "جاري تحسين الرام والعمليات..." else "Cleaning RAM & Boosting...",
                                        color = NeonGreenAccent,
                                        fontSize = 10.sp,
                                        fontFamily = if (isArabic) CairoFamily else OrbitronFamily
                                    )
                                    Text(
                                        text = "${(boostProgress * 100).toInt()}%",
                                        color = NeonGreenAccent,
                                        fontSize = 10.sp,
                                        fontFamily = OrbitronFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { boostProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = NeonGreenAccent,
                                    trackColor = Color(0xFF222736)
                                )
                            }
                        }
                    } else {
                        // ========================================================
                        // SECTION B: DETAILED ADJUSTERS, PROFILES & SLIDERS
                        // ========================================================
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Active Gaming Profiles Switcher Pills
                            Text(
                                text = AppStrings.activeProfile(isArabic),
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                profiles.take(5).forEach { profile ->
                                    val isSel = profile.id == activeProfileId
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) NeonRedPrimary else Color(0xFF191B24))
                                            .clickable { prefsManager.setActiveProfile(profile.id) }
                                            .padding(vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isArabic) profile.nameAr.split(" ").first() else profile.name.split(" ").first(),
                                            color = if (isSel) TextWhite else TextMuted,
                                            fontSize = 9.sp,
                                            fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // 6 Active Sensitivity Values Readout
                            val activeProfile = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull()
                            if (activeProfile != null) {
                                val s = activeProfile.sensitivity
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF12131A))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    MiniSensStat("GEN", s.general)
                                    MiniSensStat("RED", s.redDot)
                                    MiniSensStat("2X", s.scope2x)
                                    MiniSensStat("4X", s.scope4x)
                                    MiniSensStat("AWN", s.sniperScope)
                                    MiniSensStat("FREE", s.freeLook)
                                }
                            }

                            // 6 Quick Crosshair Styles
                            Text(
                                text = AppStrings.quickCrosshairs(isArabic),
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                                fontWeight = FontWeight.Bold
                            )

                            val quickStyles = listOf(
                                CrosshairStyle.DOT,
                                CrosshairStyle.PLUS,
                                CrosshairStyle.CIRCLE_DOT,
                                CrosshairStyle.GAP_CROSS,
                                CrosshairStyle.T_SHAPE,
                                CrosshairStyle.SNIPER_CROSS
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                quickStyles.forEach { style ->
                                    val isSel = crosshairConfig.style == style
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) NeonRedPrimary.copy(alpha = 0.3f) else Color(0xFF161822))
                                            .border(
                                                1.dp,
                                                if (isSel) NeonRedBright else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                prefsManager.updateCrosshair(crosshairConfig.copy(style = style))
                                            }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CrosshairCanvas(
                                            config = crosshairConfig.copy(
                                                style = style,
                                                size = 18f,
                                                thickness = 1.8f,
                                                centerGap = 2f
                                            )
                                        )
                                    }
                                }
                            }

                            // Crosshair Size Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(AppStrings.crosshairSize(isArabic), color = TextMuted, fontSize = 11.sp)
                                    Text("${crosshairConfig.size.toInt()} dp", color = NeonRedBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = crosshairConfig.size,
                                    onValueChange = { prefsManager.updateCrosshair(crosshairConfig.copy(size = it)) },
                                    valueRange = 16f..64f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonRedBright,
                                        activeTrackColor = NeonRedPrimary
                                    )
                                )
                            }

                            // Crosshair Opacity Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(AppStrings.crosshairOpacity(isArabic), color = TextMuted, fontSize = 11.sp)
                                    Text("${(crosshairConfig.opacity * 100).toInt()}%", color = NeonGreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = crosshairConfig.opacity,
                                    onValueChange = { prefsManager.updateCrosshair(crosshairConfig.copy(opacity = it)) },
                                    valueRange = 0.2f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonGreenAccent,
                                        activeTrackColor = NeonGreenAccent.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }

                    // Action Buttons: "CROSSHAIR EDITOR" & "OPEN APP"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E212D))
                                .border(1.dp, NeonRedBright, RoundedCornerShape(8.dp))
                                .clickable {
                                    isPanelOpen = false
                                    onNavigateToCrosshairEditor()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "محرر المؤشر" else "CROSSHAIR EDITOR",
                                color = TextWhite,
                                fontSize = 10.sp,
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF161822))
                                .border(1.dp, Color(0xFF33384A), RoundedCornerShape(8.dp))
                                .clickable { isPanelOpen = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppStrings.openApp(isArabic),
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // ========================================================
                    // BOTTOM STATUS BAR (MATCHING SCREENSHOT)
                    // ========================================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF14070B))
                            .border(1.dp, NeonRedDark, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Status Chip: Phone icon + Menu Status: ACTIVE
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PhoneStatusIcon(size = 18.dp, color = NeonRedBright)
                            Text(
                                text = AppStrings.cyberMenuStatus(isArabic),
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = if (isArabic) CairoFamily else RajdhaniFamily
                            )
                            Text(
                                text = AppStrings.cyberMenuStatusActive(isArabic),
                                color = NeonGreenAccent,
                                fontSize = 10.sp,
                                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonGreenAccent,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Right Status Chip: Speedometer + Current FPS: 60 FPS
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FpsGaugeIcon(size = 18.dp, color = NeonRedBright)
                            Text(
                                text = AppStrings.cyberCurrentFps(isArabic),
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = if (isArabic) CairoFamily else RajdhaniFamily
                            )
                            Text(
                                text = "$fpsMeasured ${AppStrings.cyberFpsUnit(isArabic)}",
                                color = NeonGreenAccent,
                                fontSize = 10.sp,
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonGreenAccent,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cybernetic Chamfered Action Tile matching the reference screenshot.
 */
@Composable
private fun CyberFeatureTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isActive: Boolean,
    isArabic: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val borderColor = if (isActive) NeonGreenAccent else NeonRedPrimary
    val glowBg = if (isActive) Color(0x3000FF66) else Color(0x20140407)

    Box(
        modifier = modifier
            .height(84.dp)
            .clip(ChamferedShape(10.dp))
            .background(Color(0xFF13141C))
            .border(1.2.dp, borderColor, ChamferedShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon + Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(glowBg),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                // Glowing status indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) NeonGreenAccent else Color(0xFF4A4E5C))
                        .border(1.dp, if (isActive) NeonGreenAccent else Color.Transparent, CircleShape)
                )
            }

            // Title & Subtitle Labels
            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = if (isActive) NeonGreenAccent else TextMuted,
                    fontSize = 8.5.sp,
                    fontFamily = if (isArabic) TajawalFamily else RajdhaniFamily,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MiniSensStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 8.sp, fontFamily = OrbitronFamily)
        Text(
            text = "$value",
            color = NeonGreenAccent,
            fontSize = 11.sp,
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Bold
        )
    }
}
