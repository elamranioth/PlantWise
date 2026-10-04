package com.example.data.repository

import com.example.data.local.dao.HistoryDao
import com.example.data.local.entity.HistoryEntity
import com.example.data.model.ComprehensivePlantInfo
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val historyDao: HistoryDao
) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun recordPhotoIdentification(plantInfo: ComprehensivePlantInfo, imageUri: String = plantInfo.imageUriOrPath) {
        val entity = HistoryEntity.fromPhotoIdentification(plantInfo, imageUri)
        historyDao.insertHistory(entity)
    }

    suspend fun recordSearchOpen(searchTerm: String, plantInfo: ComprehensivePlantInfo) {
        val entity = HistoryEntity.fromSearch(searchTerm, plantInfo)
        historyDao.insertHistory(entity)
    }

    suspend fun deleteHistory(id: String) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    suspend fun getHistoryById(id: String): HistoryEntity? {
        return historyDao.getHistoryById(id)
    }
}
