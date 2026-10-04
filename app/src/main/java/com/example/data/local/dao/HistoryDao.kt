package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM identification_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM identification_history WHERE id = :id LIMIT 1")
    suspend fun getHistoryById(id: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM identification_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)

    @Query("DELETE FROM identification_history")
    suspend fun clearAllHistory()

    @Query("SELECT COUNT(*) FROM identification_history")
    suspend fun getHistoryCount(): Int
}
