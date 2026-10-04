package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.SunlightExposure
import com.example.data.model.UserPlant

@Entity(tableName = "user_plants")
data class UserPlantEntity(
    @PrimaryKey
    val id: String,
    val plantCatalogId: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val nickname: String,
    val wateringIntervalDays: Int,
    val lastWateredTimestamp: Long,
    val sunlightLevel: String, // SunlightExposure name
    val hoursOfSunlightToday: Float,
    val healthCondition: String = "Thriving",
    val notes: String = "",
    val iconEmoji: String = "🌱",
    val locationRoom: String = "Living Room",
    val potSize: String = "6-inch Ceramic",
    val heightDescription: String = "12 - 18 inches",
    val lastFertilizedTimestamp: Long = System.currentTimeMillis(),
    val createdAtTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): UserPlant {
        val exposure = try {
            SunlightExposure.valueOf(sunlightLevel)
        } catch (_: Exception) {
            SunlightExposure.BRIGHT_INDIRECT
        }

        return UserPlant(
            id = id,
            plantId = plantCatalogId,
            commonName = commonName,
            scientificName = scientificName,
            family = family,
            nickname = nickname,
            wateringIntervalDays = wateringIntervalDays,
            lastWateredTimestamp = lastWateredTimestamp,
            sunlightLevel = exposure,
            hoursOfSunlightToday = hoursOfSunlightToday,
            healthCondition = healthCondition,
            notes = notes,
            iconEmoji = iconEmoji,
            locationRoom = locationRoom,
            potSize = potSize,
            heightDescription = heightDescription,
            lastFertilizedTimestamp = lastFertilizedTimestamp,
            growthMilestones = emptyList() // populated via relation
        )
    }

    companion object {
        fun fromDomainModel(plant: UserPlant): UserPlantEntity {
            return UserPlantEntity(
                id = plant.id,
                plantCatalogId = plant.plantId,
                commonName = plant.commonName,
                scientificName = plant.scientificName,
                family = plant.family,
                nickname = plant.nickname,
                wateringIntervalDays = plant.wateringIntervalDays,
                lastWateredTimestamp = plant.lastWateredTimestamp,
                sunlightLevel = plant.sunlightLevel.name,
                hoursOfSunlightToday = plant.hoursOfSunlightToday,
                healthCondition = plant.healthCondition,
                notes = plant.notes,
                iconEmoji = plant.iconEmoji,
                locationRoom = plant.locationRoom,
                potSize = plant.potSize,
                heightDescription = plant.heightDescription,
                lastFertilizedTimestamp = plant.lastFertilizedTimestamp
            )
        }
    }
}
