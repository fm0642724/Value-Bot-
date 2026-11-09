package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * 2x2 Grid of cyberpunk gaming feature cards.
 * Fixes text wrapping for "PERFORMANCE MONITOR" with tighter letter-spacing and auto-fit.
 */
@Composable
fun FeatureGrid(
    isArabic: Boolean = false,
    onSmartSensitivityClick: () -> Unit,
    onDeviceAnalyzerClick: () -> Unit,
    onFloatingPanelClick: () -> Unit,
    onPerformanceMonitorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Smart Sensitivity & Device Analyzer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeatureCard(
                title = AppStrings.smartSensitivityTitle(isArabic),
                subtitle = AppStrings.smartSensitivitySubtitle(isArabic),
                testTag = "smart_sensitivity_card",
                isArabic = isArabic,
                onClick = onSmartSensitivityClick,
                icon = { SmartBrainIcon(size = 36.dp, color = NeonRedBright) },
                modifier = Modifier.weight(1f)
            )

            FeatureCard(
                title = AppStrings.deviceAnalyzerTitle(isArabic),
                subtitle = AppStrings.deviceAnalyzerSubtitle(isArabic),
                testTag = "device_analyzer_card",
                isArabic = isArabic,
                onClick = onDeviceAnalyzerClick,
                icon = { DeviceChipIcon(size = 36.dp, color = NeonRedBright) },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Floating Panel & Performance Monitor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeatureCard(
                title = AppStrings.floatingPanelTitle(isArabic),
                subtitle = AppStrings.floatingPanelSubtitle(isArabic),
                testTag = "floating_panel_card",
                isArabic = isArabic,
                onClick = onFloatingPanelClick,
                icon = { FloatingPanelIcon(size = 36.dp, color = NeonRedBright) },
                modifier = Modifier.weight(1f)
            )

            FeatureCard(
                title = if (isArabic) "مراقب\nالأداء" else "PERFORMANCE\nMONITOR",
                subtitle = AppStrings.performanceMonitorSubtitle(isArabic),
                testTag = "performance_monitor_card",
                isArabic = isArabic,
                fontSize = 11.sp, // Tighter font size prevents awkward word wrapping
                onClick = onPerformanceMonitorClick,
                icon = { PerformanceChartIcon(size = 36.dp, color = NeonRedBright) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    isArabic: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.5.sp,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier
            .height(86.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = NeonRedBright),
                onClick = onClick
            ),
        chamferSize = 10.dp,
        accentColor = NeonRedPrimary,
        testTag = testTag
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                icon()

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = title,
                        color = TextWhite,
                        fontSize = if (isArabic) 12.sp else fontSize,
                        fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp,
                        letterSpacing = 0.sp,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = if (isArabic) 9.5.sp else 10.sp,
                        fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp,
                        maxLines = 1
                    )
                }
            }

            // Directional arrow
            Text(
                text = if (isArabic) "‹" else "›",
                color = NeonRedBright,
                fontSize = 24.sp,
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}
