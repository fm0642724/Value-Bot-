package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.AppStrings
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.GamingBackground
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextMuted

/**
 * Hero Section featuring Free Fire character art, background ambiance,
 * and two floating sci-fi HUD cards for Device Status and FPS monitor.
 */
@Composable
fun HeroSection(
    isArabic: Boolean,
    onDeviceStatusClick: () -> Unit,
    onFpsClick: () -> Unit,
    deviceStatus: String = "Excellent",
    fpsValue: String = "60 FPS",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(265.dp)
            .testTag("hero_section")
    ) {
        // Hero Background Art (Character, destroyed city, plane, logos)
        Image(
            painter = painterResource(id = R.drawable.hero_bg),
            contentDescription = "Game Character Hero Graphic",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient fade overlays for seamless dark blending
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GamingBackground.copy(alpha = 0.95f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GamingBackground.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Floating HUD Cards Row overlaid on the character's lower area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left HUD Card: Device Status
            DeviceStatusHudCard(
                status = deviceStatus,
                isArabic = isArabic,
                onClick = onDeviceStatusClick,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Right HUD Card: FPS Monitor
            FpsMonitorHudCard(
                fps = fpsValue,
                isArabic = isArabic,
                onClick = onFpsClick,
                modifier = Modifier.width(135.dp)
            )
        }
    }
}

@Composable
private fun DeviceStatusHudCard(
    status: String,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = remember {
        ChamferedShape(
            chamferSize = 8.dp,
            chamferTopLeft = true,
            chamferTopRight = true,
            chamferBottomRight = true,
            chamferBottomLeft = true
        )
    }

    Box(
        modifier = modifier
            .testTag("device_status_hud_card")
            .clip(shape)
            .background(Color(0xE613141B))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(NeonRedPrimary.copy(alpha = 0.7f), NeonGreenAccent.copy(alpha = 0.8f))
                ),
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = NeonGreenAccent),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            PhoneStatusIcon(
                size = 32.dp,
                color = NeonGreenAccent
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = AppStrings.deviceStatusLabel(isArabic),
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = status,
                        color = NeonGreenAccent,
                        fontSize = if (isArabic) 14.sp else 15.sp,
                        fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Status Verified",
                        tint = NeonGreenAccent,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FpsMonitorHudCard(
    fps: String,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = remember {
        ChamferedShape(
            chamferSize = 8.dp,
            chamferTopLeft = true,
            chamferTopRight = true,
            chamferBottomRight = true,
            chamferBottomLeft = true
        )
    }

    Box(
        modifier = modifier
            .testTag("fps_hud_card")
            .clip(shape)
            .background(Color(0xE613141B))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(NeonRedPrimary.copy(alpha = 0.8f), NeonGreenAccent.copy(alpha = 0.6f))
                ),
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = NeonGreenAccent),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FpsGaugeIcon(
                    size = 28.dp,
                    color = NeonRedPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = AppStrings.fpsLabel(isArabic),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = fps,
                        color = NeonGreenAccent,
                        fontSize = 15.sp,
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Animated neon green ECG waveform
            PulseWaveCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
                color = NeonGreenAccent
            )
        }
    }
}
