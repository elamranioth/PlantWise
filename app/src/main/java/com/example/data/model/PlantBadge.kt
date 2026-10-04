package com.example.data.model

enum class BadgeCategory(val title: String) {
    CARE("Care & Streaks"),
    WATERING("Hydration"),
    SUNLIGHT("Sunlight Tracking"),
    IDENTIFICATION("Plant Recognition"),
    COMMUNITY("Gardening Community"),
    KNOWLEDGE("Botanical Learning")
}

data class PlantBadge(
    val id: String,
    val title: String,
    val description: String,
    val category: BadgeCategory,
    val iconEmoji: String,
    val targetCount: Int,
    val currentCount: Int,
    val isUnlocked: Boolean,
    val xpReward: Int = 50
) {
    val progressPercent: Float
        get() = (currentCount.toFloat() / targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
}
