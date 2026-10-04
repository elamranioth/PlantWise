package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CareInstructions
import com.example.data.model.GrowthStep
import com.example.data.model.Plant
import com.example.data.model.PlantType
import com.example.data.model.SunlightExposure
import com.example.data.model.knowledge.AggregatedBotanicalPlant
import com.example.data.model.knowledge.BotanicalSource
import com.example.data.model.knowledge.TaxonomicMatchStatus
import com.example.ui.PlantUiState
import com.example.ui.PlantViewModel
import com.example.ui.components.SourceBadgeChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlantCatalogScreen(
    uiState: PlantUiState,
    viewModel: PlantViewModel,
    modifier: Modifier = Modifier
) {
    var showAddOrUpdateDialog by remember { mutableStateOf(false) }
    var plantBeingEdited by remember { mutableStateOf<Plant?>(null) }

    val families = remember(uiState.plantsCatalog) {
        uiState.plantsCatalog.map { it.family }.distinct()
    }

    val filteredPlants = remember(
        uiState.plantsCatalog,
        uiState.searchQuery,
        uiState.selectedFamilyFilter,
        uiState.selectedTypeFilter
    ) {
        uiState.plantsCatalog.filter { plant ->
            val matchesQuery = uiState.searchQuery.isBlank() ||
                    plant.commonName.contains(uiState.searchQuery, ignoreCase = true) ||
                    plant.scientificName.contains(uiState.searchQuery, ignoreCase = true) ||
                    plant.family.contains(uiState.searchQuery, ignoreCase = true) ||
                    plant.nativeHabitat.contains(uiState.searchQuery, ignoreCase = true) ||
                    plant.timberAndCraft.contains(uiState.searchQuery, ignoreCase = true) ||
                    plant.uses.any { it.contains(uiState.searchQuery, ignoreCase = true) }

            val matchesFamily = uiState.selectedFamilyFilter == null ||
                    plant.family == uiState.selectedFamilyFilter

            val matchesType = uiState.selectedTypeFilter == null ||
                    plant.plantType == uiState.selectedTypeFilter

            matchesQuery && matchesFamily && matchesType
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar with Clean Organic Styling
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { query ->
                viewModel.setSearchQuery(query)
                if (query.trim().length >= 2) {
                    viewModel.searchMultiSource(query.trim())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_plant_input"),
            placeholder = { Text("Search 50+ species, trees, GBIF, Kew, or care...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search Plants",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    IconButton(onClick = {
                        viewModel.setSearchQuery("")
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )

        // Live Cross-Database Search Progress Indicator
        if (uiState.isMultiSourceSearching) {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cross-querying GBIF, Kew POWO & Perenual databases...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Cross-Database Verified Result Banner (Requirement 3, 4, 8, 9)
        uiState.multiSourceSearchResult?.let { result ->
            Spacer(modifier = Modifier.height(8.dp))
            val isMatch = result.matchStatus == TaxonomicMatchStatus.CONFIRMED_MATCH
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectAggregatedPlant(result) }
                    .testTag("cross_database_result_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMatch) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMatch) Color(0xFF86EFAC) else Color(0xFFFDE68A)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(text = result.plantEmoji, fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = result.identity.commonName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF143D32)
                                )
                                Text(
                                    text = result.identity.acceptedScientificName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF28634D)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.95f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${result.matchStatus.symbol} ${result.matchStatus.label}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMatch) Color(0xFF15803D) else Color(0xFFB45309)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verified by: " + result.confirmingSources.joinToString { "${it.shortBadge} ✓" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F2937)
                        )

                        Text(
                            text = "View Intelligence Profile ↗",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Header Action Bar with "Add / Update Species" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Botanical & Tree Archive (${filteredPlants.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedButton(
                onClick = {
                    plantBeingEdited = null
                    showAddOrUpdateDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_custom_species_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Species", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botanical Family Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedFamilyFilter == null,
                onClick = { viewModel.setFamilyFilter(null) },
                label = { Text("All Families (${uiState.plantsCatalog.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("filter_chip_all_families")
            )

            families.forEach { family ->
                val shortFamilyName = family.substringBefore(" (")
                FilterChip(
                    selected = uiState.selectedFamilyFilter == family,
                    onClick = {
                        val next = if (uiState.selectedFamilyFilter == family) null else family
                        viewModel.setFamilyFilter(next)
                    },
                    label = { Text(shortFamilyName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Plant & Tree Type Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedTypeFilter == null,
                onClick = { viewModel.setTypeFilter(null) },
                label = { Text("All Types") }
            )

            PlantType.values().forEach { type ->
                FilterChip(
                    selected = uiState.selectedTypeFilter == type,
                    onClick = {
                        val next = if (uiState.selectedTypeFilter == type) null else type
                        viewModel.setTypeFilter(next)
                    },
                    label = { Text("${type.iconEmoji} ${type.label}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Plants List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Multi-Source Verified Botanical Intelligence Carousel
            if (uiState.allAggregatedPlants.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cross-Database Verified Species",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                SourceBadgeChip(BotanicalSource.GBIF, isSmall = true)
                                SourceBadgeChip(BotanicalSource.KEW_POWO, isSmall = true)
                                SourceBadgeChip(BotanicalSource.PERENUAL, isSmall = true)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            uiState.allAggregatedPlants.forEach { aggPlant ->
                                val isMatch = aggPlant.matchStatus == TaxonomicMatchStatus.CONFIRMED_MATCH
                                val isDiff = aggPlant.matchStatus == TaxonomicMatchStatus.SOURCES_DIFFER
                                Card(
                                    modifier = Modifier
                                        .width(230.dp)
                                        .clickable { viewModel.selectAggregatedPlant(aggPlant) }
                                        .testTag("verified_species_card_${aggPlant.id}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isDiff) Color(0xFFFBBF24) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = aggPlant.plantEmoji, fontSize = 24.sp)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isMatch) Color(0xFFDCFCE7) else if (isDiff) Color(0xFFFEF3C7) else Color(0xFFE0F2FE)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${aggPlant.matchStatus.symbol} ${aggPlant.matchStatus.label}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isMatch) Color(0xFF15803D) else if (isDiff) Color(0xFFB45309) else Color(0xFF0369A1)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = aggPlant.identity.commonName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = aggPlant.identity.acceptedScientificName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Confirmed: " + aggPlant.confirmingSources.joinToString { it.shortBadge },
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Full Living Botanical Catalog",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredPlants.size} Species",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items(filteredPlants, key = { it.id }) { plant ->
                PlantCatalogCard(
                    plant = plant,
                    onClick = { viewModel.selectPlantDetail(plant) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Comprehensive Plant & Tree Detail Dialog
    uiState.selectedPlantDetail?.let { plant ->
        ComprehensivePlantDetailDialog(
            plant = plant,
            onDismiss = { viewModel.selectPlantDetail(null) },
            onAddToGarden = { nickname ->
                viewModel.addPlantToGarden(plant, nickname)
                viewModel.selectPlantDetail(null)
            },
            onEditSpecies = {
                plantBeingEdited = plant
                showAddOrUpdateDialog = true
            }
        )
    }

    // Add or Update Species Dialog
    if (showAddOrUpdateDialog) {
        AddOrUpdatePlantSpeciesDialog(
            existingPlant = plantBeingEdited,
            onDismiss = { showAddOrUpdateDialog = false },
            onSave = { updatedPlant ->
                viewModel.addOrUpdateCatalogPlant(updatedPlant)
                showAddOrUpdateDialog = false
            }
        )
    }
}

@Composable
fun PlantCatalogCard(
    plant: Plant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("plant_catalog_card_${plant.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (plant.isTree) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = plant.iconEmoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plant.commonName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (plant.isTree) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Tree",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = plant.scientificName,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = plant.family,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💧 Every ${plant.care.wateringIntervalDays}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "☀️ ${plant.care.sunlightLevel.label.substringBefore(" ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "📏 ${plant.matureHeight.substringBefore(" ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =========================================================================
// Comprehensive Botanical & Dendrological Detail Dialog
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComprehensivePlantDetailDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onAddToGarden: (nickname: String) -> Unit,
    onEditSpecies: () -> Unit
) {
    var nicknameInput by remember { mutableStateOf(plant.commonName) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Edit and Close buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = plant.iconEmoji, fontSize = 30.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = plant.commonName,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = plant.scientificName,
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onEditSpecies) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit species data", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botanical Taxonomy Hierarchy Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔬 Scientific Classification & Taxonomy",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kingdom: ${plant.kingdom} • Division: ${plant.division}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Class: ${plant.classTaxon} • Order: ${plant.order} • Family: ${plant.family}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Characteristics: " + plant.familyCharacteristics,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dendrology & Structural Architecture (Height, Canopy, Trunk, Bark, Wood)
                DetailSection(title = "🌳 Dendrology & Tree Architecture") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoMetricBox(label = "Mature Height", value = plant.matureHeight, modifier = Modifier.weight(1f))
                        InfoMetricBox(label = "Canopy Spread", value = plant.canopySpread, modifier = Modifier.weight(1f))
                        InfoMetricBox(label = "Trunk Diameter", value = plant.trunkDiameter, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "🪵 Bark: ${plant.barkTexture}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "🪓 Wood Density: ${plant.woodDensity}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "🌱 Root System: ${plant.rootSystem}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "⏳ Lifespan: ${plant.lifespan} • Growth Rate: ${plant.growthRate}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phenology, Leaves & Flowering
                DetailSection(title = "🌸 Phenology, Foliage & Reproduction") {
                    Text(text = "🍃 Foliage: ${plant.foliageLongevity}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "🌿 Leaf Venation: ${plant.leafVenation}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "🌸 Bloom Season: ${plant.floweringSeason} (${plant.flowerColor})", style = MaterialTheme.typography.bodySmall)
                    Text(text = "🌰 Fruit/Cone: ${plant.fruitOrCone}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "🐝 Pollination: ${plant.pollinationMechanism}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Environmental Requirements & Soil
                DetailSection(title = "🌍 Habitat, Soil & Climate Tolerances") {
                    Text(text = "Native Habitat: ${plant.nativeHabitat}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(text = plant.placesWhereItGrows, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Hardiness Zones: ${plant.hardinessZones}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                    Text(text = "Soil Blend: ${plant.care.soilType} (pH: ${plant.soilPhRange})", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Drought Tolerance: ${plant.droughtTolerance} • Wind Resilience: ${plant.windResistance}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ecological Keystone Metrics
                DetailSection(title = "🌿 Ecological Impact & Wildlife Keystone") {
                    Text(text = "CO₂ Sequestration: ${plant.carbonSequestration}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Wildlife Value: ${plant.wildlifeValue}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Ecological Role: ${plant.ecologicalRole}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Human Uses, Timber & Medicinal
                DetailSection(title = "✨ Human Uses, Timber & Pharmacology") {
                    Text(text = "Timber & Craft: ${plant.timberAndCraft}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "Medicinal & Folk: ${plant.medicinalProperties}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        plant.uses.forEach { use ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = use, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Specific Care Instructions
                DetailSection(title = "🌱 Cultivation & Maintenance") {
                    CareInstructionRow(
                        icon = Icons.Default.WaterDrop,
                        title = "Watering",
                        value = "Every ${plant.care.wateringIntervalDays} days",
                        detail = plant.care.wateringTip,
                        iconTint = Color(0xFF0284C7)
                    )
                    CareInstructionRow(
                        icon = Icons.Default.WbSunny,
                        title = "Sunlight",
                        value = "${plant.care.sunlightLevel.label} (${plant.care.recommendedSunlightHours}h daily)",
                        detail = plant.care.sunlightTip,
                        iconTint = Color(0xFFF59E0B)
                    )
                    Text(text = "✂️ Pruning Season: ${plant.pruningSeason}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "🌱 Propagation: ${plant.propagationMethods}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "🌿 Companion Plants: ${plant.companionPlants}", style = MaterialTheme.typography.bodySmall)

                    // Pet Toxicity
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = if (plant.care.isPetToxic) Color(0xFFFEE2E2) else Color(0xFFD8F3DC)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (plant.care.isPetToxic) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (plant.care.isPetToxic) Color(0xFFDC2626) else Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = plant.care.toxicityDetails,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (plant.care.isPetToxic) Color(0xFF991B1B) else Color(0xFF064E3B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step-by-Step Growing Guide
                DetailSection(title = "📖 Step-by-Step Growing Guide") {
                    plant.growthSteps.forEach { step ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${step.stepNumber}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(step.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(step.instruction, style = MaterialTheme.typography.bodySmall)
                                Text("💡 Tip: ${step.expertTip}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botanical Fun Fact
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("💡 Botanical Fact", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(plant.funFact, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add to My Garden Input & Button
                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    label = { Text("Plant Nickname (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onAddToGarden(nicknameInput) },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("add_to_garden_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to My Garden & Track Care", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun InfoMetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        }
    }
}

// =========================================================================
// Add or Update Species Dialog
// =========================================================================
@Composable
fun AddOrUpdatePlantSpeciesDialog(
    existingPlant: Plant?,
    onDismiss: () -> Unit,
    onSave: (Plant) -> Unit
) {
    var commonName by remember { mutableStateOf(existingPlant?.commonName ?: "") }
    var scientificName by remember { mutableStateOf(existingPlant?.scientificName ?: "") }
    var family by remember { mutableStateOf(existingPlant?.family ?: "Plantae") }
    var isTree by remember { mutableStateOf(existingPlant?.isTree ?: false) }
    var matureHeight by remember { mutableStateOf(existingPlant?.matureHeight ?: "20 - 40 ft") }
    var nativeHabitat by remember { mutableStateOf(existingPlant?.nativeHabitat ?: "") }
    var wateringInterval by remember { mutableIntStateOf(existingPlant?.care?.wateringIntervalDays ?: 7) }
    var funFact by remember { mutableStateOf(existingPlant?.funFact ?: "") }
    var timberAndCraft by remember { mutableStateOf(existingPlant?.timberAndCraft ?: "") }
    var carbonSequestration by remember { mutableStateOf(existingPlant?.carbonSequestration ?: "~35 lbs CO₂/year") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (existingPlant == null) "➕ Add New Plant or Tree" else "✏️ Update Species Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Update the master botanical encyclopedia knowledge base",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = commonName,
                    onValueChange = { commonName = it },
                    label = { Text("Common Name (e.g. Douglas Fir)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = scientificName,
                    onValueChange = { scientificName = it },
                    label = { Text("Scientific Name (e.g. Pseudotsuga menziesii)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = family,
                    onValueChange = { family = it },
                    label = { Text("Botanical Family (e.g. Pinaceae)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = matureHeight,
                        onValueChange = { matureHeight = it },
                        label = { Text("Mature Height") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = "$wateringInterval",
                        onValueChange = { wateringInterval = it.toIntOrNull() ?: 7 },
                        label = { Text("Water Every (days)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nativeHabitat,
                    onValueChange = { nativeHabitat = it },
                    label = { Text("Native Habitat & Range") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = timberAndCraft,
                    onValueChange = { timberAndCraft = it },
                    label = { Text("Timber & Human Uses") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = funFact,
                    onValueChange = { funFact = it },
                    label = { Text("Botanical Fact") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val id = existingPlant?.id ?: commonName.lowercase().replace(" ", "_")
                            val newPlant = existingPlant?.copy(
                                commonName = commonName.ifBlank { "Plant Specimen" },
                                scientificName = scientificName.ifBlank { "Plantae sp." },
                                family = family,
                                matureHeight = matureHeight,
                                nativeHabitat = nativeHabitat,
                                funFact = funFact,
                                timberAndCraft = timberAndCraft
                            ) ?: Plant(
                                id = id,
                                commonName = commonName.ifBlank { "New Tree Specimen" },
                                scientificName = scientificName.ifBlank { "Flora sp." },
                                family = family,
                                familyCharacteristics = "Botanical species entry registered in local archive.",
                                plantType = if (isTree) PlantType.DECIDUOUS_TREE else PlantType.INDOOR_FOLIAGE,
                                uses = listOf(timberAndCraft.ifBlank { "Urban canopy enhancement and ecological carbon capture" }),
                                nativeHabitat = nativeHabitat.ifBlank { "Temperate forest regions" },
                                placesWhereItGrows = "Cultivated landscapes and gardens.",
                                hardinessZones = "USDA Zones 4 to 9",
                                care = CareInstructions(
                                    wateringIntervalDays = wateringInterval,
                                    wateringTip = "Water when topsoil dries.",
                                    sunlightLevel = SunlightExposure.DIRECT_SUN,
                                    recommendedSunlightHours = 6,
                                    sunlightTip = "Provide adequate sunlight.",
                                    soilType = "Well-draining loam.",
                                    temperatureRange = "15°C – 28°C",
                                    humidityLevel = "40% – 60%",
                                    isPetToxic = false,
                                    toxicityDetails = "Non-toxic.",
                                    repottingAdvice = "Repot every 2 years in spring."
                                ),
                                growthSteps = listOf(
                                    GrowthStep(1, "Establishment", "Plant in full sun and water weekly.", "Keep root flare elevated.")
                                ),
                                iconEmoji = if (isTree) "🌳" else "🌿",
                                funFact = funFact.ifBlank { "A magnificent botanical specimen." },
                                isTree = isTree,
                                matureHeight = matureHeight,
                                lifespan = "100+ years",
                                timberAndCraft = timberAndCraft,
                                carbonSequestration = carbonSequestration
                            )
                            onSave(newPlant)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Species", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun CareInstructionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    detail: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "$title: $value",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
