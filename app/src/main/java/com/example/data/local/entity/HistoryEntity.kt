package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ComprehensivePlantInfo

@Entity(tableName = "identification_history")
data class HistoryEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "PHOTO_IDENTIFICATION" or "SEARCH"
    val timestamp: Long,
    val plantName: String,
    val scientificName: String,
    val searchTerm: String = "",
    val imageUriOrPath: String = "",
    val resultJson: String,
    val confidence: Int = 95
) {
    fun parsePlantInfo(): ComprehensivePlantInfo? {
        return ComprehensivePlantInfo.fromJsonString(resultJson)
    }

    companion object {
        const val TYPE_PHOTO = "PHOTO_IDENTIFICATION"
        const val TYPE_SEARCH = "SEARCH"

        fun fromPhotoIdentification(
            plantInfo: ComprehensivePlantInfo,
            imageUri: String = plantInfo.imageUriOrPath
        ): HistoryEntity {
            return HistoryEntity(
                id = "hist_${System.currentTimeMillis()}_${(1000..9999).random()}",
                type = TYPE_PHOTO,
                timestamp = System.currentTimeMillis(),
                plantName = plantInfo.commonName,
                scientificName = plantInfo.scientificName,
                searchTerm = "",
                imageUriOrPath = imageUri,
                resultJson = plantInfo.toJsonString(),
                confidence = plantInfo.confidenceScore
            )
        }

        fun fromSearch(
            searchTerm: String,
            plantInfo: ComprehensivePlantInfo
        ): HistoryEntity {
            return HistoryEntity(
                id = "hist_${System.currentTimeMillis()}_${(1000..9999).random()}",
                type = TYPE_SEARCH,
                timestamp = System.currentTimeMillis(),
                plantName = plantInfo.commonName,
                scientificName = plantInfo.scientificName,
                searchTerm = searchTerm,
                imageUriOrPath = plantInfo.imageUriOrPath,
                resultJson = plantInfo.toJsonString(),
                confidence = plantInfo.confidenceScore
            )
        }
    }
}
