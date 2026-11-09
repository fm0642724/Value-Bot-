package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.domain.GamingProfile
import com.example.domain.HudLayout
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

/**
 * Profiles Management & HUD Layout Assistant Screen.
 */
@Composable
fun ProfilesScreen(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateHome() }
    val context = LocalContext.current

    val profiles by prefsManager.profiles.collectAsState()
    val activeProfileId by prefsManager.activeProfileId.collectAsState()

    var showHudAssistant by remember { mutableStateOf(false) }

    // HUD Assistant Layout parameters
    var clawFingers by remember { mutableIntStateOf(3) }
    var fireBtnSize by remember { mutableFloatStateOf(54f) }
    var joystickSize by remember { mutableFloatStateOf(45f) }
    var glooWallSize by remember { mutableFloatStateOf(65f) }

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
            IconButton(onClick = onNavigateHome, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonRedBright
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = AppStrings.profilesHeader(isArabic),
                color = TextWhite,
                fontSize = if (isArabic) 17.sp else 19.sp,
                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HUD Layout Assistant Toggle Button
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showHudAssistant = !showHudAssistant },
                chamferSize = 12.dp,
                accentColor = NeonGreenAccent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.hudAssistantTitle(isArabic),
                            color = NeonGreenAccent,
                            fontSize = 14.sp,
                            fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "حساب وتوزيع أزرار الشاشة (2/3/4 أصابع) مع معاينة فورية"
                            else "Claw button sizes & recommendations with phone preview",
                            color = TextMuted,
                            fontSize = 11.5.sp
                        )
                    }
                    Text(
                        text = if (showHudAssistant) "▲" else "▼",
                        color = NeonGreenAccent,
                        fontSize = 16.sp
                    )
                }
            }

            // HUD Assistant Section
            AnimatedVisibility(visible = showHudAssistant) {
                CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 14.dp) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Fingers Selector
                        Text(text = AppStrings.clawLayout(isArabic), color = TextWhite, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(2, 3, 4).forEach { f ->
                                val isSel = clawFingers == f
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) NeonRedPrimary else Color(0xFF1E212D))
                                        .clickable {
                                            clawFingers = f
                                            when (f) {
                                                2 -> { fireBtnSize = 50f; joystickSize = 50f; glooWallSize = 60f }
                                                3 -> { fireBtnSize = 54f; joystickSize = 45f; glooWallSize = 68f }
                                                4 -> { fireBtnSize = 58f; joystickSize = 40f; glooWallSize = 75f }
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "$f Fingers", color = if (isSel) TextWhite else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Phone-Shaped Visual Preview
                        Text(
                            text = if (isArabic) "معاينة توزيع أزرار الشاشة (Phone Preview):" else "Phone Screen HUD Preview:",
                            color = TextMuted,
                            fontSize = 11.sp
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0B0D14))
                                .border(1.5.dp, Color(0xFF2C3042), RoundedCornerShape(12.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Left Joystick
                                val joyX = w * 0.20f
                                val joyY = h * 0.70f
                                val joyR = (joystickSize / 100f) * (h * 0.28f)
                                drawCircle(Color(0xFF445577).copy(alpha = 0.5f), radius = joyR, center = Offset(joyX, joyY))
                                drawCircle(Color.White.copy(alpha = 0.8f), radius = joyR * 0.35f, center = Offset(joyX, joyY))

                                // Gloo Wall Button
                                val glooX = w * 0.78f
                                val glooY = h * 0.40f
                                val glooR = (glooWallSize / 100f) * (h * 0.24f)
                                drawCircle(Color(0xFF00AAFF).copy(alpha = 0.5f), radius = glooR, center = Offset(glooX, glooY))

                                // Fire Button
                                val fireX = w * 0.82f
                                val fireY = h * 0.74f
                                val fireR = (fireBtnSize / 100f) * (h * 0.28f)
                                drawCircle(NeonRedBright.copy(alpha = 0.6f), radius = fireR, center = Offset(fireX, fireY), style = Stroke(3.dp.toPx()))
                                drawCircle(NeonRedPrimary.copy(alpha = 0.4f), radius = fireR, center = Offset(fireX, fireY))
                            }

                            Text("Joystick", color = TextWhite, fontSize = 9.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 12.dp))
                            Text("Fire Button", color = NeonRedBright, fontSize = 9.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 12.dp))
                            Text("Gloo Wall", color = Color(0xFF00AAFF), fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp))
                        }

                        // Sliders for sizes
                        HudSlider(AppStrings.fireButtonSize(isArabic), fireBtnSize) { fireBtnSize = it }
                        HudSlider(AppStrings.joystickSize(isArabic), joystickSize) { joystickSize = it }
                        HudSlider(AppStrings.glooWallSize(isArabic), glooWallSize) { glooWallSize = it }
                    }
                }
            }

            // Profiles List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "قائمة ملفات التكوين النشطة:" else "Gaming Profiles Configurations:",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        val newId = "custom_" + System.currentTimeMillis()
                        val newProfile = GamingProfile(
                            id = newId,
                            name = "Custom Preset ${profiles.size + 1}",
                            nameAr = if (isArabic) "ملف مخصص ${profiles.size + 1}" else "Custom Preset ${profiles.size + 1}",
                            isCustom = true
                        )
                        prefsManager.saveProfile(newProfile)
                        prefsManager.setActiveProfile(newId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E212E)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = NeonRedBright)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.createNewProfile(isArabic), fontSize = 10.5.sp, color = TextWhite)
                }
            }

            // Profiles Cards
            profiles.forEach { profile ->
                val isSelected = profile.id == activeProfileId
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    chamferSize = 12.dp,
                    accentColor = if (isSelected) NeonRedBright else NeonGreenAccent
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) profile.nameAr else profile.name,
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "General: ${profile.sensitivity.general} | Red Dot: ${profile.sensitivity.redDot} | DPI: ${profile.sensitivity.dpi}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreenAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Active", tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Actions Row: Set Active, Duplicate, Delete
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!isSelected) {
                                Button(
                                    onClick = { prefsManager.setActiveProfile(profile.id) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(AppStrings.setActive(isArabic), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { prefsManager.duplicateProfile(profile.id) },
                                modifier = Modifier.height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222534)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.duplicate(isArabic), fontSize = 10.sp)
                            }

                            if (profile.isCustom) {
                                Button(
                                    onClick = { prefsManager.deleteProfile(profile.id) },
                                    modifier = Modifier.height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38151A)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = NeonRedBright, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HudSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, color = TextWhite, fontSize = 12.sp)
            Text(text = "${value.toInt()}%", color = NeonGreenAccent, fontSize = 12.sp, fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 30f..100f,
            colors = SliderDefaults.colors(thumbColor = NeonGreenAccent, activeTrackColor = NeonGreenAccent.copy(alpha = 0.8f))
        )
    }
}
