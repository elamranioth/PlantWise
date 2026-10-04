package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AlternativeMatch
import com.example.data.model.BotanicalEvidence
import com.example.data.model.ComprehensivePlantInfo
import com.example.data.model.CrossCheckAnalysis
import com.example.data.model.MedicinalUseProfile
import com.example.data.model.SourceComparisonItem
import com.example.data.model.TaxonomyVerification
import com.example.data.model.UserPlant
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlantInformationScreen(
    plantInfo: ComprehensivePlantInfo,
    userPlants: List<UserPlant>,
    onBack: () -> Unit,
    onAddToGarden: (info: ComprehensivePlantInfo, nickname: String, notes: String) -> Unit,
    onProvideFollowUpPhoto: ((Bitmap, String) -> Unit)? = null,
    isIdentifying: Boolean = false,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    fun saveBitmapToCache(bitmap: Bitmap): String {
        return try {
            val file = File(context.cacheDir, "detail_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            fos.flush()
            fos.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val cachedPath = saveBitmapToCache(bitmap)
            onProvideFollowUpPhoto?.invoke(bitmap, cachedPath)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val cachedPath = saveBitmapToCache(bitmap)
                onProvideFollowUpPhoto?.invoke(bitmap, cachedPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val isAlreadyInGarden = remember(userPlants, plantInfo) {
        userPlants.any {
            it.id == plantInfo.id ||
                    it.commonName.equals(plantInfo.commonName, ignoreCase = true) ||
                    it.scientificName.equals(plantInfo.scientificName, ignoreCase = true)
        }
    }

    var showAddToGardenDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("plant_info_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = plantInfo.commonName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )

                        // Top "Add to My Garden" Quick Action
                        if (!isAlreadyInGarden) {
                            Button(
                                onClick = { showAddToGardenDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("top_add_to_garden_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add to Garden", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "In My Garden",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("plant_info_scroll_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Hero Card: Most Likely Identification + Confidence + Photos
                    item {
                        PlantHeroCard(plantInfo = plantInfo)
                    }

                    // Primary Add to My Garden Banner (Always accessible at top)
                    item {
                        AddToGardenActionCard(
                            isAlreadyInGarden = isAlreadyInGarden,
                            onAddClick = { showAddToGardenDialog = true }
                        )
                    }

                    // Uncertainty Alert Card: If databases disagree or ambiguity exists
                    if (plantInfo.crossCheckAnalysis.isUncertain) {
                        item {
                            UncertainIdentificationAlertCard(
                                crossCheck = plantInfo.crossCheckAnalysis,
                                onTakePhoto = { cameraLauncher.launch(null) },
                                onUploadPhoto = {
                                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                        }
                    }

                    // Section 1: Cross-Database Comparison & Source Agreement
                    item {
                        CrossDatabaseComparisonCard(crossCheck = plantInfo.crossCheckAnalysis)
                    }

                    // Section 2: Alternative Realistic Matches
                    item {
                        AlternativeMatchesCard(
                            primarySpecies = plantInfo.scientificName,
                            alternatives = plantInfo.crossCheckAnalysis.alternativeMatches
                        )
                    }

                    // Section 3: Why the Plant Was Identified (Visible Botanical Traits)
                    item {
                        WhyIdentifiedBotanicalFeaturesCard(evidence = plantInfo.crossCheckAnalysis.botanicalEvidence)
                    }

                    // Section 4: Taxonomy Verification & Nomenclature
                    item {
                        TaxonomyVerificationCard(taxonomy = plantInfo.crossCheckAnalysis.taxonomyVerification)
                    }

                    // Section 5: Botanical Identity & Ecology
                    item {
                        BotanicalIdentityCard(plantInfo = plantInfo)
                    }

                    // Section 6: Complete Horticultural Care Requirements
                    item {
                        ComprehensiveCareCard(plantInfo = plantInfo)
                    }

                    // Section 7: Health, Safety & Toxicity
                    item {
                        HealthAndToxicityCard(plantInfo = plantInfo)
                    }

                    // Section 8: Doctor / Medicinal Uses (Strictly separated)
                    item {
                        MedicinalDoctorCard(medicinal = plantInfo.medicinalProfile)
                    }

                    // Section 9: Interesting Facts & General Uses
                    item {
                        FactsAndUsesCard(plantInfo = plantInfo)
                    }

                    // Section 10: Sources Checked (Expandable Transparency)
                    item {
                        SourcesCheckedExpandableCard(sources = plantInfo.crossCheckAnalysis.sourcesComparison)
                    }

                    // Final Bottom Action
                    item {
                        AddToGardenActionCard(
                            isAlreadyInGarden = isAlreadyInGarden,
                            onAddClick = { showAddToGardenDialog = true }
                        )
                    }
                }
            }

            // Scanning overlay during follow-up photo cross-checking
            if (isIdentifying) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🔬 Cross-checking additional photo...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Querying GBIF, Kew POWO & botanical consensus",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // Modal to customize nickname and initial personal notes before adding to My Garden
    if (showAddToGardenDialog) {
        SaveToGardenDialog(
            plantInfo = plantInfo,
            onDismiss = { showAddToGardenDialog = false },
            onConfirm = { nickname, notes ->
                onAddToGarden(plantInfo, nickname, notes)
                showAddToGardenDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlantHeroCard(plantInfo: ComprehensivePlantInfo) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo or Stylized Emoji Badge
                val localBitmap = remember(plantInfo.imageUriOrPath) {
                    if (plantInfo.imageUriOrPath.isNotBlank() && File(plantInfo.imageUriOrPath).exists()) {
                        BitmapFactory.decodeFile(plantInfo.imageUriOrPath)
                    } else null
                }

                if (localBitmap != null) {
                    Image(
                        bitmap = localBitmap.asImageBitmap(),
                        contentDescription = plantInfo.commonName,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = plantInfo.plantEmoji, fontSize = 38.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plantInfo.commonName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plantInfo.scientificName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plantInfo.botanicalFamily,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Other Common Names chips
            if (plantInfo.otherCommonNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Also known as:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    plantInfo.otherCommonNames.forEach { name ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddToGardenActionCard(
    isAlreadyInGarden: Boolean,
    onAddClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.BotanicalCard
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BotanicalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAlreadyInGarden) "Part of My Garden 🌿" else "Do you grow or own this plant?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.BotanicalTextPrimary
                )
                Text(
                    text = if (isAlreadyInGarden) "Tracked in your personal collection." else "Add to track watering, care schedule, and personal notes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = com.example.ui.theme.BotanicalTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAlreadyInGarden) com.example.ui.theme.BotanicalSurfaceElevated else com.example.ui.theme.BotanicalButtonPrimaryBg,
                    contentColor = if (isAlreadyInGarden) com.example.ui.theme.BotanicalPrimaryActive else com.example.ui.theme.BotanicalButtonPrimaryText
                ),
                modifier = Modifier.testTag("add_to_my_garden_main_btn")
            ) {
                Icon(
                    imageVector = if (isAlreadyInGarden) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAlreadyInGarden) "Saved" else "Add to My Garden",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BotanicalIdentityCard(plantInfo: ComprehensivePlantInfo) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌿", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Botanical Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (plantInfo.description.isNotBlank()) {
                Text(
                    text = plantInfo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(14.dp))
            }

            DetailRow(label = "Plant Type", value = plantInfo.plantType)
            DetailRow(label = "Native Origin & Distribution", value = plantInfo.nativeOriginAndDistribution)
            DetailRow(label = "Typical Mature Size", value = plantInfo.typicalMatureSize)
            DetailRow(label = "Growth Characteristics", value = plantInfo.growthCharacteristics)
        }
    }
}

@Composable
private fun ComprehensiveCareCard(plantInfo: ComprehensivePlantInfo) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💧", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complete Care Requirements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            DetailRow(label = "Sunlight", value = plantInfo.sunlightRequirements)
            DetailRow(label = "Watering", value = plantInfo.wateringRequirements)
            DetailRow(label = "Soil & Substrate", value = plantInfo.soilRequirements)
            DetailRow(label = "Temperature Range", value = plantInfo.temperatureRange)
            DetailRow(label = "Humidity", value = plantInfo.humidityRequirements)
            DetailRow(label = "Fertilization", value = plantInfo.fertilization)
            DetailRow(label = "Pruning", value = plantInfo.pruning)
            DetailRow(label = "Repotting", value = plantInfo.repotting)
            DetailRow(label = "Propagation", value = plantInfo.propagation)
            DetailRow(label = "Flowering", value = plantInfo.floweringInformation)
        }
    }
}

@Composable
private fun HealthAndToxicityCard(plantInfo: ComprehensivePlantInfo) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🛡️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health, Pests & Toxicity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pet Safety Highlight Card
            val isPetWarning = plantInfo.toxicityToPets.contains("Toxic", ignoreCase = true) ||
                    plantInfo.toxicityToPets.contains("Danger", ignoreCase = true)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = com.example.ui.theme.BotanicalSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPetWarning) com.example.ui.theme.BotanicalStatusOverdue.copy(alpha = 0.5f) else com.example.ui.theme.BotanicalBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (isPetWarning) Icons.Default.Warning else Icons.Default.Pets,
                        contentDescription = null,
                        tint = if (isPetWarning) com.example.ui.theme.BotanicalStatusOverdue else com.example.ui.theme.BotanicalStatusHealthy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPetWarning) "Pet Toxicity Caution" else "Pet Friendly",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isPetWarning) com.example.ui.theme.BotanicalStatusOverdue else com.example.ui.theme.BotanicalStatusHealthy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = plantInfo.toxicityToPets,
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.BotanicalTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(label = "Toxicity to Humans", value = plantInfo.toxicityToHumans)
            DetailRow(label = "Common Pests", value = plantInfo.commonPests)
            DetailRow(label = "Common Diseases", value = plantInfo.commonDiseases)
            DetailRow(label = "Common Plant Problems", value = plantInfo.commonProblems)
            DetailRow(label = "Edibility", value = plantInfo.edibility)
        }
    }
}

/**
 * Doctor / Medicinal Uses Section
 * Clearly separates traditional/historical uses from research-supported evidence.
 * Disclaims medical advice.
 */
@Composable
private fun MedicinalDoctorCard(medicinal: MedicinalUseProfile) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doctor_medicinal_uses_section")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.theme.BotanicalSurfaceElevated)
                        .border(1.dp, com.example.ui.theme.BotanicalBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🩺", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Doctor / Medicinal Uses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ethnobotanical & Pharmacological Documentation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!medicinal.hasEstablishedUse || (medicinal.traditionalUses.isBlank() && medicinal.historicallyReportedUses.isBlank() && medicinal.researchSupportedUses.isBlank())) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No established medicinal use found.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                // Section A: Traditional & Historical Uses
                if (medicinal.traditionalUses.isNotBlank() || medicinal.historicallyReportedUses.isNotBlank()) {
                    Text(
                        text = "📜 Traditional & Historical Uses",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (medicinal.traditionalUses.isNotBlank()) {
                        Text(
                            text = medicinal.traditionalUses,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (medicinal.historicallyReportedUses.isNotBlank()) {
                        Text(
                            text = "Historical records: ${medicinal.historicallyReportedUses}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Section B: Research-Supported & Active Compounds
                if (medicinal.researchSupportedUses.isNotBlank() || medicinal.activeCompounds.isNotBlank()) {
                    Text(
                        text = "🔬 Scientific Research & Phytochemistry",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (medicinal.activeCompounds.isNotBlank()) {
                        Text(
                            text = "Active Compounds: ${medicinal.activeCompounds}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (medicinal.researchSupportedUses.isNotBlank()) {
                        Text(
                            text = medicinal.researchSupportedUses,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Section C: Parts Used
                if (medicinal.partsUsed.isNotBlank()) {
                    DetailRow(label = "Parts Traditionally Used", value = medicinal.partsUsed)
                }

                // Section D: Safety Concerns, Toxicity & Contraindications
                if (medicinal.knownToxicity.isNotBlank() || medicinal.contraindications.isNotBlank() || medicinal.safetyConcerns.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = com.example.ui.theme.BotanicalSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BotanicalStatusWarning.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⚠️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Safety, Toxicity & Contraindications",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = com.example.ui.theme.BotanicalStatusWarning
                                )
                            }
                            if (medicinal.knownToxicity.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toxicity: ${medicinal.knownToxicity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = com.example.ui.theme.BotanicalTextPrimary
                                )
                            }
                            if (medicinal.contraindications.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Contraindications: ${medicinal.contraindications}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = com.example.ui.theme.BotanicalTextSecondary
                                )
                            }
                            if (medicinal.safetyConcerns.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Safety Concerns: ${medicinal.safetyConcerns}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = com.example.ui.theme.BotanicalTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Non-Medical Advice Legal Disclaimer
            Text(
                text = "Disclaimer: This botanical information is for historical and educational reference only and is not medical advice. Never consume or apply wild plants without medical supervision.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun FactsAndUsesCard(plantInfo: ComprehensivePlantInfo) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Uses & Interesting Facts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (plantInfo.generalUses.isNotBlank()) {
                DetailRow(label = "General Uses", value = plantInfo.generalUses)
            }

            if (plantInfo.interestingFacts.isNotEmpty()) {
                Text(
                    text = "Did you know?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                plantInfo.interestingFacts.forEach { fact ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "•", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                        Text(
                            text = fact,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SaveToGardenDialog(
    plantInfo: ComprehensivePlantInfo,
    onDismiss: () -> Unit,
    onConfirm: (nickname: String, notes: String) -> Unit
) {
    var nickname by remember { mutableStateOf(plantInfo.commonName) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪴", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Add to My Garden",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = plantInfo.commonName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nickname (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = { Text("e.g. Monty, Sunny...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_dialog_nickname_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Personal Notes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("e.g. Growing on east windowsill, repotted in perlite mix...") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_dialog_notes_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onConfirm(nickname, notes) },
                        modifier = Modifier.testTag("save_dialog_confirm_btn")
                    ) {
                        Text("Save to Garden")
                    }
                }
            }
        }
    }
}
