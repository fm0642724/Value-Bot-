package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.domain.DeviceHardwareInfo
import com.example.domain.DeviceTelemetry
import com.example.domain.GamingProfile
import com.example.domain.RecommendationResult
import com.example.domain.SensitivityConfig
import com.example.domain.SensitivityEngine
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
 * 3-Step Smart Sensitivity Screen with device detection, transparent formula breakdown,
 * editable result sliders, and export options.
 */
@Composable
fun SmartSensitivityScreen(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    // Auto-detect hardware
    val hwInfo = remember { DeviceTelemetry.getHardwareInfo(context) }

    // Step 1 State: Hardware & Playstyle
    var ramGb by remember { mutableIntStateOf(hwInfo.totalRamGb) }
    var screenInches by remember { mutableFloatStateOf(hwInfo.screenInches) }
    var refreshRateHz by remember { mutableIntStateOf(hwInfo.refreshRateHz) }
    var chipsetClass by remember { mutableStateOf(hwInfo.chipsetClass) }
    var fingerCount by remember { mutableIntStateOf(3) }
    var playFocus by remember { mutableStateOf("Headshot") }

    // Step 2 & 3 State
    var recommendation by remember {
        mutableStateOf(
            SensitivityEngine.calculate(ramGb, screenInches, refreshRateHz, chipsetClass, fingerCount, playFocus)
        )
    }

    // Editable Sliders
    var curGeneral by remember { mutableFloatStateOf(recommendation.config.general.toFloat()) }
    var curRedDot by remember { mutableFloatStateOf(recommendation.config.redDot.toFloat()) }
    var cur2x by remember { mutableFloatStateOf(recommendation.config.scope2x.toFloat()) }
    var cur4x by remember { mutableFloatStateOf(recommendation.config.scope4x.toFloat()) }
    var curSniper by remember { mutableFloatStateOf(recommendation.config.sniperScope.toFloat()) }
    var curFree by remember { mutableFloatStateOf(recommendation.config.freeLook.toFloat()) }
    var curDpi by remember { mutableFloatStateOf(recommendation.config.dpi.toFloat()) }

    var isFormulaExpanded by remember { mutableStateOf(false) }

    fun recalculate() {
        val res = SensitivityEngine.calculate(ramGb, screenInches, refreshRateHz, chipsetClass, fingerCount, playFocus)
        recommendation = res
        curGeneral = res.config.general.toFloat()
        curRedDot = res.config.redDot.toFloat()
        cur2x = res.config.scope2x.toFloat()
        cur4x = res.config.scope4x.toFloat()
        curSniper = res.config.sniperScope.toFloat()
        curFree = res.config.freeLook.toFloat()
        curDpi = res.config.dpi.toFloat()
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
                text = AppStrings.sensitivityHeader(isArabic),
                color = TextWhite,
                fontSize = if (isArabic) 17.sp else 18.sp,
                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP 1 CARD: Hardware & Playstyle
            CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 12.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = AppStrings.step1Title(isArabic),
                        color = NeonRedBright,
                        fontSize = 14.sp,
                        fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                        fontWeight = FontWeight.Bold
                    )

                    // Detected Device chip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF181A24))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${hwInfo.brand} ${hwInfo.model} (Android ${hwInfo.androidVersion})",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto-Read",
                            color = NeonGreenAccent,
                            fontSize = 10.sp,
                            fontFamily = OrbitronFamily
                        )
                    }

                    // RAM selector
                    Text(text = if (isArabic) "ذاكرة الرام (RAM):" else "Device RAM (GB):", color = TextMuted, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(4, 6, 8, 12, 16).forEach { r ->
                            val isSel = ramGb == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonRedPrimary else Color(0xFF1B1D28))
                                    .clickable {
                                        ramGb = r
                                        recalculate()
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "${r}GB", color = if (isSel) TextWhite else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Display Refresh Rate selector
                    Text(text = if (isArabic) "معدل تحديث الشاشة:" else "Screen Refresh Rate (Hz):", color = TextMuted, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(60, 90, 120, 144).forEach { hz ->
                            val isSel = refreshRateHz == hz
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonRedPrimary else Color(0xFF1B1D28))
                                    .clickable {
                                        refreshRateHz = hz
                                        recalculate()
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "${hz}Hz", color = if (isSel) TextWhite else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Chipset Class
                    Text(text = if (isArabic) "فئة المعالج (SoC):" else "Chipset Class:", color = TextMuted, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Low", "Mid", "High", "Flagship").forEach { chip ->
                            val isSel = chipsetClass.equals(chip, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonRedPrimary else Color(0xFF1B1D28))
                                    .clickable {
                                        chipsetClass = chip
                                        recalculate()
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = chip, color = if (isSel) TextWhite else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Finger Claw layout
                    Text(text = AppStrings.clawLayout(isArabic), color = TextMuted, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(2, 3, 4).forEach { f ->
                            val isSel = fingerCount == f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonRedPrimary else Color(0xFF1B1D28))
                                    .clickable {
                                        fingerCount = f
                                        recalculate()
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "$f Fingers", color = if (isSel) TextWhite else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Play Focus
                    Text(text = if (isArabic) "أسلوب اللعب والتركيز:" else "Combat Play Focus:", color = TextMuted, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Headshot", "Close Range", "Long Range", "Balanced").forEach { focus ->
                            val isSel = playFocus == focus
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonRedPrimary else Color(0xFF1B1D28))
                                    .clickable {
                                        playFocus = focus
                                        recalculate()
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = focus.split(" ").first(),
                                    color = if (isSel) TextWhite else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // STEP 2: Transparent Formula Breakdown Accordion
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isFormulaExpanded = !isFormulaExpanded },
                chamferSize = 10.dp,
                accentColor = NeonGreenAccent
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = NeonGreenAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.howCalculated(isArabic),
                                color = NeonGreenAccent,
                                fontSize = 12.5.sp,
                                fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (isFormulaExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = NeonGreenAccent
                        )
                    }

                    AnimatedVisibility(
                        visible = isFormulaExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isArabic) recommendation.breakdown.formulaSummaryAr else recommendation.breakdown.formulaSummary,
                                color = TextWhite,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FormulaLine("General", if (isArabic) recommendation.breakdown.generalExplanationAr else recommendation.breakdown.generalExplanation)
                            FormulaLine("Red Dot", if (isArabic) recommendation.breakdown.redDotExplanationAr else recommendation.breakdown.redDotExplanation)
                            FormulaLine("2X Scope", if (isArabic) recommendation.breakdown.scope2xExplanationAr else recommendation.breakdown.scope2xExplanation)
                            FormulaLine("4X Scope", if (isArabic) recommendation.breakdown.scope4xExplanationAr else recommendation.breakdown.scope4xExplanation)
                            FormulaLine("Sniper", if (isArabic) recommendation.breakdown.sniperExplanationAr else recommendation.breakdown.sniperExplanation)
                            FormulaLine("Free Look", if (isArabic) recommendation.breakdown.freeLookExplanationAr else recommendation.breakdown.freeLookExplanation)
                        }
                    }
                }
            }

            // STEP 3: Results & Editable Sliders
            CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 14.dp, accentColor = NeonRedBright) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.step3Title(isArabic),
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                            fontWeight = FontWeight.Bold
                        )

                        // Reset button
                        IconButton(onClick = { recalculate() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = NeonRedBright)
                        }
                    }

                    // Sliders
                    SensSlider(AppStrings.general(isArabic), curGeneral) { curGeneral = it }
                    SensSlider(AppStrings.redDot(isArabic), curRedDot) { curRedDot = it }
                    SensSlider(AppStrings.scope2x(isArabic), cur2x) { cur2x = it }
                    SensSlider(AppStrings.scope4x(isArabic), cur4x) { cur4x = it }
                    SensSlider(AppStrings.sniper(isArabic), curSniper) { curSniper = it }
                    SensSlider(AppStrings.freeLook(isArabic), curFree) { curFree = it }
                    SensSlider(AppStrings.recommendedDpi(isArabic), curDpi, 360f, 600f, " DPI") { curDpi = it }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Notice note
                    Text(
                        text = AppStrings.sensitivityNote(isArabic),
                        color = NeonGreenAccent,
                        fontSize = 11.5.sp,
                        fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                        lineHeight = 16.sp
                    )

                    // Action Buttons Row (Save, Copy, Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val savedConfig = SensitivityConfig(
                                    general = curGeneral.toInt(),
                                    redDot = curRedDot.toInt(),
                                    scope2x = cur2x.toInt(),
                                    scope4x = cur4x.toInt(),
                                    sniperScope = curSniper.toInt(),
                                    freeLook = curFree.toInt(),
                                    dpi = curDpi.toInt()
                                )
                                val profile = GamingProfile(
                                    id = "sens_" + System.currentTimeMillis(),
                                    name = "$playFocus Pro",
                                    nameAr = if (isArabic) "إعداد $playFocus" else "$playFocus Pro",
                                    isCustom = true,
                                    sensitivity = savedConfig
                                )
                                prefsManager.saveProfile(profile)
                                prefsManager.setActiveProfile(profile.id)
                                Toast.makeText(context, if (isArabic) "تم حفظ وتفعيل الملف بنجاح!" else "Profile saved & set as active!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = AppStrings.saveAsProfile(isArabic), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val text = "Free Fire Sensitivity:\nGeneral: ${curGeneral.toInt()}\nRed Dot: ${curRedDot.toInt()}\n2X: ${cur2x.toInt()}\n4X: ${cur4x.toInt()}\nSniper: ${curSniper.toInt()}\nFree Look: ${curFree.toInt()}\nDPI: ${curDpi.toInt()}"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Sensitivity", text))
                                Toast.makeText(context, if (isArabic) "تم النسخ!" else "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(0.8f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222432)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = AppStrings.copyToClipboard(isArabic), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val text = "🎮 GAME BOOST AI Free Fire Config:\n• General: ${curGeneral.toInt()}\n• Red Dot: ${curRedDot.toInt()}\n• 2X Scope: ${cur2x.toInt()}\n• 4X Scope: ${cur4x.toInt()}\n• Sniper: ${curSniper.toInt()}\n• Free Look: ${curFree.toInt()}\n• DPI: ${curDpi.toInt()}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Sensitivity"))
                            },
                            modifier = Modifier
                                .weight(0.7f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222432)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormulaLine(label: String, desc: String) {
    Column {
        Text(text = label, color = NeonRedBright, fontSize = 11.sp, fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold)
        Text(text = desc, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun SensSlider(
    label: String,
    value: Float,
    min: Float = 0f,
    max: Float = 100f,
    suffix: String = "%",
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextWhite, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "${value.toInt()}$suffix", color = NeonRedBright, fontSize = 12.5.sp, fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold)
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
