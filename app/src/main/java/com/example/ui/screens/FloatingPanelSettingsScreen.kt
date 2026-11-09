package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.domain.FloatingPanelSettings
import com.example.localization.AppStrings
import com.example.ui.components.AimReticleIcon
import com.example.ui.components.CyberCard
import com.example.ui.components.parseHexColor
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Floating Panel Settings Screen with button size, opacity, color, edge snap,
 * reset position, and test button.
 */
@Composable
fun FloatingPanelSettingsScreen(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    val initialSettings by prefsManager.floatingSettings.collectAsState()

    var buttonSizePx by remember { mutableIntStateOf(initialSettings.buttonSizePx) }
    var opacity by remember { mutableFloatStateOf(initialSettings.opacity) }
    var colorHex by remember { mutableStateOf(initialSettings.colorHex) }
    var edgeSnap by remember { mutableStateOf(initialSettings.edgeSnap) }
    var idleAutoFade by remember { mutableStateOf(initialSettings.idleAutoFade) }

    fun save() {
        val updated = initialSettings.copy(
            buttonSizePx = buttonSizePx,
            opacity = opacity,
            colorHex = colorHex,
            edgeSnap = edgeSnap,
            idleAutoFade = idleAutoFade
        )
        prefsManager.updateFloatingSettings(updated)
        Toast.makeText(context, if (isArabic) "تم حفظ إعدادات اللوحة العائمة!" else "Floating panel settings saved!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GamingBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Header
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
                text = if (isArabic) "إعدادات اللوحة العائمة" else "FLOATING PANEL SETTINGS",
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
            // Live Preview of the Floating Button
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                chamferSize = 14.dp,
                accentColor = NeonRedBright
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(buttonSizePx.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF140306).copy(alpha = opacity))
                            .border(2.dp, parseHexColor(colorHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AimReticleIcon(size = (buttonSizePx * 0.65f).dp, color = parseHexColor(colorHex))
                    }

                    Text(
                        text = if (isArabic) "معاينة الزر العائم الحالية" else "LIVE BUTTON PREVIEW",
                        color = TextMuted,
                        fontSize = 10.5.sp,
                        fontFamily = OrbitronFamily,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                    )
                }
            }

            // Size Slider: 32 - 96 px
            CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 10.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = if (isArabic) "حجم الزر العائم:" else "Button Size:", color = TextWhite, fontSize = 13.sp)
                        Text(text = "${buttonSizePx}px", color = NeonRedBright, fontSize = 13.sp, fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = buttonSizePx.toFloat(),
                        onValueChange = { buttonSizePx = it.toInt() },
                        valueRange = 32f..96f,
                        colors = SliderDefaults.colors(thumbColor = NeonRedBright, activeTrackColor = NeonRedPrimary)
                    )
                }
            }

            // Opacity Slider: 20% - 100%
            CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 10.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = if (isArabic) "شفافية الزر العائم:" else "Button Opacity:", color = TextWhite, fontSize = 13.sp)
                        Text(text = "${(opacity * 100).toInt()}%", color = NeonRedBright, fontSize = 13.sp, fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = opacity * 100f,
                        onValueChange = { opacity = it / 100f },
                        valueRange = 20f..100f,
                        colors = SliderDefaults.colors(thumbColor = NeonRedBright, activeTrackColor = NeonRedPrimary)
                    )
                }
            }

            // Edge Snap Toggle & Idle Auto-Fade Toggle
            CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 12.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = if (isArabic) "المحاذاة التلقائية لحافة الشاشة (Edge-Snap)" else "Snap to Screen Edges", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = if (isArabic) "يقفز الزر تلقائياً لأقرب طرف شاشة بعد السحب" else "Animates to nearest left/right border upon release", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = edgeSnap,
                            onCheckedChange = { edgeSnap = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreenAccent, checkedTrackColor = NeonGreenAccent.copy(alpha = 0.4f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = if (isArabic) "تعتيم الزر عند عدم اللمس (Idle Fade)" else "Idle Auto-Fade", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = if (isArabic) "يقلل الشفافية تلقائياً عند عدم الاستخدام" else "Reduces visibility when not interacting", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = idleAutoFade,
                            onCheckedChange = { idleAutoFade = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreenAccent, checkedTrackColor = NeonGreenAccent.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // Action Buttons: Reset Position & Test Button & Save
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        prefsManager.setFloatingPosition(88f, 35f)
                        Toast.makeText(context, if (isArabic) "تمت إعادة ضبط موضع الزر!" else "Position reset to right edge!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E212E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isArabic) "إعادة تعيين الموضع" else "Reset Pos", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        prefsManager.setFloatingPanelEnabled(true)
                        prefsManager.setFloatingPanelMinimized(false)
                        Toast.makeText(context, if (isArabic) "تم إظهار الزر العائم للاختبار!" else "Floating button activated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreenAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isArabic) "اختبار الزر" else "Test Button", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { save() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isArabic) "حفظ التغييرات" else "SAVE FLOATING SETTINGS",
                    fontSize = 13.sp,
                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
