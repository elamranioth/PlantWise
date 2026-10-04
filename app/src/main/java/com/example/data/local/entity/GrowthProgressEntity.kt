package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.GrowthMilestone

@Entity(
    tableName = "growth_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserPlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plant_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["plant_id"]),
        Index(value = ["timestamp"])
    ]
)
data class GrowthProgressEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "plant_id")
    val plantId: String,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "date_formatted")
    val dateFormatted: String,
    val title: String,
    val note: String,
    @ColumnInfo(name = "height_cm")
    val heightCm: Float? = null,
    @ColumnInfo(name = "height_note")
    val heightNote: String = "",
    @ColumnInfo(name = "leaf_count")
    val leafCount: Int? = null,
    @ColumnInfo(name = "growth_stage")
    val growthStage: String = "Vegetative",
    @ColumnInfo(name = "health_status")
    val healthStatus: String = "Thriving",
    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null
) {
    fun toDomainMilestone(): GrowthMilestone {
        return GrowthMilestone(
            id = id,
            dateFormatted = dateFormatted,
            title = title,
            note = note,
            healthStatus = healthStatus,
            heightNote = heightNote.ifBlank { heightCm?.let { "$it cm" } ?: "" },
            heightCm = heightCm,
            leafCount = leafCount,
            growthStage = growthStage,
            photoUri = photoUri
        )
    }

    companion object {
        fun fromDomainMilestone(plantId: String, milestone: GrowthMilestone, timestamp: Long = System.currentTimeMillis()): GrowthProgressEntity {
            return GrowthProgressEntity(
                id = milestone.id,
                plantId = plantId,
                timestamp = timestamp,
                dateFormatted = milestone.dateFormatted,
                title = milestone.title,
                note = milestone.note,
                heightCm = milestone.heightCm,
                heightNote = milestone.heightNote,
                leafCount = milestone.leafCount,
                growthStage = milestone.growthStage,
                healthStatus = milestone.healthStatus,
                photoUri = milestone.photoUri
            )
        }
    }
}
