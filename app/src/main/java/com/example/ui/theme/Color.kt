package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// PlantWise Clean, Premium Botanical Color System (User-Specified Design Tokens)
// Calm, fresh, natural, and easy on the eyes.
// =========================================================================

// Main Surfaces
val BotanicalBgPrimary = Color(0xFF101713)          // Main background: very dark botanical green
val BotanicalBgSecondary = Color(0xFF17211B)        // Secondary surface / app bar / header
val BotanicalCard = Color(0xFF1D2921)               // Cards: dark botanical green
val BotanicalSurfaceElevated = Color(0xFF243229)    // Elevated cards / selected surfaces

// Primary Brand & Accents
val BotanicalPrimary = Color(0xFF6FAF78)            // Primary botanical green
val BotanicalPrimaryActive = Color(0xFF79BE82)      // Primary active / CTA green
val BotanicalSoftSage = Color(0xFFA8C3A5)           // Soft sage
val BotanicalLightAccent = Color(0xFFC6D9B9)        // Light botanical accent
val BotanicalCreamAccent = Color(0xFFF2E8D5)        // Warm cream accent (used sparingly)

// Typography Tokens
val BotanicalTextPrimary = Color(0xFFF4F6F2)        // Main text: crisp off-white
val BotanicalTextSecondary = Color(0xFFB6C1B7)      // Secondary text: soft botanical gray
val BotanicalTextMuted = Color(0xFF849187)          // Muted text: low-emphasis labels

// Borders & Dividers
val BotanicalBorder = Color(0xFF314038)             // Subtle botanical borders & dividers

// Status Colors (Restrained accents for small badges, dots, and thin borders)
val BotanicalStatusHealthy = Color(0xFF78B982)      // Healthy / thriving
val BotanicalStatusWater = Color(0xFF72A9B8)        // Water / watering
val BotanicalStatusWarning = Color(0xFFD9A45B)      // Warning / due soon
val BotanicalStatusOverdue = Color(0xFFD97862)      // Overdue / important (terracotta)
val BotanicalStatusNeutral = Color(0xFF8FA79A)      // Neutral information

// Button Design Tokens
val BotanicalButtonPrimaryBg = Color(0xFF72B67B)    // Primary button background
val BotanicalButtonPrimaryText = Color(0xFF0D1710)  // Primary button text

// Navigation Design Tokens
val BotanicalNavInactive = Color(0xFF7E8D82)        // Inactive navigation icons & text
val BotanicalNavActive = Color(0xFF7DBE82)          // Active navigation item

// Welcome Card Gradient
val BotanicalWelcomeGradientStart = Color(0xFF203126)
val BotanicalWelcomeGradientEnd = Color(0xFF263A2D)

// =========================================================================
// Backwards-Compatible Semantic Bindings for existing code
// =========================================================================
val PlantPrimary = BotanicalPrimaryActive
val PlantSecondary = BotanicalSoftSage
val PlantSage = BotanicalSoftSage
val PlantMint = BotanicalSurfaceElevated
val PlantBackground = BotanicalBgPrimary
val PlantCardGreen = BotanicalCard
val PlantMutedText = BotanicalTextMuted

val PlantSurface = BotanicalCard
val PlantOnSurface = BotanicalTextPrimary
val PlantTextSecondary = BotanicalTextSecondary
val PlantSurfaceVariant = BotanicalSurfaceElevated
val PlantBorder = BotanicalBorder

val PlantDarkBackground = BotanicalBgPrimary
val PlantDarkSurface = BotanicalBgSecondary
val PlantDarkCard = BotanicalCard
val PlantDarkText = BotanicalTextPrimary
val PlantDarkTextSecondary = BotanicalTextSecondary

val PlantWaterBlue = BotanicalStatusWater
val PlantWaterBlueLight = Color(0xFF1E2D30)
val PlantSunGold = BotanicalStatusWarning
val PlantSunGoldLight = Color(0xFF2E2A20)
val PlantAlertRed = BotanicalStatusOverdue
val PlantAlertRedLight = Color(0xFF2D1E1B)
val PlantSuccessGreen = BotanicalStatusHealthy
val PlantSuccessGreenLight = Color(0xFF1B2D21)
