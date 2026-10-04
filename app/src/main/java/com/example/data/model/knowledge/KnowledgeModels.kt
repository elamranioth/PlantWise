package com.example.data.model.knowledge

import androidx.compose.ui.graphics.Color

/**
 * Botanical Data Providers
 */
enum class BotanicalSource(
    val displayName: String,
    val shortBadge: String,
    val primaryColorHex: Long,
    val authorityType: String,
    val officialUrl: String
) {
    GBIF(
        displayName = "GBIF (Global Biodiversity Information Facility)",
        shortBadge = "GBIF",
        primaryColorHex = 0xFF2B5B49,
        authorityType = "Primary Scientific Taxonomy & Global Occurrences",
        officialUrl = "https://www.gbif.org"
    ),
    KEW_POWO(
        displayName = "Royal Botanic Gardens, Kew (POWO & WCVP)",
        shortBadge = "Kew POWO",
        primaryColorHex = 0xFF143D32,
        authorityType = "Scientific Botanical Validation & Nomenclature",
        officialUrl = "https://powo.science.kew.org"
    ),
    PERENUAL(
        displayName = "Perenual Horticultural Knowledge Base",
        shortBadge = "Perenual",
        primaryColorHex = 0xFF356859,
        authorityType = "Horticultural Care, Irrigation & Growth Benchmarks",
        officialUrl = "https://perenual.com"
    ),
    PLANT_NET(
        displayName = "Pl@ntNet Visual Identification Network",
        shortBadge = "Pl@ntNet",
        primaryColorHex = 0xFF3B7A57,
        authorityType = "AI Botanical Recognition & Candidate Verification",
        officialUrl = "https://plantnet.org"
    )
}

/**
 * Cross-Database Verification Match Status
 */
enum class TaxonomicMatchStatus(
    val label: String,
    val symbol: String,
    val description: String
) {
    CONFIRMED_MATCH(
        label = "Confirmed Match",
        symbol = "✓",
        description = "Two or more authoritative botanical sources independently agree on taxonomy."
    ),
    SINGLE_SOURCE(
        label = "Single Source",
        symbol = "◐",
        description = "Only one verified database currently contains this botanical record."
    ),
    SOURCES_DIFFER(
        label = "Sources Differ",
        symbol = "⚠",
        description = "Authoritative databases have different accepted names or taxonomic classification."
    ),
    UNVERIFIED(
        label = "Unverified",
        symbol = "?",
        description = "Information could not yet be independently confirmed across botanical sources."
    )
}

/**
 * Detailed discrepancy report when two sources return differing classifications
 */
data class TaxonomicDisagreement(
    val aspect: String, // e.g., "Accepted Scientific Name", "Genus Classification", "Synonym Status"
    val sourceA: BotanicalSource,
    val valueA: String,
    val sourceB: BotanicalSource,
    val valueB: String,
    val explanation: String
)

/**
 * Metadata tracking the retrieval date, record ID, and fields supplied by an external database
 */
data class SourceRecordProvenance(
    val source: BotanicalSource,
    val recordIdentifier: String,
    val retrievalDateFormatted: String,
    val queryUsed: String,
    val fieldsProvided: List<String>,
    val recordUrl: String,
    val isLiveSynced: Boolean = true
)

/**
 * Scientific Identity (Pure Taxonomy, strictly separated from care advice)
 */
data class ScientificIdentity(
    val commonName: String,
    val acceptedScientificName: String,
    val authorCitation: String = "",
    val botanicalFamily: String,
    val genus: String,
    val species: String,
    val taxonomicStatus: String, // "ACCEPTED", "SYNONYM", "PROVISIONALLY ACCEPTED"
    val synonyms: List<String> = emptyList(),
    val sourceAttribution: List<BotanicalSource> = listOf(BotanicalSource.GBIF, BotanicalSource.KEW_POWO)
)

/**
 * Plant Care (Horticultural recommendations from care databases)
 */
data class HorticulturalCareData(
    val wateringFrequency: String,
    val wateringIntervalDays: Int,
    val sunlightRequirement: String,
    val soilComposition: String,
    val idealTemperatureRange: String,
    val humidityRequirement: String,
    val fertilizerSchedule: String,
    val pruningAdvice: String,
    val indoorOutdoorSuitability: String,
    val toxicityWarning: String,
    val sourceAttribution: List<BotanicalSource> = listOf(BotanicalSource.PERENUAL)
)

/**
 * Botanical Characteristics
 */
data class BotanicalTraits(
    val growthHabit: String,
    val matureHeightRange: String,
    val canopySpreadRange: String,
    val foliageType: String,
    val floweringPeriod: String,
    val fruitingDetails: String,
    val ecologicalRole: String,
    val sourceAttribution: List<BotanicalSource> = listOf(BotanicalSource.GBIF, BotanicalSource.PERENUAL)
)

/**
 * Geographic Distribution
 */
data class BotanicalDistributionData(
    val nativeRange: String,
    val recordedGlobalDistribution: String,
    val conservationStatusIucn: String = "Least Concern (LC)",
    val sourceAttribution: List<BotanicalSource> = listOf(BotanicalSource.GBIF, BotanicalSource.KEW_POWO)
)

/**
 * Master Aggregated Botanical Plant
 * Normalized multi-source profile with clear separation of facts, care, and attribution
 */
data class AggregatedBotanicalPlant(
    val id: String,
    val identity: ScientificIdentity,
    val care: HorticulturalCareData,
    val traits: BotanicalTraits,
    val distribution: BotanicalDistributionData,
    val matchStatus: TaxonomicMatchStatus,
    val sourcesConsulted: List<BotanicalSource>,
    val confirmingSources: List<BotanicalSource>,
    val disagreements: List<TaxonomicDisagreement> = emptyList(),
    val activeTaxonomicAuthority: BotanicalSource = BotanicalSource.KEW_POWO,
    val provenanceList: List<SourceRecordProvenance> = emptyList(),
    val isPartiallyUnavailable: Boolean = false,
    val plantEmoji: String = "🌿",
    val photoUrl: String = ""
)
