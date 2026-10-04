package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SunlightExposure
import com.example.data.model.UserPlant
import com.example.ui.PlantAppTab
import com.example.ui.PlantUiState
import com.example.ui.PlantViewModel

@Composable
fun MyGardenScreen(
    uiState: PlantUiState,
    viewModel: PlantViewModel,
    modifier: Modifier = Modifier
) {
    var selectedPlantForSunTracking by remember { mutableStateOf<UserPlant?>(null) }
    var plantToUpdate by remember { mutableStateOf<UserPlant?>(null) }
    var plantForMilestone by remember { mutableStateOf<UserPlant?>(null) }
    var showAddPlantDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val dueTodayCount = remember(uiState.userPlants) {
        uiState.userPlants.count { it.isWateringDue() }
    }

    val availableRooms = remember(uiState.userPlants) {
        uiState.userPlants.map { it.locationRoom }.distinct().filter { it.isNotBlank() }
    }

    val filteredPlants = remember(uiState.userPlants, selectedFilter) {
        when (selectedFilter) {
            "ALL" -> uiState.userPlants
            "DUE" -> uiState.userPlants.filter { it.isWateringDue() }
            "THRIVING" -> uiState.userPlants.filter { it.healthCondition.contains("Thriving", ignoreCase = true) }
            else -> uiState.userPlants.filter { it.locationRoom.equals(selectedFilter, ignoreCase = true) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Care Summary Banner
                GardenSummaryHeader(
                    userPlants = uiState.userPlants,
                    streakDays = uiState.careStreakDays
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Living Plants & Trees (${uiState.userPlants.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Room SQLite Database",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Category / Filter Chips
            if (uiState.userPlants.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All (${uiState.userPlants.size})") },
                            modifier = Modifier.testTag("filter_chip_all")
                        )

                        if (dueTodayCount > 0) {
                            FilterChip(
                                selected = selectedFilter == "DUE",
                                onClick = { selectedFilter = "DUE" },
                                label = { Text("Needs Water 💧 ($dueTodayCount)") },
                                modifier = Modifier.testTag("filter_chip_due")
                            )
                        }

                        val thrivingCount = uiState.userPlants.count { it.healthCondition.contains("Thriving", ignoreCase = true) }
                        if (thrivingCount > 0) {
                            FilterChip(
                                selected = selectedFilter == "THRIVING",
                                onClick = { selectedFilter = "THRIVING" },
                                label = { Text("Thriving 🌿 ($thrivingCount)") },
                                modifier = Modifier.testTag("filter_chip_thriving")
                            )
                        }

                        availableRooms.forEach { room ->
                            FilterChip(
                                selected = selectedFilter == room,
                                onClick = { selectedFilter = room },
                                label = { Text("📍 $room") }
                            )
                        }
                    }
                }
            }

            if (uiState.userPlants.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🪴", fontSize = 54.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Your Garden is Ready to Grow",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No plants saved in your local Room database yet. Tap below or use the floating button to add your first plant companion.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showAddPlantDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("empty_state_add_plant_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Your First Plant", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (filteredPlants.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔍", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No plants match the current filter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(onClick = { selectedFilter = "ALL" }) {
                                Text("Show All Plants")
                            }
                        }
                    }
                }
            } else {
                items(filteredPlants, key = { it.id }) { plant ->
                    UserPlantItemCard(
                        plant = plant,
                        onWater = { viewModel.waterPlant(plant.id) },
                        onTrackSunlight = { selectedPlantForSunTracking = plant },
                        onEdit = { plantToUpdate = plant },
                        onLogMilestone = { plantForMilestone = plant },
                        onRemove = { viewModel.removePlantFromGarden(plant.id) },
                        onOpenInfo = { viewModel.openPlantFromGarden(plant) }
                    )
                }
            }
        }

        // Add Plant Floating Action Button — Always visible, elevated, adding directly to Room DB
        FloatingActionButton(
            onClick = { showAddPlantDialog = true },
            shape = RoundedCornerShape(16.dp),
            containerColor = com.example.ui.theme.BotanicalButtonPrimaryBg,
            contentColor = com.example.ui.theme.BotanicalButtonPrimaryText,
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                defaultElevation = 2.dp,
                pressedElevation = 6.dp
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("garden_add_plant_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Plant", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Plant",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    // Add New Plant Dialog (persisting directly to Room DB)
    if (showAddPlantDialog) {
        AddPlantDialog(
            onDismiss = { showAddPlantDialog = false },
            onAddPlant = { commonName, nickname, scientificName, family, room, interval, sun, health, emoji, notes ->
                viewModel.addNewUserPlant(
                    commonName = commonName,
                    nickname = nickname,
                    scientificName = scientificName,
                    family = family,
                    locationRoom = room,
                    wateringIntervalDays = interval,
                    sunlightLevel = sun,
                    healthCondition = health,
                    iconEmoji = emoji,
                    notes = notes
                )
                showAddPlantDialog = false
            }
        )
    }

    // Sunlight Tracker Modal
    selectedPlantForSunTracking?.let { plant ->
        SunlightExposureDialog(
            plant = plant,
            onDismiss = { selectedPlantForSunTracking = null },
            onLogSunlight = { exposure, hours ->
                viewModel.logSunlight(plant.id, exposure, hours)
                selectedPlantForSunTracking = null
            }
        )
    }

    // Update Plant Modal (Full Update Feature)
    plantToUpdate?.let { plant ->
        UpdatePlantModal(
            plant = plant,
            onDismiss = { plantToUpdate = null },
            onSaveUpdate = { updatedPlant ->
                viewModel.updateUserPlant(updatedPlant)
                plantToUpdate = null
            },
            onOpenMilestone = {
                plantForMilestone = plant
                plantToUpdate = null
            }
        )
    }

    // Log Growth Milestone Modal
    plantForMilestone?.let { plant ->
        LogGrowthMilestoneDialog(
            plant = plant,
            onDismiss = { plantForMilestone = null },
            onSaveMilestone = { title, note, health, height, leafCount, growthStage ->
                viewModel.addGrowthMilestone(
                    userPlantId = plant.id,
                    title = title,
                    note = note,
                    healthStatus = health,
                    heightNote = height,
                    leafCount = leafCount,
                    growthStage = growthStage
                )
                plantForMilestone = null
            }
        )
    }
}

@Composable
fun GardenSummaryHeader(
    userPlants: List<UserPlant>,
    streakDays: Int
) {
    val dueTodayCount = userPlants.count { it.isWateringDue() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            com.example.ui.theme.BotanicalWelcomeGradientStart,
                            com.example.ui.theme.BotanicalWelcomeGradientEnd
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(1.dp, com.example.ui.theme.BotanicalBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome to your garden 🌿",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BotanicalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (dueTodayCount > 0) {
                            "💧 $dueTodayCount plant${if (dueTodayCount > 1) "s" else ""} due for watering today"
                        } else {
                            "🌿 All ${userPlants.size} plants are happily hydrated"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (dueTodayCount > 0) com.example.ui.theme.BotanicalStatusWarning else com.example.ui.theme.BotanicalPrimaryActive,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = com.example.ui.theme.BotanicalSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BotanicalBorder)
                ) {
                    Text(
                        text = "🔥 ${streakDays}d streak",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BotanicalSoftSage,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserPlantItemCard(
    plant: UserPlant,
    onWater: () -> Unit,
    onTrackSunlight: () -> Unit,
    onEdit: () -> Unit,
    onLogMilestone: () -> Unit,
    onRemove: () -> Unit,
    onOpenInfo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDue = plant.isWateringDue()
    var isHistoryExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_plant_card_${plant.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.BotanicalCard
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BotanicalBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar, Nickname & Scientific Name, Clean Overflow Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenInfo() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(com.example.ui.theme.BotanicalSurfaceElevated)
                        .border(
                            1.dp,
                            if (isDue) com.example.ui.theme.BotanicalStatusWarning.copy(alpha = 0.5f) else com.example.ui.theme.BotanicalBorder,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = plant.iconEmoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plant.nickname,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = com.example.ui.theme.BotanicalTextPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(com.example.ui.theme.BotanicalSurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = plant.locationRoom,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = com.example.ui.theme.BotanicalTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = plant.scientificName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Clean Overflow Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Plant Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("📖 View Full Plant Info") },
                            onClick = {
                                showMenu = false
                                onOpenInfo()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🌱 Record Milestone") },
                            onClick = {
                                showMenu = false
                                onLogMilestone()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("☀️ Track Sunlight") },
                            onClick = {
                                showMenu = false
                                onTrackSunlight()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🗑 Remove Plant", color = com.example.ui.theme.BotanicalStatusOverdue) },
                            onClick = {
                                showMenu = false
                                onRemove()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = com.example.ui.theme.BotanicalSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDue) com.example.ui.theme.BotanicalStatusWarning.copy(alpha = 0.5f) else com.example.ui.theme.BotanicalBorder
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = if (isDue) com.example.ui.theme.BotanicalStatusWarning else com.example.ui.theme.BotanicalStatusWater,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isDue) "Due today" else plant.wateringStatusLabel(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = com.example.ui.theme.BotanicalTextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = com.example.ui.theme.BotanicalSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BotanicalBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (plant.healthCondition.contains("Thriving", ignoreCase = true)) com.example.ui.theme.BotanicalStatusHealthy else com.example.ui.theme.BotanicalStatusWarning
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = plant.healthCondition,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = com.example.ui.theme.BotanicalTextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Milestone or Note preview
            if (plant.growthMilestones.isNotEmpty()) {
                val latest = plant.growthMilestones.first()
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    onClick = { isHistoryExpanded = !isHistoryExpanded },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "🌱", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${latest.dateFormatted}: ${latest.title}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isHistoryExpanded) "▲" else "▼",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isHistoryExpanded) {
                            Spacer(modifier = Modifier.height(4.dp))
                            plant.growthMilestones.forEach { m ->
                                Text(
                                    text = "• ${m.dateFormatted}: ${m.title} (${m.heightNote.ifBlank { m.growthStage }})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            } else if (plant.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 ${plant.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two clean, balanced, comfortable action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onWater,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("water_plant_btn_${plant.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.BotanicalButtonPrimaryBg,
                        contentColor = com.example.ui.theme.BotanicalButtonPrimaryText
                    )
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDue) "Water Now" else "Watered",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("edit_plant_btn_${plant.id}"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BotanicalBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = com.example.ui.theme.BotanicalTextPrimary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Manage",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

// =========================================================================
// Full Plant Update Modal Dialog
// =========================================================================
@Composable
fun UpdatePlantModal(
    plant: UserPlant,
    onDismiss: () -> Unit,
    onSaveUpdate: (UserPlant) -> Unit,
    onOpenMilestone: () -> Unit
) {
    var nickname by remember { mutableStateOf(plant.nickname) }
    var locationRoom by remember { mutableStateOf(plant.locationRoom) }
    var wateringInterval by remember { mutableIntStateOf(plant.wateringIntervalDays) }
    var selectedSunlight by remember { mutableStateOf(plant.sunlightLevel) }
    var healthCondition by remember { mutableStateOf(plant.healthCondition) }
    var heightDescription by remember { mutableStateOf(plant.heightDescription) }
    var potSize by remember { mutableStateOf(plant.potSize) }
    var notes by remember { mutableStateOf(plant.notes) }
    var markWateredNow by remember { mutableStateOf(false) }

    val roomOptions = listOf("Living Room", "Balcony", "Bedroom", "Office", "Patio", "Garden Bed", "Greenhouse")
    val healthOptions = listOf("Thriving", "Healthy & Stable", "Needs Water", "Mild Chlorosis", "Recovering", "Pest Treated")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
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
                    Column {
                        Text(
                            text = "✏️ Update Plant Details",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${plant.commonName} (${plant.scientificName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nickname
                Text(text = "Nickname", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth().testTag("update_plant_nickname_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location / Room
                Text(text = "Location / Room Spot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roomOptions.forEach { room ->
                        FilterChip(
                            selected = locationRoom == room,
                            onClick = { locationRoom = room },
                            label = { Text(room, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Watering Cadence (Interval Stepper)
                Text(
                    text = "Watering Frequency: Every $wateringInterval days",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 5, 7, 10, 14).forEach { days ->
                            FilterChip(
                                selected = wateringInterval == days,
                                onClick = { wateringInterval = days },
                                label = { Text("${days}d") }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (wateringInterval > 1) wateringInterval-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$wateringInterval", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        IconButton(
                            onClick = { wateringInterval++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Reset last watered
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mark as watered today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = markWateredNow,
                        onCheckedChange = { markWateredNow = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Health Status
                Text(text = "Current Health Condition", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    healthOptions.forEach { status ->
                        FilterChip(
                            selected = healthCondition == status,
                            onClick = { healthCondition = status },
                            label = { Text(status, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Height and Pot Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = heightDescription,
                        onValueChange = { heightDescription = it },
                        label = { Text("Plant Height") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = potSize,
                        onValueChange = { potSize = it },
                        label = { Text("Pot Size / Type") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Care Notes
                Text(text = "Care & Growth Notes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    placeholder = { Text("e.g. Unfurled new leaf, repotted into perlite mix...") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Link to Log Milestone
                OutlinedButton(
                    onClick = onOpenMilestone,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Record Growth Milestone & Photo Note")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val updated = plant.copy(
                                nickname = nickname.ifBlank { plant.commonName },
                                locationRoom = locationRoom,
                                wateringIntervalDays = wateringInterval,
                                lastWateredTimestamp = if (markWateredNow) System.currentTimeMillis() else plant.lastWateredTimestamp,
                                sunlightLevel = selectedSunlight,
                                healthCondition = healthCondition,
                                heightDescription = heightDescription,
                                potSize = potSize,
                                notes = notes
                            )
                            onSaveUpdate(updated)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Updates", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// Log Growth Milestone Dialog
// =========================================================================
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LogGrowthMilestoneDialog(
    plant: UserPlant,
    onDismiss: () -> Unit,
    onSaveMilestone: (title: String, note: String, health: String, height: String, leafCount: Int?, growthStage: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var height by remember { mutableStateOf(plant.heightDescription) }
    var health by remember { mutableStateOf(plant.healthCondition) }
    var growthStage by remember { mutableStateOf("Vegetative") }
    var leafCountText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "🌱 Record Growth Milestone",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Logging progress for ${plant.nickname} (saved to local Room database)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Growth Stage",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                val stages = listOf("Vegetative", "New Shoot / Leaf", "Repotted", "Budding", "Flowering", "Pruned")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    stages.forEach { stage ->
                        val isSelected = growthStage == stage
                        FilterChip(
                            selected = isSelected,
                            onClick = { growthStage = stage },
                            label = { Text(stage, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Milestone Title") },
                    placeholder = { Text("e.g. Unfurled 5th Fenestrated Leaf! 🌿") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height / Spread") },
                        placeholder = { Text("45 cm") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = leafCountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) leafCountText = it },
                        label = { Text("Leaf Count") },
                        placeholder = { Text("e.g. 8") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Observations & Notes") },
                    placeholder = { Text("Deep green color, healthy aerial root development...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
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
                            onSaveMilestone(
                                title.ifBlank { "Growth Milestone" },
                                note,
                                health,
                                height,
                                leafCountText.toIntOrNull(),
                                growthStage
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save to Room DB", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SunlightExposureDialog(
    plant: UserPlant,
    onDismiss: () -> Unit,
    onLogSunlight: (exposure: SunlightExposure, hours: Float) -> Unit
) {
    var selectedExposure by remember { mutableStateOf(plant.sunlightLevel) }
    var loggedHours by remember { mutableFloatStateOf(plant.hoursOfSunlightToday) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "☀️ Sunlight Exposure Tracker",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tracking spot lighting for ${plant.nickname} (${plant.commonName})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Spot Sunlight Level:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                SunlightExposure.values().forEach { exposure ->
                    val isSelected = selectedExposure == exposure
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedExposure = exposure },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSelected) "🔘" else "⚪",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${exposure.label} (${exposure.idealLux})",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = exposure.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Daily Sunlight Hours: ${loggedHours.toInt()}h",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(2f, 4f, 6f, 8f).forEach { hrs ->
                        OutlinedButton(
                            onClick = { loggedHours = hrs },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (loggedHours == hrs) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                            )
                        ) {
                            Text("${hrs.toInt()}h", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = { onLogSunlight(selectedExposure, loggedHours) }) {
                        Text("Save Sunlight Log", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// Add New Plant Dialog (persists directly into Room Database)
// =========================================================================
private data class PopularPlantPreset(
    val name: String,
    val scientific: String,
    val family: String,
    val wateringDays: Int,
    val sunlight: SunlightExposure,
    val emoji: String
)

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AddPlantDialog(
    onDismiss: () -> Unit,
    onAddPlant: (
        commonName: String,
        nickname: String,
        scientificName: String,
        family: String,
        locationRoom: String,
        wateringIntervalDays: Int,
        sunlightLevel: SunlightExposure,
        healthCondition: String,
        iconEmoji: String,
        notes: String
    ) -> Unit
) {
    val presets = remember {
        listOf(
            PopularPlantPreset("Monstera Deliciosa", "Monstera deliciosa", "Araceae", 7, SunlightExposure.BRIGHT_INDIRECT, "🌿"),
            PopularPlantPreset("Snake Plant", "Dracaena trifasciata", "Asparagaceae", 14, SunlightExposure.LOW_LIGHT, "🪴"),
            PopularPlantPreset("Golden Pothos", "Epipremnum aureum", "Araceae", 5, SunlightExposure.BRIGHT_INDIRECT, "🌱"),
            PopularPlantPreset("Peace Lily", "Spathiphyllum wallisii", "Araceae", 4, SunlightExposure.MEDIUM_INDIRECT, "🌸"),
            PopularPlantPreset("Fiddle Leaf Fig", "Ficus lyrata", "Moraceae", 7, SunlightExposure.BRIGHT_INDIRECT, "🍃"),
            PopularPlantPreset("Spider Plant", "Chlorophytum comosum", "Asparagaceae", 5, SunlightExposure.MEDIUM_INDIRECT, "🌿"),
            PopularPlantPreset("ZZ Plant", "Zamioculcas zamiifolia", "Araceae", 14, SunlightExposure.LOW_LIGHT, "🪴"),
            PopularPlantPreset("Aloe Vera", "Aloe barbadensis", "Asphodelaceae", 14, SunlightExposure.DIRECT_SUN, "🌵"),
            PopularPlantPreset("Lavender", "Lavandula angustifolia", "Lamiaceae", 5, SunlightExposure.DIRECT_SUN, "🌸"),
            PopularPlantPreset("Boston Fern", "Nephrolepis exaltata", "Nephrolepidaceae", 3, SunlightExposure.BRIGHT_INDIRECT, "🌿")
        )
    }

    var commonName by remember { mutableStateOf("Monstera Deliciosa") }
    var nickname by remember { mutableStateOf("Monty") }
    var scientificName by remember { mutableStateOf("Monstera deliciosa") }
    var family by remember { mutableStateOf("Araceae") }
    var locationRoom by remember { mutableStateOf("Living Room") }
    var wateringInterval by remember { mutableIntStateOf(7) }
    var selectedSunlight by remember { mutableStateOf(SunlightExposure.BRIGHT_INDIRECT) }
    var healthCondition by remember { mutableStateOf("Thriving") }
    var selectedEmoji by remember { mutableStateOf("🌿") }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    val roomOptions = listOf("Living Room", "Balcony", "Bedroom", "Office", "Kitchen", "Patio", "Greenhouse")
    val emojiOptions = listOf("🌱", "🌿", "🪴", "🌵", "🌸", "🍃", "🎋", "🌴", "🍀", "🌺")
    val healthOptions = listOf("Thriving", "Healthy & Stable", "Needs Care")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Plant Companion",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Saved locally to Room SQLite database",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedEmoji, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Popular Presets
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = commonName.equals(preset.name, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                commonName = preset.name
                                nickname = preset.name
                                scientificName = preset.scientific
                                family = preset.family
                                wateringInterval = preset.wateringDays
                                selectedSunlight = preset.sunlight
                                selectedEmoji = preset.emoji
                                nameError = false
                            },
                            label = { Text("${preset.emoji} ${preset.name}", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Plant Name Input
                Text(text = "Plant Species / Common Name *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = commonName,
                    onValueChange = {
                        commonName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Plant name cannot be empty") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_plant_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("e.g. Monstera Deliciosa, Snake Plant...") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nickname Input
                Text(text = "Nickname (Optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_plant_nickname_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("e.g. Monty, Greenie, Spiky...") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location / Room
                Text(text = "Location / Room", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roomOptions.forEach { room ->
                        FilterChip(
                            selected = locationRoom == room,
                            onClick = { locationRoom = room },
                            label = { Text(room, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Watering Cadence (Interval Stepper)
                Text(
                    text = "Watering Schedule: Every $wateringInterval days",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(3, 5, 7, 10, 14, 21).forEach { days ->
                            FilterChip(
                                selected = wateringInterval == days,
                                onClick = { wateringInterval = days },
                                label = { Text("${days}d") }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (wateringInterval > 1) wateringInterval-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$wateringInterval", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        IconButton(
                            onClick = { wateringInterval++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sunlight Requirement
                Text(text = "Sunlight Requirement", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SunlightExposure.values().forEach { exposure ->
                        FilterChip(
                            selected = selectedSunlight == exposure,
                            onClick = { selectedSunlight = exposure },
                            label = { Text(exposure.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Plant Icon Emoji
                Text(text = "Plant Avatar / Emoji", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojiOptions.forEach { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Health Status
                Text(text = "Initial Health Condition", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    healthOptions.forEach { status ->
                        FilterChip(
                            selected = healthCondition == status,
                            onClick = { healthCondition = status },
                            label = { Text(status, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                Text(text = "Plant Notes (Optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    placeholder = { Text("e.g. Bought at local nursery, repotted in ceramic pot...") }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (commonName.isBlank()) {
                                nameError = true
                            } else {
                                onAddPlant(
                                    commonName.trim(),
                                    nickname.trim().ifBlank { commonName.trim() },
                                    scientificName.trim().ifBlank { commonName.trim() },
                                    family.trim().ifBlank { "Botanical Plant" },
                                    locationRoom,
                                    wateringInterval,
                                    selectedSunlight,
                                    healthCondition,
                                    selectedEmoji,
                                    notes.trim()
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_plant_submit_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Garden", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
