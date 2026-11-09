package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GamingColorScheme = darkColorScheme(
    primary = NeonRedPrimary,
    onPrimary = TextWhite,
    primaryContainer = NeonRedDark,
    onPrimaryContainer = TextWhite,
    secondary = NeonGreenAccent,
    onSecondary = GamingBackground,
    secondaryContainer = NeonGreenDark,
    onSecondaryContainer = NeonGreenBright,
    background = GamingBackground,
    onBackground = TextWhite,
    surface = GamingSurface,
    onSurface = TextWhite,
    surfaceVariant = GamingSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = GamingBorderRedDim,
    outlineVariant = GamingBorder
)

@Composable
fun MyApplicationTheme(
    isArabic: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GamingColorScheme,
        typography = getAppTypography(isArabic),
        content = content
    )
}
