package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextMuted

enum class NavTab {
    HOME,
    AIM,
    STATS,
    PROFILES,
    VIP
}

/**
 * Bottom Navigation Bar matching the screenshot.
 * Displays 5 tabs: Home, AIM, Stats, Profiles, VIP with localized text and active glow indicator.
 */
@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    isArabic: Boolean = false,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar")
            .background(Color(0xFF070709))
            .drawBehind {
                val strokeW = 1.5.dp.toPx()
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1F0509),
                            NeonRedPrimary.copy(alpha = 0.8f),
                            NeonRedBright,
                            NeonRedPrimary.copy(alpha = 0.8f),
                            Color(0xFF1F0509)
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeW
                )
            }
            .navigationBarsPadding()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val tabTitle = when (tab) {
                    NavTab.HOME -> AppStrings.navHome(isArabic)
                    NavTab.AIM -> AppStrings.navAim(isArabic)
                    NavTab.STATS -> AppStrings.navStats(isArabic)
                    NavTab.PROFILES -> AppStrings.navProfiles(isArabic)
                    NavTab.VIP -> AppStrings.navVip(isArabic)
                }

                NavBarItem(
                    tab = tab,
                    title = tabTitle,
                    isSelected = isSelected,
                    isArabic = isArabic,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    tab: NavTab,
    title: String,
    isSelected: Boolean,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("nav_item_${tab.name.lowercase()}")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp, color = NeonRedBright),
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val tintColor = if (isSelected) NeonRedBright else TextDarkMuted

        when (tab) {
            NavTab.HOME -> {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = tintColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            NavTab.AIM -> {
                AimReticleIcon(
                    size = 24.dp,
                    color = tintColor
                )
            }
            NavTab.STATS -> {
                PerformanceChartIcon(
                    size = 24.dp,
                    color = tintColor
                )
            }
            NavTab.PROFILES -> {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Profiles",
                    tint = tintColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            NavTab.VIP -> {
                VipCrownIcon(
                    size = 24.dp,
                    color = tintColor
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            color = if (isSelected) NeonRedBright else TextMuted,
            fontSize = if (isArabic) 10.sp else 11.sp,
            fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )

        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                NeonRedBright,
                                Color.Transparent
                            )
                        )
                    )
            )
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}
