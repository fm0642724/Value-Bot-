package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.domain.CrosshairConfig
import com.example.domain.CrosshairStyle
import com.example.localization.AppStrings
import com.example.ui.components.CrosshairCanvas
import com.example.ui.components.CyberCard
import com.example.ui.components.parseHexColor
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedDark
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlin.math.roundToInt

/**
 * Full Crosshair Customizer Screen with live draggable preview, 12+ styles,
 * color palette, nudge buttons, and persistent storage.
 */
@Composable
fun CrosshairEditorScreen(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    val initialConfig = remember { prefsManager.crosshair.value }

    var style by remember { mutableStateOf(initialConfig.style) }
    var colorHex by remember { mutableStateOf(initialConfig.colorHex) }
    var size by remember { mutableFloatStateOf(initialConfig.size) }
    var thickness by remember { mutableFloatStateOf(initialConfig.thickness) }
    var centerGap by remember { mutableFloatStateOf(initialConfig.centerGap) }
    var outline by remember { mutableStateOf(initialConfig.outline) }
    var outlineColorHex by remember { mutableStateOf(initialConfig.outlineColorHex) }
    var opacity by remember { mutableFloatStateOf(initialConfig.opacity) }
    var rotation by remember { mutableFloatStateOf(initialConfig.rotation) }
    var posXPercent by remember { mutableFloatStateOf(initialConfig.posXPercent) }
    var posYPercent by remember { mutableFloatStateOf(initialConfig.posYPercent) }
    var isOverlayEnabled by remember { mutableStateOf(initialConfig.enabled) }

    val currentConfig = CrosshairConfig(
        id = initialConfig.id,
        name = initialConfig.name,
        style = style,
        colorHex = colorHex,
        size = size,
        thickness = thickness,
        centerGap = centerGap,
        outline = outline,
        outlineColorHex = outlineColorHex,
        opacity = opacity,
        rotation = rotation,
        posXPercent = posXPercent,
        posYPercent = posYPercent,
        enabled = isOverlayEnabled
    )

    fun saveConfig() {
        prefsManager.updateCrosshair(currentConfig)
        Toast.makeText(context, if (isArabic) "تم حفظ وتفعيل مؤشر التصويب!" else "Crosshair configuration saved!", Toast.LENGTH_SHORT).show()
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
                text = AppStrings.crosshairHeader(isArabic),
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
            // Live Preview Canvas on Mock Game Background
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                chamferSize = 14.dp,
                accentColor = NeonRedBright
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0C0D12))
                ) {
                    val previewWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                    val previewHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(previewWidth, previewHeight) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    posXPercent = (posXPercent + (dragAmount.x / previewWidth) * 100f).coerceIn(10f, 90f)
                                    posYPercent = (posYPercent + (dragAmount.y / previewHeight) * 100f).coerceIn(10f, 90f)
                                }
                            }
                    ) {
                        // Crosshair positioned on preview
                        val crosshairX = (previewWidth * (posXPercent / 100f)) - (size * 1.5f)
                        val crosshairY = (previewHeight * (posYPercent / 100f)) - (size * 1.5f)

                    Box(
                        modifier = Modifier.offset {
                            IntOffset(crosshairX.roundToInt(), crosshairY.roundToInt())
                        }
                    ) {
                        CrosshairCanvas(config = currentConfig)
                    }

                    // Floating Controls inside Preview: Center button and Nudge buttons
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xD0181A24))
                                .clickable {
                                    posXPercent = 50f
                                    posYPercent = 50f
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = AppStrings.centerButton(isArabic), color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "X: ${posXPercent.toInt()}% Y: ${posYPercent.toInt()}%",
                            color = NeonGreenAccent,
                            fontSize = 11.sp,
                            fontFamily = OrbitronFamily,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }

                    // 1px Nudge controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        NudgeBtn(Icons.Default.KeyboardArrowLeft) { posXPercent = (posXPercent - 0.5f).coerceAtLeast(10f) }
                        NudgeBtn(Icons.Default.KeyboardArrowUp) { posYPercent = (posYPercent - 0.5f).coerceAtLeast(10f) }
                        NudgeBtn(Icons.Default.KeyboardArrowDown) { posYPercent = (posYPercent + 0.5f).coerceAtMost(90f) }
                        NudgeBtn(Icons.Default.KeyboardArrowRight) { posXPercent = (posXPercent + 0.5f).coerceAtMost(90f) }
                    }
                }
            }
        }

            // Enable in-app tap-through overlay toggle
            CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 10.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.enableInAppOverlay(isArabic),
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "رسم المؤشر فوق كافة شاشات التطبيق" else "Draws crosshair above every screen",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isOverlayEnabled,
                        onCheckedChange = {
                            isOverlayEnabled = it
                            prefsManager.setCrosshairEnabled(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreenAccent,
                            checkedTrackColor = NeonGreenAccent.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // 12 Crosshair Designs Selector
            CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 12.dp) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AppStrings.crosshairStyle(isArabic),
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val styles = CrosshairStyle.values()
                    val rows = styles.toList().chunked(4)

                    rows.forEach { rowStyles ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowStyles.forEach { st ->
                                val isSel = style == st
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) NeonRedPrimary else Color(0xFF191B24))
                                        .border(
                                            1.dp,
                                            if (isSel) NeonRedBright else Color(0xFF2E3142),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { style = st },
                                    contentAlignment = Alignment.Center
                                ) {
                                    CrosshairCanvas(
                                        config = currentConfig.copy(style = st, size = 26f, thickness = 2f, centerGap = 4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Color Palette Selector
            CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 10.dp) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = AppStrings.colorPalette(isArabic), color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    val colors = listOf(
                        "#FF1E3C", "#00FF66", "#00F0FF", "#FFFF00",
                        "#FF00E5", "#FFFFFF", "#FF8800", "#7928CA"
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        colors.forEach { hex ->
                            val isSel = colorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(hex))
                                    .border(
                                        if (isSel) 3.dp else 1.dp,
                                        if (isSel) Color.White else Color.Black,
                                        CircleShape
                                    )
                                    .clickable { colorHex = hex }
                            )
                        }
                    }
                }
            }

            // Sliders: Size, Thickness, Center Gap, Opacity, Rotation
            CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 12.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EditSlider(AppStrings.crosshairSize(isArabic), size, 16f, 72f, " dp") { size = it }
                    EditSlider(AppStrings.thickness(isArabic), thickness, 1f, 8f, " dp") { thickness = it }
                    EditSlider(AppStrings.centerGap(isArabic), centerGap, 0f, 24f, " dp") { centerGap = it }
                    EditSlider(AppStrings.opacity(isArabic), opacity * 100f, 20f, 100f, "%") { opacity = it / 100f }
                    EditSlider(AppStrings.rotation(isArabic), rotation, 0f, 180f, "°") { rotation = it }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(AppStrings.outlineToggle(isArabic), color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = outline,
                            onCheckedChange = { outline = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonRedBright, checkedTrackColor = NeonRedDark)
                        )
                    }
                }
            }

            // Save Preset Button
            Button(
                onClick = { saveConfig() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = TextWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "حفظ إعدادات المؤشر" else "SAVE CROSSHAIR CONFIG",
                    fontSize = 14.sp,
                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NudgeBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xD01E202B))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = TextWhite, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EditSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    suffix: String = "",
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = "${value.toInt()}$suffix", color = NeonRedBright, fontSize = 12.sp, fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = NeonRedBright,
                activeTrackColor = NeonRedPrimary,
                inactiveTrackColor = Color(0xFF262836)
            )
        )
    }
}
