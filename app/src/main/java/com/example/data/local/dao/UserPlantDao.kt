package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.PlantWithGrowthProgress
import com.example.data.local.entity.UserPlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPlantDao {

    @Query("SELECT * FROM user_plants ORDER BY createdAtTimestamp DESC")
    fun getAllUserPlants(): Flow<List<UserPlantEntity>>

    @Transaction
    @Query("SELECT * FROM user_plants ORDER BY createdAtTimestamp DESC")
    fun getPlantsWithGrowthProgress(): Flow<List<PlantWithGrowthProgress>>

    @Transaction
    @Query("SELECT * FROM user_plants WHERE id = :id")
    fun getPlantWithGrowthProgressById(id: String): Flow<PlantWithGrowthProgress?>

    @Query("SELECT * FROM user_plants WHERE id = :id")
    suspend fun getUserPlantByIdSync(id: String): UserPlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPlant(plant: UserPlantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPlants(plants: List<UserPlantEntity>)

    @Update
    suspend fun updateUserPlant(plant: UserPlantEntity)

    @Delete
    suspend fun deleteUserPlant(plant: UserPlantEntity)

    @Query("DELETE FROM user_plants WHERE id = :id")
    suspend fun deleteUserPlantById(id: String)

    @Query("UPDATE user_plants SET lastWateredTimestamp = :timestamp WHERE id = :id")
    suspend fun updateWateringTimestamp(id: String, timestamp: Long)

    @Query("UPDATE user_plants SET sunlightLevel = :exposure, hoursOfSunlightToday = :hours WHERE id = :id")
    suspend fun updateSunlight(id: String, exposure: String, hours: Float)

    @Query("""
        UPDATE user_plants 
        SET nickname = :nickname, 
            wateringIntervalDays = :intervalDays, 
            sunlightLevel = :sunlight, 
            healthCondition = :health, 
            notes = :notes, 
            potSize = :potSize, 
            heightDescription = :height 
        WHERE id = :id
    """)
    suspend fun updatePlantDetails(
        id: String,
        nickname: String,
        intervalDays: Int,
        sunlight: String,
        health: String,
        notes: String,
        potSize: String,
        height: String
    )

    @Query("SELECT COUNT(*) FROM user_plants")
    fun getUserPlantsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_plants")
    suspend fun getUserPlantsCountSync(): Int
}
