package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppStrings
import com.example.ui.components.AimBoostDialog
import com.example.ui.components.AimBoostHeroButton
import com.example.ui.components.AppHeader
import com.example.ui.components.CrosshairEditorIcon
import com.example.ui.components.DeviceAnalyzerDialog
import com.example.ui.components.FeatureGrid
import com.example.ui.components.FloatingPanelBar
import com.example.ui.components.FullWidthActionCard
import com.example.ui.components.HeroSection
import com.example.ui.components.LightningBoltIcon
import com.example.ui.components.PerformanceMonitorDialog
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.NeonRedBright

/**
 * Main Home Screen matching the approved design layout, buttons, colors, and spacing.
 * Includes Sections 3 and 4, quick language switch, real device metrics, and full navigation.
 */
@Composable
fun HomeScreen(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    onNavigateToSmartSensitivity: () -> Unit,
    onNavigateToOptimizePhone: () -> Unit,
    onNavigateToCrosshairEditor: () -> Unit,
    onNavigateToFloatingSettings: () -> Unit,
    fpsMeasured: Int = 60,
    onToggleFloatingPanel: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val floatingSettings by prefsManager.floatingSettings.collectAsState()
    val scanHistory by prefsManager.scanHistory.collectAsState()

    // Dialog state controllers
    var showAimBoostDialog by remember { mutableStateOf(false) }
    var showDeviceAnalyzerDialog by remember { mutableStateOf(false) }
    var showPerformanceMonitorDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Dynamic Device Status computed from latest Performance Score
    val latestScore = scanHistory.firstOrNull()?.score ?: 92
    val deviceStatusText = when {
        latestScore >= 85 -> AppStrings.deviceStatusExcellent(isArabic)
        latestScore >= 70 -> AppStrings.deviceStatusGood(isArabic)
        latestScore >= 50 -> AppStrings.deviceStatusAverage(isArabic)
        else -> AppStrings.deviceStatusWeak(isArabic)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GamingBackground)
            .testTag("home_screen_root"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp), // Space for bottom navigation bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top App Header (Logo, Game Boost AI Title, Quick Language Switch, Settings & Info)
            AppHeader(
                isArabic = isArabic,
                onToggleLanguage = { prefsManager.toggleLanguage() },
                onSettingsClick = { showSettingsDialog = true },
                onInfoClick = { showInfoDialog = true }
            )

            // 2. Hero Section (Character art, background ambiance, floating HUD cards)
            HeroSection(
                isArabic = isArabic,
                onDeviceStatusClick = { showDeviceAnalyzerDialog = true },
                onFpsClick = { showPerformanceMonitorDialog = true },
                deviceStatus = deviceStatusText,
                fpsValue = "$fpsMeasured FPS"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Main Action Button: "AIM BOOST" (Train • Improve • Win)
            AimBoostHeroButton(
                isArabic = isArabic,
                onClick = { showAimBoostDialog = true }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. 2x2 Feature Grid (Smart Sensitivity, Device Analyzer, Floating Panel, Performance Monitor)
            FeatureGrid(
                isArabic = isArabic,
                onSmartSensitivityClick = onNavigateToSmartSensitivity,
                onDeviceAnalyzerClick = { showDeviceAnalyzerDialog = true },
                onFloatingPanelClick = onNavigateToFloatingSettings,
                onPerformanceMonitorClick = { showPerformanceMonitorDialog = true }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Floating Panel Toggle Bar (Status badge, label, and interactive switch)
            FloatingPanelBar(
                enabled = floatingSettings.enabled,
                isArabic = isArabic,
                onEnabledChange = {
                    onToggleFloatingPanel?.invoke(it) ?: prefsManager.setFloatingPanelEnabled(it)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 3: OPTIMIZE PHONE (Full-width neon card)
            FullWidthActionCard(
                title = AppStrings.optimizePhoneTitle(isArabic),
                subtitle = AppStrings.optimizePhoneSubtitle(isArabic),
                testTag = "optimize_phone_card",
                isArabic = isArabic,
                onClick = onNavigateToOptimizePhone,
                icon = { LightningBoltIcon(size = 38.dp, color = NeonRedBright) },
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 4: CROSSHAIR EDITOR (Full-width neon card)
            FullWidthActionCard(
                title = AppStrings.crosshairEditorTitle(isArabic),
                subtitle = AppStrings.crosshairEditorSubtitle(isArabic),
                testTag = "crosshair_editor_card",
                isArabic = isArabic,
                onClick = onNavigateToCrosshairEditor,
                icon = { CrosshairEditorIcon(size = 38.dp, color = NeonRedBright) },
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Active Interactive Dialogs
        if (showAimBoostDialog) {
            AimBoostDialog(onDismissRequest = { showAimBoostDialog = false })
        }

        if (showDeviceAnalyzerDialog) {
            DeviceAnalyzerDialog(onDismissRequest = { showDeviceAnalyzerDialog = false })
        }

        if (showPerformanceMonitorDialog) {
            PerformanceMonitorDialog(onDismissRequest = { showPerformanceMonitorDialog = false })
        }

        if (showSettingsDialog) {
            SettingsDialog(
                prefsManager = prefsManager,
                isArabic = isArabic,
                onDismissRequest = { showSettingsDialog = false }
            )
        }

        if (showInfoDialog) {
            InfoDialog(
                isArabic = isArabic,
                onDismissRequest = { showInfoDialog = false }
            )
        }
    }
}
