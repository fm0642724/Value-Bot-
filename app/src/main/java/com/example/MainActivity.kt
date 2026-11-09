package com.example

import android.os.Bundle
import android.view.Choreographer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.PreferencesManager
import com.example.domain.DeviceTelemetry
import com.example.ui.components.BottomNavBar
import com.example.ui.components.FloatingOverlayLayer
import com.example.ui.components.NavTab
import com.example.ui.screens.AimScreen
import com.example.ui.screens.CrosshairEditorScreen
import com.example.ui.screens.FloatingPanelSettingsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OptimizePhoneScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.SmartSensitivityScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.VipScreen
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.MyApplicationTheme

enum class AppSubScreen {
    NONE,
    SMART_SENSITIVITY,
    OPTIMIZE_PHONE,
    CROSSHAIR_EDITOR,
    FLOATING_SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefsManager = PreferencesManager.getInstance(this)

        setContent {
            val isArabic by prefsManager.isArabic.collectAsState()
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(isArabic = isArabic) {
                    MainAppContainer(prefsManager = prefsManager, isArabic = isArabic)
                }
            }
        }
    }
}

@Composable
fun MainAppContainer(
    prefsManager: PreferencesManager,
    isArabic: Boolean
) {
    val context = LocalContext.current

    var selectedTab by rememberSaveable { mutableStateOf(NavTab.HOME) }
    var activeSubScreen by rememberSaveable { mutableStateOf(AppSubScreen.NONE) }

    val floatingSettings by prefsManager.floatingSettings.collectAsState()
    val profiles by prefsManager.profiles.collectAsState()
    val activeProfileId by prefsManager.activeProfileId.collectAsState()
    val crosshairConfig by prefsManager.crosshair.collectAsState()

    // Real measured FPS counter using Choreographer
    var measuredFps by remember { mutableIntStateOf(60) }
    DisposableEffect(Unit) {
        var frameCount = 0
        var lastTimeNanos = System.nanoTime()

        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                val elapsedNanos = frameTimeNanos - lastTimeNanos
                if (elapsedNanos >= 1_000_000_000L) { // 1 second
                    measuredFps = ((frameCount * 1_000_000_000L) / elapsedNanos).toInt().coerceIn(45, 144)
                    frameCount = 0
                    lastTimeNanos = frameTimeNanos
                }
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(callback)

        onDispose {
            Choreographer.getInstance().removeFrameCallback(callback)
        }
    }

    val (batteryPct, _) = remember { DeviceTelemetry.getBatteryMetrics(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GamingBackground)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(GamingBackground),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                // Bottom Nav Bar is shown when not in a sub-screen
                if (activeSubScreen == AppSubScreen.NONE) {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        isArabic = isArabic,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(GamingBackground)
            ) {
                when (activeSubScreen) {
                    AppSubScreen.SMART_SENSITIVITY -> {
                        SmartSensitivityScreen(
                            prefsManager = prefsManager,
                            isArabic = isArabic,
                            onNavigateBack = { activeSubScreen = AppSubScreen.NONE }
                        )
                    }
                    AppSubScreen.OPTIMIZE_PHONE -> {
                        OptimizePhoneScreen(
                            prefsManager = prefsManager,
                            isArabic = isArabic,
                            onNavigateBack = { activeSubScreen = AppSubScreen.NONE }
                        )
                    }
                    AppSubScreen.CROSSHAIR_EDITOR -> {
                        CrosshairEditorScreen(
                            prefsManager = prefsManager,
                            isArabic = isArabic,
                            onNavigateBack = { activeSubScreen = AppSubScreen.NONE }
                        )
                    }
                    AppSubScreen.FLOATING_SETTINGS -> {
                        FloatingPanelSettingsScreen(
                            prefsManager = prefsManager,
                            isArabic = isArabic,
                            onNavigateBack = { activeSubScreen = AppSubScreen.NONE }
                        )
                    }
                    AppSubScreen.NONE -> {
                        when (selectedTab) {
                            NavTab.HOME -> {
                                HomeScreen(
                                    prefsManager = prefsManager,
                                    isArabic = isArabic,
                                    onNavigateToSmartSensitivity = { activeSubScreen = AppSubScreen.SMART_SENSITIVITY },
                                    onNavigateToOptimizePhone = { activeSubScreen = AppSubScreen.OPTIMIZE_PHONE },
                                    onNavigateToCrosshairEditor = { activeSubScreen = AppSubScreen.CROSSHAIR_EDITOR },
                                    onNavigateToFloatingSettings = { activeSubScreen = AppSubScreen.FLOATING_SETTINGS },
                                    fpsMeasured = measuredFps
                                )
                            }
                            NavTab.AIM -> {
                                AimScreen(
                                    prefsManager = prefsManager,
                                    isArabic = isArabic,
                                    onNavigateHome = { selectedTab = NavTab.HOME }
                                )
                            }
                            NavTab.STATS -> {
                                StatsScreen(
                                    prefsManager = prefsManager,
                                    isArabic = isArabic,
                                    onNavigateHome = { selectedTab = NavTab.HOME }
                                )
                            }
                            NavTab.PROFILES -> {
                                ProfilesScreen(
                                    prefsManager = prefsManager,
                                    isArabic = isArabic,
                                    onNavigateHome = { selectedTab = NavTab.HOME }
                                )
                            }
                            NavTab.VIP -> {
                                VipScreen(
                                    isArabic = isArabic,
                                    onNavigateHome = { selectedTab = NavTab.HOME }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top-Level In-App Floating Overlay (Draggable button & panel + Tap-through Crosshair)
        FloatingOverlayLayer(
            prefsManager = prefsManager,
            isArabic = isArabic,
            profiles = profiles,
            activeProfileId = activeProfileId,
            crosshairConfig = crosshairConfig,
            settings = floatingSettings,
            batteryPct = batteryPct,
            fpsMeasured = measuredFps,
            onNavigateToCrosshairEditor = { activeSubScreen = AppSubScreen.CROSSHAIR_EDITOR }
        )
    }
}
