package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.ui.components.CyberCard
import com.example.ui.components.VipCrownIcon
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * VIP Tab Screen displaying tournament benefits, crown header,
 * and a disabled "Coming Soon" button with no fake purchases.
 */
@Composable
fun VipScreen(
    isArabic: Boolean,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateHome() }

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
                text = AppStrings.vipTitle(isArabic),
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
            // Crown Header Card
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                chamferSize = 16.dp,
                accentColor = Color(0xFFFFD700)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VipCrownIcon(size = 54.dp, color = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "VIP TOURNAMENT PRO PASS",
                        color = Color(0xFFFFD700),
                        fontSize = 17.sp,
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) "حزمة المزايا الحصرية للاعبي البطولات التنافسية"
                        else "Exclusive enhancements tailored for competitive esport players",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = if (isArabic) CairoFamily else RajdhaniFamily
                    )
                }
            }

            // Benefits Checklist
            val benefits = listOf(
                Pair(
                    "Ultra-Low Latency Touch Driver",
                    if (isArabic) "تعريف اللمس فائق السرعة بزمن وصول فوري" else "Zero-buffer touch interrupt priority"
                ),
                Pair(
                    "AI Micro-Jitter Suppression",
                    if (isArabic) "فلتر كبح الارتجاف الدقيق للطلقة أثناء السحب" else "Removes finger tremble during drag shots"
                ),
                Pair(
                    "120 FPS Framerate Lock",
                    if (isArabic) "تثبيت معدل 120 إطاراً في الثانية دون هبوط" else "Eliminates thermal frame throttling drops"
                ),
                Pair(
                    "12 Exclusive Holographic Crosshairs",
                    if (isArabic) "12 نمط مؤشر تصويب هولوجرافي مخصص" else "Tournament legal glowing HUD reticles"
                ),
                Pair(
                    "Pro Esports Profile Cloud Sync",
                    if (isArabic) "مزامنة سحابية غير محدودة لملفات اللاعبين" else "Unlimited backup of all custom sensitivities"
                )
            )

            benefits.forEach { (title, subtitle) ->
                CyberCard(modifier = Modifier.fillMaxWidth(), chamferSize = 10.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(NeonGreenAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = subtitle, color = TextMuted, fontSize = 11.5.sp)
                        }
                    }
                }
            }

            // Disabled "Coming Soon" Button (No fake purchases!)
            Button(
                onClick = { /* Disabled - no fake purchases */ },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = Color(0xFF202330),
                    disabledContentColor = Color(0xFF888B9E)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = AppStrings.vipComingSoon(isArabic),
                    fontSize = 13.sp,
                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
