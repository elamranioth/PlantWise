package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.example.data.local.dao.GrowthProgressDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.UserPlantDao
import com.example.data.local.entity.GrowthProgressEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.UserPlantEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserPlantEntity::class,
        GrowthProgressEntity::class,
        HistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PlantWiseDatabase : RoomDatabase() {

    abstract fun userPlantDao(): UserPlantDao
    abstract fun growthProgressDao(): GrowthProgressDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: PlantWiseDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `identification_history` (
                        `id` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `plantName` TEXT NOT NULL,
                        `scientificName` TEXT NOT NULL,
                        `searchTerm` TEXT NOT NULL,
                        `imageUriOrPath` TEXT NOT NULL,
                        `resultJson` TEXT NOT NULL,
                        `confidence` INTEGER NOT NULL DEFAULT 95,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): PlantWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlantWiseDatabase::class.java,
                    "plantwise_garden.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .addCallback(PlantWiseDatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PlantWiseDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-seed initial sample user plants and growth progress tracking milestones
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                val plantDao = database.userPlantDao()
                val progressDao = database.growthProgressDao()

                val now = System.currentTimeMillis()
                val oneDayMillis = 24L * 60 * 60 * 1000

                val initialPlants = listOf(
                    UserPlantEntity(
                        id = "up_1",
                        plantCatalogId = "monstera_deliciosa",
                        commonName = "Monstera Deliciosa",
                        scientificName = "Monstera deliciosa Liebm.",
                        family = "Araceae (Arum family)",
                        nickname = "Monty",
                        wateringIntervalDays = 7,
                        lastWateredTimestamp = now - (6 * oneDayMillis),
                        sunlightLevel = "BRIGHT_INDIRECT",
                        hoursOfSunlightToday = 5.5f,
                        healthCondition = "Thriving & Lush",
                        notes = "New leaf unraveling with 4 fenestrations!",
                        iconEmoji = "🪴",
                        locationRoom = "Living Room",
                        potSize = "8-inch Terracotta",
                        heightDescription = "45 cm",
                        createdAtTimestamp = now - (30 * oneDayMillis)
                    ),
                    UserPlantEntity(
                        id = "up_2",
                        plantCatalogId = "holy_basil_tulsi",
                        commonName = "Holy Basil (Tulsi)",
                        scientificName = "Ocimum tenuiflorum L.",
                        family = "Lamiaceae (Mint family)",
                        nickname = "Tulsi Devi",
                        wateringIntervalDays = 3,
                        lastWateredTimestamp = now - (3 * oneDayMillis),
                        sunlightLevel = "DIRECT_SUN",
                        hoursOfSunlightToday = 6.0f,
                        healthCondition = "Fragrant & Healthy",
                        notes = "Pinched flower buds yesterday for bushy branching.",
                        iconEmoji = "🌱",
                        locationRoom = "Balcony Garden",
                        potSize = "6-inch Clay Pot",
                        heightDescription = "25 cm",
                        createdAtTimestamp = now - (20 * oneDayMillis)
                    ),
                    UserPlantEntity(
                        id = "up_3",
                        plantCatalogId = "snake_plant",
                        commonName = "Snake Plant",
                        scientificName = "Dracaena trifasciata (Prain) Mabb.",
                        family = "Asparagaceae (Asparagus family)",
                        nickname = "Sancho",
                        wateringIntervalDays = 18,
                        lastWateredTimestamp = now - (5 * oneDayMillis),
                        sunlightLevel = "LOW_LIGHT",
                        hoursOfSunlightToday = 3.0f,
                        healthCondition = "Thriving & Indestructible",
                        notes = "Produces fresh pup at the soil boundary!",
                        iconEmoji = "🪴",
                        locationRoom = "Bedroom Office",
                        potSize = "7-inch Ceramic Planter",
                        heightDescription = "60 cm",
                        createdAtTimestamp = now - (45 * oneDayMillis)
                    )
                )

                plantDao.insertUserPlants(initialPlants)

                // Initial growth milestones tracking progress
                val initialMilestones = listOf(
                    GrowthProgressEntity(
                        id = "milestone_1",
                        plantId = "up_1",
                        timestamp = now - (2 * oneDayMillis),
                        dateFormatted = "2 days ago",
                        title = "Phenomenal Leaf Fenestration",
                        note = "Fifth leaf opened up displaying 4 clean internal fenestration slits.",
                        heightCm = 45f,
                        heightNote = "45 cm (+4 cm)",
                        healthStatus = "Thriving"
                    ),
                    GrowthProgressEntity(
                        id = "milestone_2",
                        plantId = "up_1",
                        timestamp = now - (14 * oneDayMillis),
                        dateFormatted = "2 weeks ago",
                        title = "Repotted to Chunky Aroid Mix",
                        note = "Moved into 8-inch breathable terracotta with orchid bark and pumice.",
                        heightCm = 41f,
                        heightNote = "41 cm",
                        healthStatus = "Recovered"
                    ),
                    GrowthProgressEntity(
                        id = "milestone_3",
                        plantId = "up_2",
                        timestamp = now - (4 * oneDayMillis),
                        dateFormatted = "4 days ago",
                        title = "Pruned for Lateral Branching",
                        note = "Trimmed apical nodes to encourage lateral bushy growth.",
                        heightCm = 25f,
                        heightNote = "25 cm",
                        healthStatus = "Fragrant & Healthy"
                    ),
                    GrowthProgressEntity(
                        id = "milestone_4",
                        plantId = "up_3",
                        timestamp = now - (10 * oneDayMillis),
                        dateFormatted = "10 days ago",
                        title = "New Rhizome Sprout Emerged",
                        note = "A sturdy green shoot popped out next to the main rosette.",
                        heightCm = 60f,
                        heightNote = "60 cm",
                        healthStatus = "Thriving"
                    )
                )

                progressDao.insertProgressList(initialMilestones)
            }
        }
    }
}
