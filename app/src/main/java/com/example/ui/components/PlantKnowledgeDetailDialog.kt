package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.knowledge.AggregatedBotanicalPlant
import com.example.data.model.knowledge.BotanicalSource
import com.example.data.model.knowledge.TaxonomicMatchStatus
import com.example.ui.theme.PlantMint
import com.example.ui.theme.PlantPrimary
import com.example.ui.theme.PlantSecondary
import com.example.ui.theme.PlantTextSecondary

@Composable
fun PlantKnowledgeDetailDialog(
    plant: AggregatedBotanicalPlant,
    onDismiss: () -> Unit,
    onAddToGarden: (AggregatedBotanicalPlant) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var showSourcesDialog by remember { mutableStateOf(false) }
    var showDisagreementDetails by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("plant_knowledge_detail_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = plant.plantEmoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = plant.identity.commonName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = plant.identity.acceptedScientificName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION: MATCH STATUS BANNER
                val matchStatusColor = when (plant.matchStatus) {
                    TaxonomicMatchStatus.CONFIRMED_MATCH -> Color(0xFFDCFCE7)
                    TaxonomicMatchStatus.SOURCES_DIFFER -> Color(0xFFFEF3C7)
                    TaxonomicMatchStatus.SINGLE_SOURCE -> Color(0xFFE0F2FE)
                    TaxonomicMatchStatus.UNVERIFIED -> Color(0xFFF3F4F6)
                }
                val matchTextColor = when (plant.matchStatus) {
                    TaxonomicMatchStatus.CONFIRMED_MATCH -> Color(0xFF15803D)
                    TaxonomicMatchStatus.SOURCES_DIFFER -> Color(0xFFB45309)
                    TaxonomicMatchStatus.SINGLE_SOURCE -> Color(0xFF0369A1)
                    TaxonomicMatchStatus.UNVERIFIED -> Color(0xFF4B5563)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = matchStatusColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = plant.matchStatus.symbol,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = matchTextColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = plant.matchStatus.label,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = matchTextColor
                                )
                            }

                            // View Sources Trigger Button
                            Surface(
                                onClick = { showSourcesDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.testTag("view_sources_btn")
                            ) {
                                Text(
                                    text = "View Sources (${plant.sourcesConsulted.size}) ↗",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = matchTextColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = plant.matchStatus.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = matchTextColor.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        // Source confirmation tags
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verified by:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = matchTextColor
                            )
                            plant.confirmingSources.forEach { src ->
                                SourceBadgeChip(source = src, isSmall = true)
                            }
                        }
                    }
                }

                // SECTION: DISAGREEMENT BREAKDOWN (If sources differ)
                if (plant.disagreements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDisagreementDetails = !showDisagreementDetails },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Taxonomic Sources Differ",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFFB45309)
                                    )
                                }
                                Icon(
                                    if (showDisagreementDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309)
                                )
                            }

                            AnimatedVisibility(visible = showDisagreementDetails) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    plant.disagreements.forEach { dis ->
                                        Text(
                                            text = "Aspect: ${dis.aspect}",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF78350F)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row {
                                                    Text(text = "${dis.sourceA.shortBadge}: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2B5B49))
                                                    Text(text = dis.valueA, fontSize = 12.sp, color = Color(0xFF1F2937))
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row {
                                                    Text(text = "${dis.sourceB.shortBadge}: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF143D32))
                                                    Text(text = dis.valueB, fontSize = 12.sp, color = Color(0xFF1F2937))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = dis.explanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF92400E)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Currently displaying ${plant.activeTaxonomicAuthority.shortBadge} accepted nomenclature.",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF143D32)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: IDENTITY (Scientific Facts)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Botanical Identity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                plant.identity.sourceAttribution.forEach { src ->
                                    SourceBadgeChip(src, isSmall = true)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        DetailFactRow("Accepted Name", "${plant.identity.acceptedScientificName} ${plant.identity.authorCitation}")
                        DetailFactRow("Family", plant.identity.botanicalFamily)
                        DetailFactRow("Genus", plant.identity.genus)
                        DetailFactRow("Species Epithet", plant.identity.species)
                        DetailFactRow("Taxonomic Status", plant.identity.taxonomicStatus)

                        if (plant.identity.synonyms.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Known Synonyms (${plant.identity.synonyms.size}):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            plant.identity.synonyms.take(4).forEach { syn ->
                                Text(
                                    text = "• $syn",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 2: PLANT CARE (Horticultural Advice)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Horticultural Care",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            plant.care.sourceAttribution.forEach { src ->
                                SourceBadgeChip(src, isSmall = true)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        DetailFactRow("Watering Cycle", plant.care.wateringFrequency)
                        DetailFactRow("Sunlight", plant.care.sunlightRequirement)
                        DetailFactRow("Soil Composition", plant.care.soilComposition)
                        DetailFactRow("Temperature Range", plant.care.idealTemperatureRange)
                        DetailFactRow("Humidity", plant.care.humidityRequirement)
                        DetailFactRow("Fertilization", plant.care.fertilizerSchedule)
                        DetailFactRow("Pruning", plant.care.pruningAdvice)
                        DetailFactRow("Indoor/Outdoor", plant.care.indoorOutdoorSuitability)
                        DetailFactRow("Toxicity", plant.care.toxicityWarning)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 3: BOTANICAL CHARACTERISTICS & DISTRIBUTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Morphology & Distribution",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                plant.distribution.sourceAttribution.forEach { src ->
                                    SourceBadgeChip(src, isSmall = true)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        DetailFactRow("Growth Habit", plant.traits.growthHabit)
                        DetailFactRow("Mature Height", plant.traits.matureHeightRange)
                        DetailFactRow("Canopy Spread", plant.traits.canopySpreadRange)
                        DetailFactRow("Foliage Type", plant.traits.foliageType)
                        DetailFactRow("Flowering", plant.traits.floweringPeriod)
                        DetailFactRow("Fruit / Seed", plant.traits.fruitingDetails)
                        DetailFactRow("Native Range", plant.distribution.nativeRange)
                        DetailFactRow("Global Occurrences", plant.distribution.recordedGlobalDistribution)
                        DetailFactRow("IUCN Status", plant.distribution.conservationStatusIucn)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSourcesDialog = true },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Sources", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { onAddToGarden(plant) },
                        modifier = Modifier.weight(1.2f).height(44.dp).testTag("add_aggregated_to_garden_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Garden", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Dialog displaying database provenance and references
    if (showSourcesDialog) {
        Dialog(onDismissRequest = { showSourcesDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Botanical Data Sources",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showSourcesDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "PlantWise aggregates scientific taxonomy, nomenclatural status, and horticultural data from verified international botanical authorities:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    plant.provenanceList.forEach { prov ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prov.source.displayName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    SourceBadgeChip(prov.source, isSmall = true)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Record ID: ${prov.recordIdentifier}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Synchronized: ${prov.retrievalDateFormatted}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Fields Supplied: " + prov.fieldsProvided.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (prov.recordUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.clickable {
                                            try { uriHandler.openUri(prov.recordUrl) } catch (_: Exception) {}
                                        },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Open Botanical Record Reference",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showSourcesDialog = false },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close Provenance Details")
                    }
                }
            }
        }
    }
}

@Composable
fun SourceBadgeChip(
    source: BotanicalSource,
    isSmall: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(source.primaryColorHex).copy(alpha = 0.12f))
            .border(0.8.dp, Color(source.primaryColorHex).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
        .padding(horizontal = if (isSmall) 6.dp else 8.dp, vertical = if (isSmall) 2.dp else 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = source.shortBadge,
            fontSize = if (isSmall) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(source.primaryColorHex)
        )
    }
}

@Composable
private fun DetailFactRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.3f)
        )
    }
}
