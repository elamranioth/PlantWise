package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GrowthProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthProgressDao {

    @Query("SELECT * FROM growth_progress WHERE plant_id = :plantId ORDER BY timestamp DESC")
    fun getProgressForPlant(plantId: String): Flow<List<GrowthProgressEntity>>

    @Query("SELECT * FROM growth_progress WHERE plant_id = :plantId ORDER BY timestamp ASC")
    fun getProgressForPlantChronological(plantId: String): Flow<List<GrowthProgressEntity>>

    @Query("SELECT * FROM growth_progress WHERE plant_id = :plantId ORDER BY timestamp DESC")
    suspend fun getProgressForPlantSync(plantId: String): List<GrowthProgressEntity>

    @Query("SELECT * FROM growth_progress ORDER BY timestamp DESC")
    fun getAllProgressRecords(): Flow<List<GrowthProgressEntity>>

    @Query("SELECT * FROM growth_progress WHERE plant_id = :plantId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestProgress(plantId: String): Flow<GrowthProgressEntity?>

    @Query("SELECT * FROM growth_progress WHERE plant_id = :plantId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestProgressSync(plantId: String): GrowthProgressEntity?

    @Query("SELECT * FROM growth_progress WHERE plant_id = :plantId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getProgressInRange(plantId: String, startTime: Long, endTime: Long): Flow<List<GrowthProgressEntity>>

    @Query("SELECT MAX(height_cm) FROM growth_progress WHERE plant_id = :plantId")
    fun getMaxRecordedHeight(plantId: String): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: GrowthProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressList(progressList: List<GrowthProgressEntity>)

    @Update
    suspend fun updateProgress(progress: GrowthProgressEntity)

    @Delete
    suspend fun deleteProgress(progress: GrowthProgressEntity)

    @Query("DELETE FROM growth_progress WHERE id = :id")
    suspend fun deleteProgressById(id: String)

    @Query("DELETE FROM growth_progress WHERE plant_id = :plantId")
    suspend fun deleteProgressForPlant(plantId: String)

    @Query("SELECT COUNT(*) FROM growth_progress WHERE plant_id = :plantId")
    suspend fun getMilestoneCountForPlant(plantId: String): Int
}
