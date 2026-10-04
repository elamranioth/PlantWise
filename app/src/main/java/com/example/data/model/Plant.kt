package com.example.data.model

enum class PlantType(val label: String, val iconEmoji: String) {
    DECIDUOUS_TREE("Deciduous Tree", "🌳"),
    EVERGREEN_TREE("Evergreen Tree & Conifer", "🌲"),
    FRUIT_TREE("Fruit & Nut Tree", "🍎"),
    INDOOR_FOLIAGE("Indoor Foliage", "🪴"),
    HERB_MEDICINAL("Herb & Medicinal", "🌿"),
    SUCCULENT_CACTUS("Succulent & Cactus", "🌵"),
    FLOWERING("Flowering Shrub & Bloom", "🌸"),
    EDIBLE_FRUIT("Edible Crop & Vegetable", "🍅"),
    VINE_CLIMBER("Vine & Climber", "🍇"),
    FERN("Fern & Ancient Spore", "🌾"),
    CARNIVOROUS("Carnivorous Plant", "🪴"),
    PALM_CYCAD("Palm & Cycad", "🌴")
}

enum class SunlightExposure(val label: String, val idealLux: String, val description: String) {
    DIRECT_SUN("Direct Sunlight", "5000+ Lux", "Unfiltered full sun for 6+ hours daily. Best for trees, herbs, cacti, and fruiting crops."),
    BRIGHT_INDIRECT("Bright Indirect Light", "2000 - 5000 Lux", "Abundant light filtered through sheer curtains or near east-facing windows."),
    MEDIUM_INDIRECT("Medium Filtered Light", "1000 - 2000 Lux", "Dappled light several feet from windows. Great for understory trees and tropical foliage."),
    LOW_LIGHT("Low / Shaded Light", "500 - 1000 Lux", "Tolerates shaded forest floors, darker corners, and north-facing rooms.")
}

data class CareInstructions(
    val wateringIntervalDays: Int,
    val wateringTip: String,
    val sunlightLevel: SunlightExposure,
    val recommendedSunlightHours: Int,
    val sunlightTip: String,
    val soilType: String,
    val temperatureRange: String,
    val humidityLevel: String,
    val isPetToxic: Boolean,
    val toxicityDetails: String,
    val repottingAdvice: String
)

data class GrowthStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val expertTip: String
)

data class Plant(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val familyCharacteristics: String,
    val plantType: PlantType,
    val uses: List<String>,
    val nativeHabitat: String,
    val placesWhereItGrows: String,
    val hardinessZones: String,
    val care: CareInstructions,
    val growthSteps: List<GrowthStep>,
    val iconEmoji: String = "🌿",
    val funFact: String,
    val isTree: Boolean = false,
    val matureHeight: String = "3 - 6 ft",
    val lifespan: String = "Perennial",

    // Comprehensive Botanical & Dendrological Extensions
    val kingdom: String = "Plantae",
    val division: String = "Tracheophyta (Vascular Plants)",
    val classTaxon: String = "Magnoliopsida",
    val order: String = "Rosales",
    val genus: String = "",
    val species: String = "",
    val canopySpread: String = "15 - 30 ft",
    val trunkDiameter: String = "1 - 3 ft",
    val barkTexture: String = "Smooth to furrowed",
    val woodDensity: String = "Moderate density hardwood",
    val rootSystem: String = "Fibrous lateral root network",
    val growthRate: String = "Medium (12 - 24 in/year)",
    val foliageLongevity: String = "Deciduous / Evergreen",
    val leafVenation: String = "Pinnate / Palmate",
    val floweringSeason: String = "Spring to Summer",
    val flowerColor: String = "Creamy White / Yellow",
    val fruitOrCone: String = "Berry / Drupe / Cone / Samara",
    val pollinationMechanism: String = "Entomophilous (Bees & Pollinators)",
    val carbonSequestration: String = "~48 lbs CO₂/year at full maturity",
    val wildlifeValue: String = "Provides nesting canopy, seeds, and nectar",
    val soilPhRange: String = "6.0 - 7.0 (Slightly acidic to neutral)",
    val droughtTolerance: String = "Moderate once established",
    val salinityTolerance: String = "Low to Moderate",
    val windResistance: String = "High storm resilience",
    val commonPests: String = "Aphids, spider mites, caterpillars, scale",
    val pruningSeason: String = "Late winter during vegetative dormancy",
    val propagationMethods: String = "Stem cuttings, seed stratification, grafting",
    val companionPlants: String = "Ferns, clover, aromatic herbs, alliums",
    val timberAndCraft: String = "Cabinetry, tool handles, ornamental carving",
    val medicinalProperties: String = "Antioxidant flavonoids, essential oils",
    val ecologicalRole: String = "Canopy builder, soil stabilizer, microclimate buffer"
)
