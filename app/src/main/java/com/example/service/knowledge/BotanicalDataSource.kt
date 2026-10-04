package com.example.service.knowledge

import com.example.data.model.knowledge.BotanicalSource

data class RawTaxonomyRecord(
    val source: BotanicalSource,
    val sourceRecordId: String,
    val query: String,
    val acceptedScientificName: String,
    val canonicalName: String,
    val authorship: String,
    val family: String,
    val genus: String,
    val species: String,
    val taxonomicStatus: String, // "ACCEPTED", "SYNONYM", "DOUBTFUL"
    val acceptedUsageKeyOrName: String = "",
    val synonyms: List<String> = emptyList(),
    val nativeRange: String = "",
    val recordUrl: String,
    val confidence: Int = 95
)

data class RawCareRecord(
    val source: BotanicalSource = BotanicalSource.PERENUAL,
    val sourceRecordId: String,
    val commonName: String,
    val scientificName: String,
    val wateringFrequency: String,
    val wateringIntervalDays: Int,
    val sunlight: String,
    val soil: String,
    val temperatureRange: String,
    val humidity: String,
    val fertilizer: String,
    val pruning: String,
    val indoorOutdoor: String,
    val toxicity: String,
    val matureHeight: String,
    val growthHabit: String
)

interface BotanicalTaxonomyDataSource {
    val source: BotanicalSource
    suspend fun matchSpecies(query: String): RawTaxonomyRecord?
    suspend fun searchSpecies(query: String): List<RawTaxonomyRecord>
}

interface BotanicalCareDataSource {
    val source: BotanicalSource
    suspend fun getCareData(scientificOrCommonName: String): RawCareRecord?
}
