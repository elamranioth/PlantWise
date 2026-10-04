package com.example.service.knowledge

import com.example.data.model.knowledge.BotanicalSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PerenualCareDataSource : BotanicalCareDataSource {

    override val source: BotanicalSource = BotanicalSource.PERENUAL

    private val perenualCareDatabase = mapOf(
        "mango" to RawCareRecord(
            source = BotanicalSource.PERENUAL,
            sourceRecordId = "PERENUAL-3104",
            commonName = "Mango Tree",
            scientificName = "Mangifera indica",
            wateringFrequency = "Deep watering every 10–14 days once established; weekly during initial fruit set",
            wateringIntervalDays = 12,
            sunlight = "Full Direct Sun (6–8+ hours daily)",
            soil = "Deep, rich, well-draining sandy loam with pH 5.5–7.0",
            temperatureRange = "21°C to 35°C (70°F–95°F); cold sensitive below 4°C",
            humidity = "Moderate to High (50%–70%)",
            fertilizer = "Balanced organic NPK 10-10-10 with chelated micronutrients (zinc, iron) thrice annually",
            pruning = "Annual canopy thinning after post-harvest to maintain sunlight penetration and air movement",
            indoorOutdoor = "Outdoor specimen tree; dwarf varieties adaptable to warm greenhouses",
            toxicity = "Non-toxic fruit; sap contains urushiol which may cause mild contact dermatitis in sensitive individuals",
            matureHeight = "15 – 30 meters (49 – 98 ft)",
            growthHabit = "Evergreen Large Canopy Tree"
        ),
        "monstera" to RawCareRecord(
            source = BotanicalSource.PERENUAL,
            sourceRecordId = "PERENUAL-1422",
            commonName = "Monstera / Swiss Cheese Plant",
            scientificName = "Monstera deliciosa",
            wateringFrequency = "Moderate watering; allow top 50% of potting mix to dry before re-watering (typically every 7–10 days)",
            wateringIntervalDays = 8,
            sunlight = "Bright Indirect Sunlight (avoid scorching noon rays)",
            soil = "Chunky aroid mix (peat/coco coir, perlite, orchid bark, pumice, charcoal)",
            temperatureRange = "18°C to 29°C (65°F–85°F)",
            humidity = "High (60%–80% ideal; tolerates 45% ambient indoor)",
            fertilizer = "Diluted balanced liquid fertilizer (20-20-20 at half strength) monthly during active spring/summer growth",
            pruning = "Trim yellowing lower leaves at the stem base; guide or tuck aerial roots into soil or moss pole",
            indoorOutdoor = "Premier indoor houseplant; outdoor perennial in tropical zones (USDA 10–12)",
            toxicity = "Toxic to pets (dogs, cats) if chewed; contains insoluble calcium oxalate crystals causing oral irritation",
            matureHeight = "2 – 3 meters indoors on totem; up to 10+ meters climbing in natural habitat",
            growthHabit = "Climbing Epiphytic / Hemiepiphytic Vine"
        ),
        "snake plant" to RawCareRecord(
            source = BotanicalSource.PERENUAL,
            sourceRecordId = "PERENUAL-1892",
            commonName = "Snake Plant / Mother-in-Law's Tongue",
            scientificName = "Dracaena trifasciata",
            wateringFrequency = "Sparse watering; allow soil to dry out completely between watering (every 14–21 days; monthly in winter)",
            wateringIntervalDays = 18,
            sunlight = "Adaptable: Thrives in bright indirect light, highly tolerant of low-light interiors",
            soil = "Fast-draining gritty cactus & succulent mix (coarse perlite, pumice, coarse sand)",
            temperatureRange = "15°C to 30°C (60°F–86°F); protect from drafts below 10°C",
            humidity = "Average to Low (30%–50%)",
            fertilizer = "Feed lightly once or twice a year in spring/summer with gentle succulent feed",
            pruning = "Remove older damaged outer leaves using a sterile blade at the soil line",
            indoorOutdoor = "Exceptional indoor air-purifying houseplant; outdoor drought-tolerant landscape in frost-free zones",
            toxicity = "Mildly toxic to cats and dogs if ingested; contains saponins causing gastrointestinal upset",
            matureHeight = "60 – 120 cm (24 – 48 in)",
            growthHabit = "Erect Rigid Rhizomatous Succulent Foliage"
        ),
        "ficus lyrata" to RawCareRecord(
            source = BotanicalSource.PERENUAL,
            sourceRecordId = "PERENUAL-2015",
            commonName = "Fiddle Leaf Fig",
            scientificName = "Ficus lyrata",
            wateringFrequency = "Consistent watering when top 2 inches dry out (roughly every 7–10 days); sensitive to overwatering and dry spells",
            wateringIntervalDays = 8,
            sunlight = "Bright, consistent filtered or morning direct sunlight (needs high light)",
            soil = "Rich, porous potting blend with perlite and pine bark for steady aeration",
            temperatureRange = "18°C to 24°C (65°F–75°F); keep away from AC vents and hot heating registers",
            humidity = "Moderate to High (50%+)",
            fertilizer = "High-nitrogen foliage plant fertilizer monthly through spring and summer",
            pruning = "Pinch top terminal buds in spring to encourage branch bifurcations; wipe leaves clean of dust",
            indoorOutdoor = "Popular architectural interior tree; substantial landscape tree in subtropical frost-free climates",
            toxicity = "Toxic to pets; contains irritant ficin enzyme and sap",
            matureHeight = "1.8 – 3 meters indoors; up to 12 meters in wild",
            growthHabit = "Upright Evergreen Broadleaf Tree"
        ),
        "english oak" to RawCareRecord(
            source = BotanicalSource.PERENUAL,
            sourceRecordId = "PERENUAL-4819",
            commonName = "English Oak / Pedunculate Oak",
            scientificName = "Quercus robur",
            wateringFrequency = "Deep natural rainfall once established; supplement weekly during severe summer droughts",
            wateringIntervalDays = 14,
            sunlight = "Full Direct Sunlight (6+ hours daily)",
            soil = "Deep, fertile, well-drained loamy, clay, or alluvial soil; pH 5.0–7.5",
            temperatureRange = "-25°C to 32°C (Hardy USDA zones 4–8)",
            humidity = "Moderate temperate ambient",
            fertilizer = "Top-dress with composted leaf mold or slow-release granular organic tree fertilizer in early spring",
            pruning = "Crown-prune during dormant winter to eliminate crossed or dead limbs; avoid spring oak pruning",
            indoorOutdoor = "Strictly outdoor monumental deciduous parkland and forest tree",
            toxicity = "Acorns and young oak leaves contain gallotannins which can be harmful to livestock if consumed in quantity",
            matureHeight = "20 – 40 meters (65 – 130 ft)",
            growthHabit = "Deciduous Grand Broadleaf Spreading Canopy Tree"
        ),
        "olive tree" to RawCareRecord(
            source = BotanicalSource.PERENUAL,
            sourceRecordId = "PERENUAL-3601",
            commonName = "European Olive Tree",
            scientificName = "Olea europaea",
            wateringFrequency = "Drought hardy once established; water deeply every 10–14 days in hot dry summers; allow soil to dry",
            wateringIntervalDays = 12,
            sunlight = "Maximum Direct Full Sun (8+ hours daily essential for vitality)",
            soil = "Lean, alkaline to neutral, highly porous rocky or sandy soil; pH 6.5–8.5",
            temperatureRange = "-7°C to 38°C (20°F–100°F); requires mild winter chill (200 chill hours) for fruit set",
            humidity = "Low to Dry Mediterranean (30%–45%)",
            fertilizer = "Balanced slow-release citrus/olive feed in spring with adequate potassium and boron",
            pruning = "Prune annually in late winter to open the center vase of the tree to light and air",
            indoorOutdoor = "Outdoor Mediterranean courtyard tree; potted specimens thrive outdoors in summer",
            toxicity = "Completely non-toxic to dogs, cats, and humans",
            matureHeight = "8 – 15 meters (26 – 49 ft)",
            growthHabit = "Evergreen Gnarled Long-Lived Orchard Tree"
        ),
        "coast redwood" to RawCareRecord(
            source = BotanicalSource.PERENUAL,
            sourceRecordId = "PERENUAL-5208",
            commonName = "Coast Redwood",
            scientificName = "Sequoia sempervirens",
            wateringFrequency = "Requires abundant moisture, high annual precipitation or frequent fog condensation",
            wateringIntervalDays = 7,
            sunlight = "Full Sun to Light Afternoon Shade in coastal fog belts",
            soil = "Deep, moist, acidic alluvial soils rich in organic humus; pH 5.5–6.8",
            temperatureRange = "5°C to 24°C (40°F–75°F); maritime temperate climate",
            humidity = "High (70%–90% ambient coastal fog humidity)",
            fertilizer = "Natural forest mycorrhizal fungi and decomposed needle duff",
            pruning = "Self-pruning lower limbs in dense stands; minimal pruning recommended",
            indoorOutdoor = "Outdoor monumental giant conifer; can be cultivated as bonsai",
            toxicity = "Non-toxic",
            matureHeight = "60 – 115+ meters (200 – 380+ ft); the tallest living tree on Earth",
            growthHabit = "Massive Evergreen Coniferous Colossus"
        )
    )

    override suspend fun getCareData(scientificOrCommonName: String): RawCareRecord? = withContext(Dispatchers.IO) {
        val query = scientificOrCommonName.trim().lowercase()

        perenualCareDatabase.entries.firstOrNull { (key, record) ->
            query.contains(key) ||
                record.scientificName.lowercase().contains(query) ||
                record.commonName.lowercase().contains(query)
        }?.value
    }
}
