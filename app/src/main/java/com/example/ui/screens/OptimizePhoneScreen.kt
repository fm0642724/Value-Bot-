package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.domain.DeviceTelemetry
import com.example.domain.OptimizationRecommendation
import com.example.domain.OptimizationResult
import com.example.localization.AppStrings
import com.example.ui.components.CyberCard
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Real Optimization Screen running live hardware diagnostic scan,
 * performance score, OEM recommendations, and scan history.
 */
@Composable
fun OptimizePhoneScreen(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var currentScanResult by remember { mutableStateOf<OptimizationResult?>(null) }
    val scanHistory by prefsManager.scanHistory.collectAsState()

    fun runScan() {
        isScanning = true
        scanProgress = 0f
        coroutineScope.launch {
            for (i in 1..10) {
                delay(120)
                scanProgress = i / 10f
            }
            val result = DeviceTelemetry.runFullOptimizationScan(context)
            currentScanResult = result
            prefsManager.addOptimizationScan(result)
            isScanning = false
        }
    }

    LaunchedEffect(Unit) {
        if (currentScanResult == null) {
            val latest = scanHistory.firstOrNull()
            if (latest != null) {
                currentScanResult = latest
            } else {
                runScan()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GamingBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonRedBright
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = AppStrings.optimizePhoneTitle(isArabic),
                color = TextWhite,
                fontSize = if (isArabic) 18.sp else 20.sp,
                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Score & Scan Trigger Card
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                chamferSize = 16.dp,
                accentColor = NeonRedBright
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isScanning) {
                        Text(
                            text = AppStrings.scanning(isArabic),
                            color = NeonGreenAccent,
                            fontSize = 13.sp,
                            fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonRedBright,
                            trackColor = Color(0xFF202330)
                        )
                    } else {
                        val score = currentScanResult?.score ?: 88
                        val scoreColor = when {
                            score >= 85 -> NeonGreenAccent
                            score >= 65 -> Color(0xFFFFB800)
                            else -> NeonRedBright
                        }

                        // Score Gauge Ring
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF14080D))
                                .border(3.dp, scoreColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$score",
                                    color = scoreColor,
                                    fontSize = 38.sp,
                                    fontFamily = OrbitronFamily,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "/ 100",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = OrbitronFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = AppStrings.performanceScore(isArabic),
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { runScan() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = TextWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.scanNow(isArabic),
                                fontSize = 13.sp,
                                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Real Diagnostic Metrics Grid
            val res = currentScanResult
            if (res != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricMiniCard("BATTERY", "${res.batteryPct}%", "${res.batteryTempC}°C", modifier = Modifier.weight(1f))
                    MetricMiniCard("RAM FREE", "${res.ramAvailableMb} MB", "of ${res.ramTotalMb} MB", modifier = Modifier.weight(1f))
                    MetricMiniCard("PING", "${res.pingMs} ms", "DNS Socket", modifier = Modifier.weight(1f))
                    MetricMiniCard("STORAGE", "${String.format("%.1f", res.storageFreeGb)} GB", "Free", modifier = Modifier.weight(1f))
                }
            }

            // Checklist of Recommendations with Expandable OEM Guides
            Text(
                text = if (isArabic) "قائمة تحسينات الأداء وتوجيهات الأجهزة (OEM):" else "Recommended Optimizations & OEM Steps:",
                color = TextWhite,
                fontSize = 14.sp,
                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                fontWeight = FontWeight.Bold
            )

            currentScanResult?.recommendations?.forEach { rec ->
                RecommendationExpandableCard(rec = rec, isArabic = isArabic)
            }

            // Scan History List (Last 10)
            if (scanHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = NeonRedBright, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.scanHistory(isArabic),
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                scanHistory.take(5).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF13151E))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val timeStr = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(item.timestamp))
                            Text(text = timeStr, color = TextWhite, fontSize = 12.sp, fontFamily = OrbitronFamily)
                            Text(
                                text = "Ping: ${item.pingMs}ms | Ram: ${item.ramAvailableMb}MB | Bat: ${item.batteryPct}%",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "${item.score} / 100",
                            color = if (item.score >= 80) NeonGreenAccent else Color(0xFFFFB800),
                            fontSize = 15.sp,
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
private fun MetricMiniCard(label: String, val1: String, val2: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF13151E))
            .border(1.dp, Color(0xFF282B3A), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = TextMuted, fontSize = 9.sp, fontFamily = OrbitronFamily)
            Text(text = val1, color = NeonGreenAccent, fontSize = 12.sp, fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold)
            Text(text = val2, color = TextMuted, fontSize = 9.5.sp)
        }
    }
}

@Composable
private fun RecommendationExpandableCard(rec: OptimizationRecommendation, isArabic: Boolean) {
    var expanded by remember { mutableStateOf(false) }

    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        chamferSize = 10.dp,
        accentColor = if (rec.isUrgent) NeonRedBright else NeonGreenAccent
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (rec.isUrgent) Icons.Default.Warning else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (rec.isUrgent) NeonRedBright else NeonGreenAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) rec.titleAr else rec.title,
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isArabic) rec.descriptionAr else rec.description,
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B1D28))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = AppStrings.oemGuide(isArabic),
                                color = NeonGreenAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) rec.oemNoteAr else rec.oemNote,
                                color = TextWhite,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
