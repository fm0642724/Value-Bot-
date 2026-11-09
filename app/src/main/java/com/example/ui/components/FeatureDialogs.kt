package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.GamingSurface
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Base Sci-Fi Gaming Modal Dialog.
 */
@Composable
fun CyberDialog(
    title: String,
    onDismissRequest: () -> Unit,
    testTag: String = "cyber_dialog",
    content: @Composable () -> Unit
) {
    val shape = remember {
        ChamferedShape(
            chamferSize = 14.dp,
            chamferTopLeft = true,
            chamferTopRight = true,
            chamferBottomRight = true,
            chamferBottomLeft = true
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .testTag(testTag)
                .fillMaxWidth()
                .clip(shape)
                .background(Color(0xFF111218))
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(NeonRedBright, NeonRedPrimary.copy(alpha = 0.5f), Color(0xFF2B080F))
                    ),
                    shape = shape
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Title & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .testTag("dialog_close_button")
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NeonRedBright
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                content()
            }
        }
    }
}

/**
 * Aim Boost Modal: Real-time Touch & Calibration Suite.
 */
@Composable
fun AimBoostDialog(
    onDismissRequest: () -> Unit
) {
    var isBoosting by remember { mutableStateOf(false) }
    var boostComplete by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    CyberDialog(
        title = "AIM BOOST OPTIMIZER",
        onDismissRequest = onDismissRequest,
        testTag = "aim_boost_dialog"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "AI-Driven Touch Latency & Recoil Calibration",
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = RajdhaniFamily
            )

            // Diagnostic Stat Rows
            StatRow("Touch Sampling Rate", "240 Hz Ultra")
            StatRow("Input Delay Compensation", "1.2 ms")
            StatRow("Headshot Tracking Sensitivity", "+28% Boosted")
            StatRow("Screen Micro-Jitter Filter", "Active")

            Spacer(modifier = Modifier.height(8.dp))

            if (isBoosting) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Calibrating Neural Touch Engine...",
                        color = NeonGreenAccent,
                        fontSize = 13.sp,
                        fontFamily = OrbitronFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = NeonRedBright,
                        trackColor = Color(0xFF22242F)
                    )
                }
            } else if (boostComplete) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonGreenAccent.copy(alpha = 0.15f))
                        .border(1.dp, NeonGreenAccent, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ Aim Boost Applied Successfully!",
                        color = NeonGreenAccent,
                        fontSize = 14.sp,
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    isBoosting = true
                    boostComplete = false
                    coroutineScope.launch {
                        delay(1200)
                        isBoosting = false
                        boostComplete = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_aim_boost_button")
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (boostComplete) "RE-OPTIMIZE AIM" else "BOOST AIM NOW",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Smart Sensitivity Dialog: Sensitivity Sliders.
 */
@Composable
fun SmartSensitivityDialog(
    onDismissRequest: () -> Unit
) {
    var general by remember { mutableFloatStateOf(95f) }
    var redDot by remember { mutableFloatStateOf(100f) }
    var scope2x by remember { mutableFloatStateOf(88f) }
    var scope4x by remember { mutableFloatStateOf(82f) }
    var sniper by remember { mutableFloatStateOf(65f) }
    var dpi by remember { mutableFloatStateOf(480f) }
    var applied by remember { mutableStateOf(false) }

    CyberDialog(
        title = "SMART SENSITIVITY",
        onDismissRequest = onDismissRequest,
        testTag = "smart_sensitivity_dialog"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "AI Recommended Free Fire & BR Pro Player Config",
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = RajdhaniFamily
            )

            SensitivitySlider("General", general, 0f, 100f) { general = it }
            SensitivitySlider("Red Dot Sight", redDot, 0f, 100f) { redDot = it }
            SensitivitySlider("2x Scope", scope2x, 0f, 100f) { scope2x = it }
            SensitivitySlider("4x Scope", scope4x, 0f, 100f) { scope4x = it }
            SensitivitySlider("Sniper Scope", sniper, 0f, 100f) { sniper = it }
            SensitivitySlider("Device DPI", dpi, 320f, 600f, suffix = " DPI") { dpi = it }

            Spacer(modifier = Modifier.height(6.dp))

            if (applied) {
                Text(
                    text = "✓ Configuration Saved to Device Profile",
                    color = NeonGreenAccent,
                    fontSize = 13.sp,
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { applied = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_sensitivity_button")
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "APPLY AI SENSITIVITY",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SensitivitySlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    suffix: String = "%",
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextWhite,
                fontSize = 13.sp,
                fontFamily = RajdhaniFamily,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${value.toInt()}$suffix",
                color = NeonRedBright,
                fontSize = 13.sp,
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = NeonRedBright,
                activeTrackColor = NeonRedPrimary,
                inactiveTrackColor = Color(0xFF282A35)
            )
        )
    }
}

/**
 * Device Analyzer Dialog.
 */
@Composable
fun DeviceAnalyzerDialog(
    onDismissRequest: () -> Unit
) {
    var ramCleaned by remember { mutableStateOf(false) }

    CyberDialog(
        title = "DEVICE ANALYZER",
        onDismissRequest = onDismissRequest,
        testTag = "device_analyzer_dialog"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatRow("SoC Processor", "Snapdragon 8 Gen 3")
            StatRow("GPU Engine", "Adreno 750 (Vulkan 1.3)")
            StatRow("Total Memory", "12 GB LPDDR5X")
            StatRow("Memory In Use", if (ramCleaned) "4.1 GB (Optimized)" else "8.4 GB (Heavy)")
            StatRow("Thermal State", "38.2°C Normal")
            StatRow("Refresh Rate", "120 Hz AMOLED")
            StatRow("Battery Level", "72% (Fast Charging)")

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { ramCleaned = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clean_ram_button")
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (ramCleaned) "RAM OPTIMIZED" else "DEEP CLEAN RAM",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Performance Monitor Dialog.
 */
@Composable
fun PerformanceMonitorDialog(
    onDismissRequest: () -> Unit
) {
    var currentMode by remember { mutableStateOf("Turbo Gamer") }

    CyberDialog(
        title = "PERFORMANCE MONITOR",
        onDismissRequest = onDismissRequest,
        testTag = "performance_monitor_dialog"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatRow("Target Frame Rate", "60 FPS (Locked)")
            StatRow("Network Ping", "18 ms Ultra Low")
            StatRow("Jitter Variation", "± 1.4 ms")
            StatRow("Battery Drain Rate", "4.2% / hr")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Performance Profile Mode:",
                color = TextWhite,
                fontSize = 13.sp,
                fontFamily = RajdhaniFamily,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Battery", "Balanced", "Turbo Gamer").forEach { mode ->
                    val isSelected = currentMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) NeonRedPrimary else Color(0xFF1E2028))
                            .clickable { currentMode = mode }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode,
                            color = if (isSelected) TextWhite else TextMuted,
                            fontSize = 11.sp,
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF161822))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 13.sp,
            fontFamily = RajdhaniFamily,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = NeonGreenAccent,
            fontSize = 13.sp,
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Bold
        )
    }
}
