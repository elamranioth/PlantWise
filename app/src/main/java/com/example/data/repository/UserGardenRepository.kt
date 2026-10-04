package com.example.data.repository

import com.example.data.local.dao.GrowthProgressDao
import com.example.data.local.dao.UserPlantDao
import com.example.data.local.entity.GrowthProgressEntity
import com.example.data.local.entity.UserPlantEntity
import com.example.data.model.GrowthMilestone
import com.example.data.model.SunlightExposure
import com.example.data.model.UserPlant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository pattern implementation for managing User Plants and local Growth Progress tracking
 * using Room Database DAOs.
 */
class UserGardenRepository(
    private val userPlantDao: UserPlantDao,
    private val growthProgressDao: GrowthProgressDao
) {
    /**
     * Observes all user plants combined with their local growth progress records.
     * Reactively emits whenever a plant is added, watered, edited, or a growth milestone is logged.
     */
    val plantsWithGrowthProgress: Flow<List<UserPlant>> =
        userPlantDao.getPlantsWithGrowthProgress().map { list ->
            list.map { it.toDomainUserPlant() }
        }

    /**
     * Observes a specific user plant with its full growth history.
     */
    fun getPlantWithGrowthProgress(id: String): Flow<UserPlant?> =
        userPlantDao.getPlantWithGrowthProgressById(id).map { it?.toDomainUserPlant() }

    /**
     * Observes growth progress milestones for a specific plant in descending order.
     */
    fun getGrowthProgress(plantId: String): Flow<List<GrowthMilestone>> =
        growthProgressDao.getProgressForPlant(plantId).map { list ->
            list.map { it.toDomainMilestone() }
        }

    /**
     * Observes chronological growth progress milestones (ascending) for timeline and chart views.
     */
    fun getGrowthProgressChronological(plantId: String): Flow<List<GrowthMilestone>> =
        growthProgressDao.getProgressForPlantChronological(plantId).map { list ->
            list.map { it.toDomainMilestone() }
        }

    /**
     * Inserts a new user plant into local SQLite via Room.
     */
    suspend fun insertUserPlant(plant: UserPlant) {
        val entity = UserPlantEntity.fromDomainModel(plant)
        userPlantDao.insertUserPlant(entity)
        if (plant.growthMilestones.isNotEmpty()) {
            val milestoneEntities = plant.growthMilestones.map {
                GrowthProgressEntity.fromDomainMilestone(plant.id, it)
            }
            growthProgressDao.insertProgressList(milestoneEntities)
        }
    }

    /**
     * Updates an existing user plant details.
     */
    suspend fun updateUserPlant(plant: UserPlant) {
        userPlantDao.updatePlantDetails(
            id = plant.id,
            nickname = plant.nickname,
            intervalDays = plant.wateringIntervalDays,
            sunlight = plant.sunlightLevel.name,
            health = plant.healthCondition,
            notes = plant.notes,
            potSize = plant.potSize,
            height = plant.heightDescription
        )
    }

    /**
     * Deletes a user plant and its associated growth progress records (cascade).
     */
    suspend fun deleteUserPlant(plantId: String) {
        userPlantDao.deleteUserPlantById(plantId)
    }

    /**
     * Updates the last watered timestamp for a user plant.
     */
    suspend fun updateWatering(plantId: String, timestamp: Long = System.currentTimeMillis()) {
        userPlantDao.updateWateringTimestamp(plantId, timestamp)
    }

    /**
     * Updates sunlight exposure for a user plant.
     */
    suspend fun updateSunlight(plantId: String, exposure: SunlightExposure, hours: Float) {
        userPlantDao.updateSunlight(plantId, exposure.name, hours)
    }

    /**
     * Records a new growth progress measurement or milestone locally.
     */
    suspend fun recordGrowthProgress(
        plantId: String,
        milestone: GrowthMilestone,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val entity = GrowthProgressEntity.fromDomainMilestone(plantId, milestone, timestamp)
        growthProgressDao.insertProgress(entity)
    }

    /**
     * Deletes a specific growth milestone by ID.
     */
    suspend fun deleteGrowthProgress(milestoneId: String) {
        growthProgressDao.deleteProgressById(milestoneId)
    }

    /**
     * Checks if the database has any plants, and returns count.
     */
    suspend fun getUserPlantsCount(): Int = userPlantDao.getUserPlantsCountSync()
}
