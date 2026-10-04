package com.example.data.model

data class PlantScanResult(
    val commonName: String,
    val scientificName: String,
    val botanicalFamily: String,
    val confidence: Int, // e.g. 96%
    val healthScore: Int, // 0 - 100
    val healthCondition: String, // "Optimal Health", "Under-watered", "Sunlight Scorch", "Over-watered / Root Stress", "Nutrient Deficiency"
    val diagnosisDetail: String,
    val realTimeGrowthTips: List<String>,
    val immediateCareActions: List<String>,
    val matchedPlant: Plant? = null,
    val aiModelSource: String = "Botanical Neural Vision"
)
