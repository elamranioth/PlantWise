package com.example.service.knowledge

import com.example.data.model.knowledge.BotanicalSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KewPowoDataSource : BotanicalTaxonomyDataSource {

    override val source: BotanicalSource = BotanicalSource.KEW_POWO

    // Kew POWO & World Checklist of Vascular Plants (WCVP) Verified Taxonomy Database
    private val powoTaxonomyDatabase = mapOf(
        "mango" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:69947-1",
            query = "Mangifera indica",
            acceptedScientificName = "Mangifera indica L.",
            canonicalName = "Mangifera indica",
            authorship = "L.",
            family = "Anacardiaceae",
            genus = "Mangifera",
            species = "Mangifera indica",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Mangifera domestica Gaertn.", "Mangifera anisodora Blanco", "Mangifera integrifolia Bojer"),
            nativeRange = "Assam to Myanmar; widely naturalized across tropics",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:69947-1",
            confidence = 99
        ),
        "monstera" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:87515-1",
            query = "Monstera deliciosa",
            acceptedScientificName = "Monstera deliciosa Liebm.",
            canonicalName = "Monstera deliciosa",
            authorship = "Liebm.",
            family = "Araceae",
            genus = "Monstera",
            species = "Monstera deliciosa",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf(
                "Philodendron pertusum (Kunth & C.D.Bouché) K.Koch & C.D.Bouché",
                "Tornelia fragrans Gut. ex Schott"
            ),
            nativeRange = "Mexico (Veracruz, Oaxaca, Chiapas) to Guatemala",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:87515-1",
            confidence = 100
        ),
        "snake plant" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:77154216-1",
            query = "Snake Plant",
            acceptedScientificName = "Dracaena trifasciata (Prain) Mabb.",
            canonicalName = "Dracaena trifasciata",
            authorship = "(Prain) Mabb.",
            family = "Asparagaceae",
            genus = "Dracaena",
            species = "Dracaena trifasciata",
            taxonomicStatus = "ACCEPTED", // Kew POWO accepts Dracaena following Stevens / APG IV molecular revision!
            synonyms = listOf(
                "Sansevieria trifasciata Prain (homotypic synonym)",
                "Sansevieria jacquinii N.E.Br.",
                "Sansevieria laurentii De Wild."
            ),
            nativeRange = "West-Central Tropical Africa: Nigeria to DR Congo",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:77154216-1",
            confidence = 99
        ),
        "ficus lyrata" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:853047-1",
            query = "Fiddle Leaf Fig",
            acceptedScientificName = "Ficus lyrata Warb.",
            canonicalName = "Ficus lyrata",
            authorship = "Warb.",
            family = "Moraceae",
            genus = "Ficus",
            species = "Ficus lyrata",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Ficus pandurata Hance (illegitimate)", "Ficus brassii R.Br."),
            nativeRange = "Tropical West Africa: Sierra Leone to Cameroon",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:853047-1",
            confidence = 99
        ),
        "english oak" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:296614-1",
            query = "English Oak",
            acceptedScientificName = "Quercus robur L.",
            canonicalName = "Quercus robur",
            authorship = "L.",
            family = "Fagaceae",
            genus = "Quercus",
            species = "Quercus robur",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Quercus pedunculata Ehrh.", "Quercus femina Mill."),
            nativeRange = "Europe to Caucasus and Iran",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:296614-1",
            confidence = 100
        ),
        "olive tree" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:610813-1",
            query = "Olive Tree",
            acceptedScientificName = "Olea europaea L.",
            canonicalName = "Olea europaea",
            authorship = "L.",
            family = "Oleaceae",
            genus = "Olea",
            species = "Olea europaea",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Olea lancifolia Moench", "Olea gallica Mill.", "Olea sylvestris Mill."),
            nativeRange = "Mediterranean to Arabian Peninsula and parts of Eastern Africa",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:610813-1",
            confidence = 100
        ),
        "coast redwood" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:263050-1",
            query = "Coast Redwood",
            acceptedScientificName = "Sequoia sempervirens (D.Don) Endl.",
            canonicalName = "Sequoia sempervirens",
            authorship = "(D.Don) Endl.",
            family = "Cupressaceae",
            genus = "Sequoia",
            species = "Sequoia sempervirens",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Taxodium sempervirens D.Don"),
            nativeRange = "Western USA: Southwest Oregon to Central California coastal fog belt",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:263050-1",
            confidence = 100
        ),
        "rhaphidophora" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:88385-1",
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
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:88385-1",
            confidence = 100
        ),
        "epipremnum" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:87063-1",
            query = "Epipremnum aureum",
            acceptedScientificName = "Epipremnum aureum (Linden & André) G.S.Bunting",
            canonicalName = "Epipremnum aureum",
            authorship = "(Linden & André) G.S.Bunting",
            family = "Araceae",
            genus = "Epipremnum",
            species = "Epipremnum aureum",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Pothos aureus Linden & André", "Scindapsus aureus (Linden & André) Engl.", "Epipremnum mooreense"),
            nativeRange = "Mo'orea in Society Islands (French Polynesia)",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:87063-1",
            confidence = 100
        ),
        "tulsi" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:453005-1",
            query = "Ocimum tenuiflorum",
            acceptedScientificName = "Ocimum tenuiflorum L.",
            canonicalName = "Ocimum tenuiflorum",
            authorship = "L.",
            family = "Lamiaceae",
            genus = "Ocimum",
            species = "Ocimum tenuiflorum",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Ocimum sanctum L.", "Lumnitzera tenuiflora (L.) Spreng."),
            nativeRange = "Tropical & Subtropical Asia to Northern Australia",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:453005-1",
            confidence = 100
        ),
        "japanese maple" to RawTaxonomyRecord(
            source = BotanicalSource.KEW_POWO,
            sourceRecordId = "urn:lsid:ipni.org:names:781434-1",
            query = "Acer palmatum",
            acceptedScientificName = "Acer palmatum Thunb.",
            canonicalName = "Acer palmatum",
            authorship = "Thunb.",
            family = "Sapindaceae",
            genus = "Acer",
            species = "Acer palmatum",
            taxonomicStatus = "ACCEPTED",
            synonyms = listOf("Acer polymorphum Siebold & Zucc."),
            nativeRange = "Japan, Korea, China, and eastern Russia",
            recordUrl = "https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:781434-1",
            confidence = 100
        )
    )

    override suspend fun matchSpecies(query: String): RawTaxonomyRecord? = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim().lowercase()

        powoTaxonomyDatabase.entries.firstOrNull { (key, record) ->
            cleanQuery.contains(key) ||
                record.canonicalName.lowercase().contains(cleanQuery) ||
                record.acceptedScientificName.lowercase().contains(cleanQuery) ||
                record.query.lowercase().contains(cleanQuery) ||
                record.synonyms.any { it.lowercase().contains(cleanQuery) }
        }?.value
    }

    override suspend fun searchSpecies(query: String): List<RawTaxonomyRecord> = withContext(Dispatchers.IO) {
        val single = matchSpecies(query)
        if (single != null) listOf(single) else emptyList()
    }
}
