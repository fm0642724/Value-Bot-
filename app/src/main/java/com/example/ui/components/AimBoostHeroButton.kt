package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextWhite

/**
 * Main Hero Action Button: "AIM BOOST" (Train • Improve • Win)
 * Hexagonal chamfered design with intense glowing red energy borders and RTL mirroring.
 */
@Composable
fun AimBoostHeroButton(
    isArabic: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aim_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val shape = remember {
        ChamferedShape(
            chamferSize = 18.dp,
            chamferTopLeft = true,
            chamferTopRight = true,
            chamferBottomRight = true,
            chamferBottomLeft = true
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("aim_boost_hero_button")
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1F0408),
                        Color(0xFF0F0204),
                        Color(0xFF1F0408)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NeonRedPrimary.copy(alpha = pulseGlow),
                        NeonRedBright,
                        NeonRedPrimary.copy(alpha = pulseGlow)
                    )
                ),
                shape = shape
            )
            .drawBehind {
                val w = size.width
                val h = size.height
                val c = 18.dp.toPx()
                val sparkColor = NeonRedBright.copy(alpha = pulseGlow)
                val strokeW = 3.dp.toPx()

                // Corner sparks
                drawLine(sparkColor, Offset(0f, c), Offset(c, 0f), strokeW)
                drawLine(sparkColor, Offset(w - c, 0f), Offset(w, c), strokeW)
                drawLine(sparkColor, Offset(w, h - c), Offset(w - c, h), strokeW)
                drawLine(sparkColor, Offset(c, h), Offset(0f, h - c), strokeW)
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = NeonRedBright),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reticle + Titles
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AimReticleIcon(
                    size = 46.dp,
                    color = NeonRedBright
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = AppStrings.aimBoostTitle(isArabic),
                        color = TextWhite,
                        fontSize = if (isArabic) 20.sp else 24.sp,
                        fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                        fontWeight = FontWeight.Black,
                        fontStyle = if (isArabic) FontStyle.Normal else FontStyle.Italic,
                        letterSpacing = if (isArabic) 0.sp else 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = AppStrings.aimBoostSubtitle(isArabic),
                        color = TextWhite.copy(alpha = 0.85f),
                        fontSize = if (isArabic) 12.sp else 12.sp,
                        fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = if (isArabic) 0.sp else 0.8.sp
                    )
                }
            }

            // Directional Chevron: "»" in LTR, "«" in RTL
            Text(
                text = if (isArabic) "«" else "»",
                color = NeonRedBright,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = OrbitronFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
