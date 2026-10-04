package com.example.service.knowledge

import com.example.data.model.knowledge.BotanicalSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class GbifDataSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()
) : BotanicalTaxonomyDataSource {

    override val source: BotanicalSource = BotanicalSource.GBIF

    // Curated GBIF Backbone Taxonomy fallback cache
    private val localGbifBackbone = mapOf(
        "mango" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-3190623",
            query = "Mango",
            acceptedScientificName = "Mangifera indica L.",
            canonicalName = "Mangifera indica",
            authorship = "L.",
            family = "Anacardiaceae",
            genus = "Mangifera",
            species = "Mangifera indica",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Mangifera domestica Gaertn.", "Mangifera anisodora Blanco"),
            nativeRange = "Tropical Asia (India, Myanmar, Bangladesh)",
            recordUrl = "https://www.gbif.org/species/3190623",
            confidence = 98
        ),
        "monstera" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-2868155",
            query = "Monstera deliciosa",
            acceptedScientificName = "Monstera deliciosa Liebm.",
            canonicalName = "Monstera deliciosa",
            authorship = "Liebm.",
            family = "Araceae",
            genus = "Monstera",
            species = "Monstera deliciosa",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Philodendron pertusum (Kunth & C.D.Bouché) K.Koch & C.D.Bouché"),
            nativeRange = "Tropical rainforests of southern Mexico to Panama",
            recordUrl = "https://www.gbif.org/species/2868155",
            confidence = 99
        ),
        "snake plant" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-2769910",
            query = "Snake Plant",
            acceptedScientificName = "Sansevieria trifasciata Prain",
            canonicalName = "Sansevieria trifasciata",
            authorship = "Prain",
            family = "Asparagaceae",
            genus = "Sansevieria",
            species = "Sansevieria trifasciata",
            taxonomicStatus = "ACCEPTED", // Notice GBIF retains Sansevieria in classic backbone, differing from modern Kew Dracaena!
            synonyms = listOf("Dracaena trifasciata (Prain) Mabb.", "Sansevieria zeylanica var. laurentii"),
            nativeRange = "Tropical West Africa from Nigeria east to the Congo",
            recordUrl = "https://www.gbif.org/species/2769910",
            confidence = 94
        ),
        "ficus lyrata" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-5361907",
            query = "Fiddle Leaf Fig",
            acceptedScientificName = "Ficus lyrata Warb.",
            canonicalName = "Ficus lyrata",
            authorship = "Warb.",
            family = "Moraceae",
            genus = "Ficus",
            species = "Ficus lyrata",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Ficus pandurata Hance"),
            nativeRange = "Western Africa from Cameroon west to Sierra Leone",
            recordUrl = "https://www.gbif.org/species/5361907",
            confidence = 97
        ),
        "english oak" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-2878688",
            query = "English Oak",
            acceptedScientificName = "Quercus robur L.",
            canonicalName = "Quercus robur",
            authorship = "L.",
            family = "Fagaceae",
            genus = "Quercus",
            species = "Quercus robur",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Quercus pedunculata Ehrh.", "Quercus femina Mill."),
            nativeRange = "Europe, Anatolia, Caucasus and parts of North Africa",
            recordUrl = "https://www.gbif.org/species/2878688",
            confidence = 99
        ),
        "olive tree" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-3172439",
            query = "Olive Tree",
            acceptedScientificName = "Olea europaea L.",
            canonicalName = "Olea europaea",
            authorship = "L.",
            family = "Oleaceae",
            genus = "Olea",
            species = "Olea europaea",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Olea sylvestris Mill.", "Olea oleaster Hoffmanns. & Link"),
            nativeRange = "Mediterranean Basin, southwestern Asia, and southeastern Europe",
            recordUrl = "https://www.gbif.org/species/3172439",
            confidence = 98
        ),
        "coast redwood" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-2684200",
            query = "Coast Redwood",
            acceptedScientificName = "Sequoia sempervirens (D.Don) Endl.",
            canonicalName = "Sequoia sempervirens",
            authorship = "(D.Don) Endl.",
            family = "Cupressaceae",
            genus = "Sequoia",
            species = "Sequoia sempervirens",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Taxodium sempervirens D.Don"),
            nativeRange = "Coastal Pacific Northwest, Northern California to Southern Oregon",
            recordUrl = "https://www.gbif.org/species/2684200",
            confidence = 99
        ),
        "rhaphidophora" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-2868482",
            query = "Rhaphidophora tetrasperma",
            acceptedScientificName = "Rhaphidophora tetrasperma Hook.f.",
            canonicalName = "Rhaphidophora tetrasperma",
            authorship = "Hook.f.",
            family = "Araceae",
            genus = "Rhaphidophora",
            species = "Rhaphidophora tetrasperma",
            taxonomicStatus = "ACCEPTED",
            synonyms = emptyList(),
            nativeRange = "Southern Thailand to Peninsular Malaysia",
            recordUrl = "https://www.gbif.org/species/2868482",
            confidence = 98
        ),
        "epipremnum" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-2868288",
            query = "Golden Pothos",
            acceptedScientificName = "Epipremnum aureum (Linden & André) G.S.Bunting",
            canonicalName = "Epipremnum aureum",
            authorship = "(Linden & André) G.S.Bunting",
            family = "Araceae",
            genus = "Epipremnum",
            species = "Epipremnum aureum",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Pothos aureus Linden & André", "Scindapsus aureus (Linden & André) Engl."),
            nativeRange = "Mo'orea in Society Islands (French Polynesia)",
            recordUrl = "https://www.gbif.org/species/2868288",
            confidence = 99
        ),
        "tulsi" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-2927050",
            query = "Holy Basil",
            acceptedScientificName = "Ocimum tenuiflorum L.",
            canonicalName = "Ocimum tenuiflorum",
            authorship = "L.",
            family = "Lamiaceae",
            genus = "Ocimum",
            species = "Ocimum tenuiflorum",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Ocimum sanctum L."),
            nativeRange = "Tropical & Subtropical Asia to Northern Australia",
            recordUrl = "https://www.gbif.org/species/2927050",
            confidence = 99
        ),
        "japanese maple" to RawTaxonomyRecord(
            source = BotanicalSource.GBIF,
            sourceRecordId = "GBIF-3189859",
            query = "Japanese Maple",
            acceptedScientificName = "Acer palmatum Thunb.",
            canonicalName = "Acer palmatum",
            authorship = "Thunb.",
            family = "Sapindaceae",
            genus = "Acer",
            species = "Acer palmatum",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Acer polymorphum Siebold & Zucc."),
            nativeRange = "Japan, Korea, China, eastern Mongolia, and southeast Russia",
            recordUrl = "https://www.gbif.org/species/3189859",
            confidence = 99
        )
    )

    override suspend fun matchSpecies(query: String): RawTaxonomyRecord? = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        val normalized = cleanQuery.lowercase()

        // 1. Attempt live GBIF REST API call
        try {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val request = Request.Builder()
                .url("https://api.gbif.org/v1/species/match?name=$encoded&verbose=false")
                .header("User-Agent", "PlantWise-BotanicalEngine/1.4 (Android; OpenBotanical)")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = JSONObject(body)
                    val matchType = json.optString("matchType", "NONE")
                    if (matchType != "NONE" && json.has("scientificName")) {
                        val usageKey = json.optInt("usageKey", json.optInt("speciesKey", 0))
                        val sciName = json.optString("scientificName")
                        val canonical = json.optString("canonicalName", sciName)
                        val family = json.optString("family", "Unknown Family")
                        val genus = json.optString("genus", canonical.split(" ").firstOrNull() ?: "")
                        val species = json.optString("species", canonical)
                        val status = json.optString("status", "ACCEPTED")
                        val confidence = json.optInt("confidence", 90)

                        return@withContext RawTaxonomyRecord(
                            source = BotanicalSource.GBIF,
                            sourceRecordId = "GBIF-$usageKey",
                            query = cleanQuery,
                            acceptedScientificName = sciName,
                            canonicalName = canonical,
                            authorship = sciName.removePrefix(canonical).trim(),
                            family = family,
                            genus = genus,
                            species = species,
                            taxonomicStatus = status,
                            recordUrl = "https://www.gbif.org/species/$usageKey",
                            confidence = confidence
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Live network call fell back gracefully to cached GBIF backbone
        }

        // 2. Query cached GBIF Backbone
        localGbifBackbone.entries.firstOrNull { (key, record) ->
            normalized.contains(key) ||
                record.canonicalName.lowercase().contains(normalized) ||
                record.acceptedScientificName.lowercase().contains(normalized) ||
                record.commonNameMatches(normalized)
        }?.value
    }

    private fun RawTaxonomyRecord.commonNameMatches(q: String): Boolean {
        return query.lowercase().contains(q) || synonyms.any { it.lowercase().contains(q) }
    }

    override suspend fun searchSpecies(query: String): List<RawTaxonomyRecord> = withContext(Dispatchers.IO) {
        val single = matchSpecies(query)
        if (single != null) listOf(single) else emptyList()
    }
}
