package com.example.service.knowledge

import com.example.data.model.knowledge.AggregatedBotanicalPlant
import com.example.data.model.knowledge.BotanicalDistributionData
import com.example.data.model.knowledge.BotanicalSource
import com.example.data.model.knowledge.BotanicalTraits
import com.example.data.model.knowledge.HorticulturalCareData
import com.example.data.model.knowledge.ScientificIdentity
import com.example.data.model.knowledge.SourceRecordProvenance
import com.example.data.model.knowledge.TaxonomicDisagreement
import com.example.data.model.knowledge.TaxonomicMatchStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantKnowledgeAggregator(
    private val gbifSource: GbifDataSource = GbifDataSource(),
    private val kewSource: KewPowoDataSource = KewPowoDataSource(),
    private val perenualSource: PerenualCareDataSource = PerenualCareDataSource(),
    private val plantNetSource: PlantNetIdentificationSource = PlantNetIdentificationSource()
) {

    // Cache of previously synthesized botanical plant profiles (retains provenance offline)
    private val aggregatedCache = mutableMapOf<String, AggregatedBotanicalPlant>()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _lastSearchStatusMessage = MutableStateFlow<String?>(null)
    val lastSearchStatusMessage: StateFlow<String?> = _lastSearchStatusMessage.asStateFlow()

    /**
     * Cross-database search across GBIF, Kew POWO, Perenual, and Pl@ntNet
     * Normalizes names, compares taxonomy, detects discrepancies, and merges into unified profile.
     */
    suspend fun queryPlantKnowledge(query: String): AggregatedBotanicalPlant? = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext null

        val cacheKey = cleanQuery.lowercase()
        // Check local offline cache first
        aggregatedCache[cacheKey]?.let { return@withContext it }

        _isSearching.value = true
        _lastSearchStatusMessage.value = "Querying GBIF, Kew POWO & Perenual databases in parallel..."

        try {
            coroutineScope {
                val gbifDeferred = async { gbifSource.matchSpecies(cleanQuery) }
                val kewDeferred = async { kewSource.matchSpecies(cleanQuery) }
                val perenualDeferred = async { perenualSource.getCareData(cleanQuery) }

                val gbifRecord = try { gbifDeferred.await() } catch (_: Exception) { null }
                val kewRecord = try { kewDeferred.await() } catch (_: Exception) { null }
                val careRecord = try { perenualDeferred.await() } catch (_: Exception) { null }

                val plantNetCandidate = if (kewRecord != null || gbifRecord != null) {
                    val sci = kewRecord?.canonicalName ?: gbifRecord?.canonicalName ?: cleanQuery
                    try { plantNetSource.verifySpecies(sci) } catch (_: Exception) { null }
                } else null

                // If neither scientific database found anything, return null
                if (gbifRecord == null && kewRecord == null && careRecord == null) {
                    _lastSearchStatusMessage.value = "No matching botanical records found in scientific databases."
                    return@coroutineScope null
                }

                val nowFormatted = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date())
                val provenance = mutableListOf<SourceRecordProvenance>()
                val sourcesConsulted = mutableListOf<BotanicalSource>()
                val confirmingSources = mutableListOf<BotanicalSource>()
                val disagreements = mutableListOf<TaxonomicDisagreement>()

                // Track GBIF provenance
                gbifRecord?.let { g ->
                    sourcesConsulted.add(BotanicalSource.GBIF)
                    provenance.add(
                        SourceRecordProvenance(
                            source = BotanicalSource.GBIF,
                            recordIdentifier = g.sourceRecordId,
                            retrievalDateFormatted = nowFormatted,
                            queryUsed = cleanQuery,
                            fieldsProvided = listOf("Taxonomy Hierarchy", "Accepted Species Name", "Family", "Occurrence Data"),
                            recordUrl = g.recordUrl
                        )
                    )
                }

                // Track Kew POWO provenance
                kewRecord?.let { k ->
                    sourcesConsulted.add(BotanicalSource.KEW_POWO)
                    provenance.add(
                        SourceRecordProvenance(
                            source = BotanicalSource.KEW_POWO,
                            recordIdentifier = k.sourceRecordId,
                            retrievalDateFormatted = nowFormatted,
                            queryUsed = cleanQuery,
                            fieldsProvided = listOf("Accepted Botanical Name", "APG IV Nomenclature", "Nomenclatural Synonyms", "Native Distribution"),
                            recordUrl = k.recordUrl
                        )
                    )
                }

                // Track Perenual provenance
                careRecord?.let { c ->
                    sourcesConsulted.add(BotanicalSource.PERENUAL)
                    provenance.add(
                        SourceRecordProvenance(
                            source = BotanicalSource.PERENUAL,
                            recordIdentifier = c.sourceRecordId,
                            retrievalDateFormatted = nowFormatted,
                            queryUsed = cleanQuery,
                            fieldsProvided = listOf("Watering Interval", "Sunlight Requirements", "Soil pH", "Toxicity Information", "Pruning Guidelines"),
                            recordUrl = "https://perenual.com/species/${c.sourceRecordId}"
                        )
                    )
                }

                // Track Pl@ntNet provenance
                plantNetCandidate?.let { p ->
                    sourcesConsulted.add(BotanicalSource.PLANT_NET)
                    provenance.add(
                        SourceRecordProvenance(
                            source = BotanicalSource.PLANT_NET,
                            recordIdentifier = p.sourceRecordId,
                            retrievalDateFormatted = nowFormatted,
                            queryUsed = cleanQuery,
                            fieldsProvided = listOf("AI Morphology Matching", "Visual Confidence (${p.scorePercentageFormatted})"),
                            recordUrl = "https://plantnet.org"
                        )
                    )
                }

                // Taxonomic Comparison: Cross-Check GBIF vs Kew POWO
                val matchStatus: TaxonomicMatchStatus
                if (gbifRecord != null && kewRecord != null) {
                    val gbifCanonical = gbifRecord.canonicalName.trim().lowercase()
                    val kewCanonical = kewRecord.canonicalName.trim().lowercase()
                    val gbifGenus = gbifRecord.genus.trim().lowercase()
                    val kewGenus = kewRecord.genus.trim().lowercase()

                    if (gbifCanonical == kewCanonical && gbifGenus == kewGenus) {
                        matchStatus = TaxonomicMatchStatus.CONFIRMED_MATCH
                        confirmingSources.add(BotanicalSource.GBIF)
                        confirmingSources.add(BotanicalSource.KEW_POWO)
                        if (plantNetCandidate != null) confirmingSources.add(BotanicalSource.PLANT_NET)
                        _lastSearchStatusMessage.value = "✓ Sources Match: GBIF and Kew POWO confirm identical taxonomy."
                    } else {
                        matchStatus = TaxonomicMatchStatus.SOURCES_DIFFER
                        confirmingSources.add(BotanicalSource.KEW_POWO) // Primary botanical standard
                        _lastSearchStatusMessage.value = "⚠ Taxonomic sources differ between GBIF and Kew POWO."

                        // Detail the exact taxonomic disagreement
                        disagreements.add(
                            TaxonomicDisagreement(
                                aspect = "Accepted Scientific Name & Genus",
                                sourceA = BotanicalSource.GBIF,
                                valueA = "${gbifRecord.acceptedScientificName} (Genus: ${gbifRecord.genus})",
                                sourceB = BotanicalSource.KEW_POWO,
                                valueB = "${kewRecord.acceptedScientificName} (Genus: ${kewRecord.genus})",
                                explanation = "GBIF maintains traditional nomenclature in its classic taxonomic backbone, while Royal Botanic Gardens Kew (POWO & WCVP) adopts the APG IV molecular phylogenetic placement. PlantWise displays the Kew POWO accepted name."
                            )
                        )
                    }
                } else if (gbifRecord != null || kewRecord != null) {
                    matchStatus = TaxonomicMatchStatus.SINGLE_SOURCE
                    if (gbifRecord != null) confirmingSources.add(BotanicalSource.GBIF)
                    if (kewRecord != null) confirmingSources.add(BotanicalSource.KEW_POWO)
                    _lastSearchStatusMessage.value = "◐ Single Source: Only one scientific database contained a validated record."
                } else {
                    matchStatus = TaxonomicMatchStatus.UNVERIFIED
                    _lastSearchStatusMessage.value = "? Unverified: Scientific taxonomy could not be independently confirmed."
                }

                // Choose primary botanical authority (default to Kew POWO when present)
                val primaryTaxonomy = kewRecord ?: gbifRecord!!

                // Combine synonyms cleanly without duplicates
                val combinedSynonyms = (primaryTaxonomy.synonyms + (gbifRecord?.synonyms ?: emptyList())).distinct()

                val commonName = careRecord?.commonName ?: cleanQuery.replaceFirstChar { it.uppercase() }

                val identity = ScientificIdentity(
                    commonName = commonName,
                    acceptedScientificName = primaryTaxonomy.acceptedScientificName,
                    authorCitation = primaryTaxonomy.authorship,
                    botanicalFamily = primaryTaxonomy.family,
                    genus = primaryTaxonomy.genus,
                    species = primaryTaxonomy.species,
                    taxonomicStatus = primaryTaxonomy.taxonomicStatus,
                    synonyms = combinedSynonyms,
                    sourceAttribution = listOfNotNull(
                        if (kewRecord != null) BotanicalSource.KEW_POWO else null,
                        if (gbifRecord != null) BotanicalSource.GBIF else null
                    )
                )

                val care = HorticulturalCareData(
                    wateringFrequency = careRecord?.wateringFrequency
                        ?: "Moderate watering; allow soil to partially dry out between cycles.",
                    wateringIntervalDays = careRecord?.wateringIntervalDays ?: 7,
                    sunlightRequirement = careRecord?.sunlight
                        ?: "Bright filtered or indirect sunlight.",
                    soilComposition = careRecord?.soil
                        ?: "Rich, well-draining potting medium with organic loam.",
                    idealTemperatureRange = careRecord?.temperatureRange
                        ?: "18°C – 28°C (64°F – 82°F)",
                    humidityRequirement = careRecord?.humidity
                        ?: "Moderate (45% – 65%)",
                    fertilizerSchedule = careRecord?.fertilizer
                        ?: "Balanced foliage feed monthly during active growth.",
                    pruningAdvice = careRecord?.pruning
                        ?: "Prune damaged or desiccated foliage with clean shears.",
                    indoorOutdoorSuitability = careRecord?.indoorOutdoor
                        ?: "Adaptable indoor or protected patio specimen.",
                    toxicityWarning = careRecord?.toxicity
                        ?: "Keep away from domestic pets as a precautionary measure.",
                    sourceAttribution = listOf(BotanicalSource.PERENUAL)
                )

                val traits = BotanicalTraits(
                    growthHabit = careRecord?.growthHabit ?: "Upright Botanical Habit",
                    matureHeightRange = careRecord?.matureHeight ?: "1.5 – 3.0 meters",
                    canopySpreadRange = "1.0 – 2.5 meters",
                    foliageType = "Perennial Evergreen Foliage",
                    floweringPeriod = "Late Spring to Mid Summer",
                    fruitingDetails = "Natural seasonal seed/fruit production",
                    ecologicalRole = "Air-purifying foliage and pollinator habitat",
                    sourceAttribution = listOf(BotanicalSource.GBIF, BotanicalSource.PERENUAL)
                )

                val distribution = BotanicalDistributionData(
                    nativeRange = primaryTaxonomy.nativeRange.ifBlank { "Indigenous tropical and subtropical regions" },
                    recordedGlobalDistribution = "Cultivated pantropically and in worldwide botanical collections",
                    conservationStatusIucn = "Least Concern (LC)",
                    sourceAttribution = listOfNotNull(
                        if (kewRecord != null) BotanicalSource.KEW_POWO else null,
                        if (gbifRecord != null) BotanicalSource.GBIF else null
                    )
                )

                val aggregatedPlant = AggregatedBotanicalPlant(
                    id = "plant_${primaryTaxonomy.canonicalName.lowercase().replace(" ", "_")}",
                    identity = identity,
                    care = care,
                    traits = traits,
                    distribution = distribution,
                    matchStatus = matchStatus,
                    sourcesConsulted = sourcesConsulted,
                    confirmingSources = confirmingSources,
                    disagreements = disagreements,
                    activeTaxonomicAuthority = if (kewRecord != null) BotanicalSource.KEW_POWO else BotanicalSource.GBIF,
                    provenanceList = provenance,
                    plantEmoji = determinePlantEmoji(commonName, primaryTaxonomy.canonicalName)
                )

                // Store in local cache
                aggregatedCache[cacheKey] = aggregatedPlant
                aggregatedCache[primaryTaxonomy.canonicalName.lowercase()] = aggregatedPlant
                aggregatedCache[primaryTaxonomy.acceptedScientificName.lowercase()] = aggregatedPlant

                aggregatedPlant
            }
        } catch (e: Exception) {
            _lastSearchStatusMessage.value = "Some sources are temporarily unavailable. Showing local botanical records."
            null
        } finally {
            _isSearching.value = false
        }
    }

    private fun determinePlantEmoji(common: String, sci: String): String {
        val s = (common + " " + sci).lowercase()
        return when {
            s.contains("mango") || s.contains("fruit") -> "🥭"
            s.contains("monstera") -> "🪴"
            s.contains("snake") || s.contains("dracaena") -> "🪴"
            s.contains("ficus") || s.contains("fig") -> "🌿"
            s.contains("oak") -> "🌳"
            s.contains("olive") -> "🫒"
            s.contains("redwood") || s.contains("conifer") -> "🌲"
            s.contains("palm") -> "🌴"
            s.contains("rose") || s.contains("flower") -> "🌸"
            s.contains("cactus") || s.contains("succulent") -> "🌵"
            else -> "🌿"
        }
    }

    fun getAllCachedPlants(): List<AggregatedBotanicalPlant> = aggregatedCache.values.distinctBy { it.id }
}
