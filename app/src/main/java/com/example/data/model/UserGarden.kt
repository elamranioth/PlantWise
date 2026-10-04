package com.example.data.model

data class GrowthMilestone(
    val id: String,
    val dateFormatted: String,
    val title: String,
    val note: String,
    val healthStatus: String = "Thriving",
    val heightNote: String = "",
    val heightCm: Float? = null,
    val leafCount: Int? = null,
    val growthStage: String = "Vegetative",
    val photoUri: String? = null
)

data class UserPlant(
    val id: String,
    val plantId: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val nickname: String,
    val wateringIntervalDays: Int,
    val lastWateredTimestamp: Long,
    val sunlightLevel: SunlightExposure,
    val hoursOfSunlightToday: Float,
    val healthCondition: String = "Thriving",
    val notes: String = "",
    val iconEmoji: String = "🌱",
    val locationRoom: String = "Living Room",
    val potSize: String = "6-inch Ceramic",
    val heightDescription: String = "12 - 18 inches",
    val lastFertilizedTimestamp: Long = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000),
    val growthMilestones: List<GrowthMilestone> = emptyList()
) {
    fun daysUntilWatering(): Int {
        val now = System.currentTimeMillis()
        val elapsedMillis = now - lastWateredTimestamp
        val elapsedDays = (elapsedMillis / (1000 * 60 * 60 * 24)).toInt()
        return wateringIntervalDays - elapsedDays
    }

    fun isWateringDue(): Boolean {
        return daysUntilWatering() <= 0
    }

    fun wateringStatusLabel(): String {
        val remaining = daysUntilWatering()
        return when {
            remaining < 0 -> "Overdue by ${-remaining}d ⚠️"
            remaining == 0 -> "Due Today! 💧"
            remaining == 1 -> "Due Tomorrow"
            else -> "In $remaining days"
        }
    }

    fun lastWateredRelative(): String {
        val now = System.currentTimeMillis()
        val daysAgo = ((now - lastWateredTimestamp) / (1000 * 60 * 60 * 24)).toInt()
        return when (daysAgo) {
            0 -> "Watered today"
            1 -> "Watered yesterday"
            else -> "Watered $daysAgo days ago"
        }
    }
}

data class SunlightCheckResult(
    val selectedExposure: SunlightExposure,
    val plantRecommended: SunlightExposure,
    val isCompatible: Boolean,
    val advice: String
)
