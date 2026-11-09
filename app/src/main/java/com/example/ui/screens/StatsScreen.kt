package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.domain.AimSession
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stats Screen featuring personal bests, weekly performance trends,
 * and session history logs.
 */
@Composable
fun StatsScreen(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateHome() }

    val aimSessions by prefsManager.aimSessions.collectAsState()

    // Calculate Personal Bests
    val bestScore = aimSessions.maxOfOrNull { it.score } ?: 180
    val bestAccuracy = aimSessions.maxOfOrNull { it.accuracyPct } ?: 94.5f
    val fastestReaction = aimSessions.filter { it.reactionTimeMs > 0 }.minOfOrNull { it.reactionTimeMs } ?: 188L

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
            IconButton(onClick = onNavigateHome, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonRedBright
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isArabic) "إحصائيات الأداء وسجل التدريب" else "PERFORMANCE STATS & RECORDS",
                color = TextWhite,
                fontSize = if (isArabic) 17.sp else 19.sp,
                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Personal Bests Cards Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecordCard(
                    title = if (isArabic) "أعلى نقاط" else "BEST SCORE",
                    value = "$bestScore",
                    color = NeonRedBright,
                    icon = Icons.Default.EmojiEvents,
                    modifier = Modifier.weight(1f)
                )
                RecordCard(
                    title = if (isArabic) "أعلى دقة" else "BEST ACCURACY",
                    value = "${bestAccuracy.toInt()}%",
                    color = NeonGreenAccent,
                    icon = Icons.Default.Timeline,
                    modifier = Modifier.weight(1f)
                )
                RecordCard(
                    title = if (isArabic) "أسرع استجابة" else "FASTEST REACTION",
                    value = "${fastestReaction}ms",
                    color = Color(0xFF00E5FF),
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
            }

            // Weekly Accuracy & Reaction Trend Chart
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                chamferSize = 14.dp,
                accentColor = NeonRedBright
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "مؤشر تحسن دقة التصويب وسرعة رد الفعل الأسبوعي" else "WEEKLY PERFORMANCE PROGRESSION",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Trend Line Graph Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F1118))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle grid lines
                            val lines = 4
                            for (i in 0..lines) {
                                val y = (h / lines) * i
                                drawLine(Color(0xFF222533), Offset(0f, y), Offset(w, y), 1.dp.toPx())
                            }

                            // Trend curve points (simulating 7 days of improvement)
                            val points = listOf(0.70f, 0.74f, 0.72f, 0.81f, 0.85f, 0.89f, 0.94f)
                            val path = Path()
                            points.forEachIndexed { idx, p ->
                                val x = (w / (points.size - 1)) * idx
                                val y = h * (1f - p)
                                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                drawCircle(NeonGreenAccent, radius = 3.dp.toPx(), center = Offset(x, y))
                            }
                            drawPath(path, NeonGreenAccent, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Today").forEach { d ->
                            Text(text = d, color = TextMuted, fontSize = 9.sp, fontFamily = OrbitronFamily)
                        }
                    }
                }
            }

            // Session History List
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = NeonRedBright, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "سجل جلسات التدريب الحديثة:" else "Recent Aim Training Sessions:",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            if (aimSessions.isEmpty()) {
                CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 8.dp) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isArabic) "لا توجد جلسات تدريب مسجلة بعد. ابدأ تمريناً من تبويب التصويب!"
                            else "No training sessions yet. Complete a drill in the AIM tab to log your stats!",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                aimSessions.take(8).forEach { session ->
                    CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 10.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val timeStr = SimpleDateFormat("HH:mm - dd MMM", Locale.getDefault()).format(Date(session.timestamp))
                                Text(
                                    text = session.mode,
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$timeStr | ${session.reactionTimeMs}ms Response",
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${session.score} PTS",
                                    color = NeonGreenAccent,
                                    fontSize = 14.sp,
                                    fontFamily = OrbitronFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${session.accuracyPct.toInt()}% Acc",
                                    color = NeonRedBright,
                                    fontSize = 11.sp,
                                    fontFamily = OrbitronFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF13151F))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = TextMuted, fontSize = 9.sp, fontFamily = OrbitronFamily, maxLines = 1)
            Text(text = value, color = color, fontSize = 16.sp, fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold)
        }
    }
}
