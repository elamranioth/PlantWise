package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BotanicalColorScheme = darkColorScheme(
    primary = BotanicalPrimaryActive,           // #79BE82 - Primary active/CTA green
    onPrimary = BotanicalButtonPrimaryText,     // #0D1710 - Dark text on green CTA
    primaryContainer = BotanicalSurfaceElevated,// #243229 - Elevated surface / active selection
    onPrimaryContainer = BotanicalLightAccent,  // #C6D9B9 - Light botanical accent
    secondary = BotanicalSoftSage,              // #A8C3A5 - Soft sage
    onSecondary = BotanicalButtonPrimaryText,   // #0D1710
    secondaryContainer = BotanicalCard,         // #1D2921 - Cards
    onSecondaryContainer = BotanicalTextPrimary,// #F4F6F2 - Crisp main text
    tertiary = BotanicalStatusWater,            // #72A9B8 - Water / hydration
    onTertiary = BotanicalButtonPrimaryText,
    background = BotanicalBgPrimary,            // #101713 - Main background: very dark botanical green
    onBackground = BotanicalTextPrimary,        // #F4F6F2
    surface = BotanicalBgSecondary,             // #17211B - Secondary surface
    onSurface = BotanicalTextPrimary,           // #F4F6F2
    surfaceVariant = BotanicalCard,             // #1D2921 - Cards
    onSurfaceVariant = BotanicalTextSecondary,  // #B6C1B7 - Secondary text
    outline = BotanicalBorder                   // #314038 - Subtle divider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BotanicalColorScheme,
        typography = Typography,
        content = content
    )
}
