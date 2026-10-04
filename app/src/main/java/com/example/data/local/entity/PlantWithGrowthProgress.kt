package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.model.UserPlant

data class PlantWithGrowthProgress(
    @Embedded
    val plant: UserPlantEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "plant_id"
    )
    val growthHistory: List<GrowthProgressEntity>
) {
    fun toDomainUserPlant(): UserPlant {
        val basePlant = plant.toDomainModel()
        // Sort milestones newest first
        val sortedMilestones = growthHistory
            .sortedByDescending { it.timestamp }
            .map { it.toDomainMilestone() }

        return basePlant.copy(growthMilestones = sortedMilestones)
    }
}
