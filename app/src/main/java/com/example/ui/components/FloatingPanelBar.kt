package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextWhite

/**
 * Floating Panel Toggle Bar with bilingual status badge, label, and switch.
 */
@Composable
fun FloatingPanelBar(
    enabled: Boolean,
    isArabic: Boolean = false,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(54.dp)
            .testTag("floating_panel_status_bar")
            .clip(shape)
            .background(Color(0xFF0F1014))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NeonGreenAccent.copy(alpha = 0.8f),
                        Color(0xFF282A33),
                        if (enabled) NeonGreenAccent.copy(alpha = 0.8f) else Color(0xFF282A33),
                        NeonGreenAccent.copy(alpha = 0.8f)
                    )
                ),
                shape = shape
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon & Label
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkmark circle badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (enabled) NeonGreenAccent else NeonGreenAccent.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Status",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = AppStrings.floatingPanelBarLabel(isArabic),
                    color = TextWhite,
                    fontSize = if (isArabic) 13.5.sp else 14.sp,
                    fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (isArabic) 0.sp else 0.5.sp
                )

                Text(
                    text = if (enabled) AppStrings.enabled(isArabic) else AppStrings.disabled(isArabic),
                    color = if (enabled) NeonGreenAccent else NeonRedBright,
                    fontSize = if (isArabic) 13.5.sp else 14.sp,
                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (isArabic) 0.sp else 0.5.sp
                )
            }

            // Interactive Switch
            CyberSwitch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                testTag = "floating_panel_toggle_switch"
            )
        }
    }
}
