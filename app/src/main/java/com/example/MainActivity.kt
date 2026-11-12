package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Choreographer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.PreferencesManager
import com.example.domain.DeviceTelemetry
import com.example.overlay.OverlayService
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
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

enum class AppSubScreen {
    NONE,
    SMART_SENSITIVITY,
    OPTIMIZE_PHONE,
    CROSSHAIR_EDITOR,
    FLOATING_SETTINGS
}

class MainActivity : ComponentActivity() {
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefsManager = PreferencesManager.getInstance(this)

        if (prefsManager.floatingSettings.value.enabled && OverlayService.isPermissionGranted(this)) {
            OverlayService.start(this)
        }

        setContent {
            val isArabic by prefsManager.isArabic.collectAsState()
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(isArabic = isArabic) {
                    MainAppContainer(
                        prefsManager = prefsManager,
                        isArabic = isArabic,
                        onRequestOverlayPermission = {
                            startActivity(OverlayService.requestPermissionIntent(this))
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check overlay permission when returning from Android Settings:
        if (prefsManager.floatingSettings.value.enabled) {
            if (OverlayService.isPermissionGranted(this)) {
                OverlayService.start(this)
            }
        }
    }
}

@Composable
fun MainAppContainer(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    onRequestOverlayPermission: () -> Unit = {}
) {
    val context = LocalContext.current

    var selectedTab by rememberSaveable { mutableStateOf(NavTab.HOME) }
    var activeSubScreen by rememberSaveable { mutableStateOf(AppSubScreen.NONE) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    val floatingSettings by prefsManager.floatingSettings.collectAsState()
    val profiles by prefsManager.profiles.collectAsState()
    val activeProfileId by prefsManager.activeProfileId.collectAsState()
    val crosshairConfig by prefsManager.crosshair.collectAsState()

    // Android 13+ Notification Permission Launcher
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission response handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Toggle Floating Panel with real permission verification
    val handleToggleFloatingPanel: (Boolean) -> Unit = { enable ->
        if (enable) {
            if (!OverlayService.isPermissionGranted(context)) {
                showOverlayPermissionDialog = true
            } else {
                prefsManager.setFloatingPanelEnabled(true)
                OverlayService.start(context)
            }
        } else {
            prefsManager.setFloatingPanelEnabled(false)
            OverlayService.stop(context)
        }
    }

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

    // Explanation Dialog for SYSTEM_ALERT_WINDOW
    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = {
                Text(
                    text = if (isArabic) "إذن العرض فوق التطبيقات الأخرى مطلوب" else "Overlay Permission Required",
                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                    fontWeight = FontWeight.Bold,
                    color = NeonRedBright
                )
            },
            text = {
                Text(
                    text = if (isArabic)
                        "يتطلب تطبيق GAME BOOST AI منح إذن 'العرض فوق التطبيقات الأخرى' (SYSTEM_ALERT_WINDOW) حتى يتمكن من رسم زر اللوحة العائمة وعلامة التصويب التكتيكية فوق شاشة لعبة Free Fire وأثناء المباريات.\n\nاضغط 'منح الإذن' للتفعيل من إعدادات النظام."
                    else
                        "GAME BOOST AI requires the 'Display over other apps' (SYSTEM_ALERT_WINDOW) permission so the floating control panel and tactical crosshair overlay can appear over Free Fire during matches.\n\nTap 'Grant Permission' to open Android system settings.",
                    fontFamily = if (isArabic) CairoFamily else null,
                    fontSize = 13.sp,
                    color = TextWhite
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverlayPermissionDialog = false
                        onRequestOverlayPermission()
                    }
                ) {
                    Text(
                        text = if (isArabic) "منح الإذن" else "Grant Permission",
                        color = NeonGreenAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text(
                        text = if (isArabic) "إلغاء" else "Cancel",
                        color = TextMuted
                    )
                }
            },
            containerColor = GamingBackground
        )
    }

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
                                    fpsMeasured = measuredFps,
                                    onToggleFloatingPanel = handleToggleFloatingPanel
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

        // Top-Level In-App Floating Overlay (Live preview within the app & emulator)
        // If native system overlay permission is granted, OverlayService renders the real
        // WindowManager overlay above everything (including MainActivity and Free Fire).
        // Only render the in-app Compose simulation layer when system overlay permission is not granted.
        if (!OverlayService.isPermissionGranted(context)) {
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
}
