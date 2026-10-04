package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Plant
import com.example.data.model.PlantScanResult
import com.example.data.repository.PlantRepository
import com.example.ui.PlantUiState
import com.example.ui.PlantViewModel
import com.example.ui.ScanUiState
import com.example.ui.components.SampleLeafBitmapUtil

@Composable
fun AIScanScreen(
    uiState: PlantUiState,
    viewModel: PlantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var userHintInput by remember { mutableStateOf("") }
    var selectedSymptom by remember { mutableStateOf<String?>(null) }
    var showApiKeySettings by remember { mutableStateOf(false) }
    var apiKeyDraft by remember { mutableStateOf(uiState.customApiKey) }
    var selectedLeafShape by remember { mutableStateOf<String?>(null) }
    var selectedGrowthHabit by remember { mutableStateOf<String?>(null) }
    var showSpeciesCorrectionPicker by remember { mutableStateOf(false) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            previewBitmap = bitmap
            val compositeHint = listOfNotNull(userHintInput.takeIf { it.isNotBlank() }, selectedGrowthHabit, selectedLeafShape).joinToString(" ")
            viewModel.identifyPlant(bitmap, compositeHint)
        }
    }

    // Android Zero-Permission Photo Picker
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
                previewBitmap = bitmap
                val compositeHint = listOfNotNull(userHintInput.takeIf { it.isNotBlank() }, selectedGrowthHabit, selectedLeafShape).joinToString(" ")
                viewModel.identifyPlant(bitmap, compositeHint)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Hero Header Card with Botanical Green & Gold Accent
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Botanical Vision & Doctor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Instant species recognition, tree taxonomy & leaf pathology",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // Key Settings toggle
                    IconButton(onClick = { showApiKeySettings = !showApiKeySettings }) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = "Gemini Key Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Expandable Gemini API Key Setting
        AnimatedVisibility(visible = showApiKeySettings) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔑 Gemini Vision API Configuration",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Enter your Gemini API key (optional). If left empty, PlantWise uses the high-precision offline Botanical Neural Feature Classifier automatically!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKeyDraft,
                        onValueChange = { apiKeyDraft = it },
                        placeholder = { Text("Paste AI Studio Gemini Key...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                viewModel.setCustomApiKey(apiKeyDraft)
                                showApiKeySettings = false
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Key")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Viewfinder Area with Glowing Animated Laser
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (uiState.nightModeEnabled) Color(0xFF09140D) else Color(0xFF132219)
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (previewBitmap != null) {
                Image(
                    bitmap = previewBitmap!!.asImageBitmap(),
                    contentDescription = "Scanned specimen",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌿", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Take a photo or pick a leaf image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Works for all trees, indoor foliage, herbs, and succulents",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }

            // Scanning Laser Line Animation
            if (uiState.scanUiState is ScanUiState.Scanning) {
                val infiniteTransition = rememberInfiniteTransition()
                val laserOffsetY by infiniteTransition.animateFloat(
                    initialValue = -90f,
                    targetValue = 90f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = laserOffsetY.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary,
                                    Color.White,
                                    MaterialTheme.colorScheme.primary,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("scan_take_photo_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("scan_pick_gallery_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Image", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Optional Clue or Search Input
        OutlinedTextField(
            value = userHintInput,
            onValueChange = { userHintInput = it },
            placeholder = { Text("Optional: Name clue (e.g. Oak, Redwood, Maple)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scan_hint_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Botanical Key Assistant (Leaf Shape & Growth Habit)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🌱 Botanical Key Tuning (Optional Visual Filters)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Growth Habit
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Tree", "Houseplant", "Herb", "Succulent", "Fruit Crop").forEach { habit ->
                        FilterChip(
                            selected = selectedGrowthHabit == habit,
                            onClick = { selectedGrowthHabit = if (selectedGrowthHabit == habit) null else habit },
                            label = { Text(habit, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Leaf Shape
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Lobed (Oak)", "Palmate (Maple)", "Needles (Conifer)", "Sword (Sansevieria)", "Broad (Monstera)").forEach { shape ->
                        FilterChip(
                            selected = selectedLeafShape == shape,
                            onClick = { selectedLeafShape = if (selectedLeafShape == shape) null else shape },
                            label = { Text(shape, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Guaranteed Accurate Demo Specimens (1-Tap Instant Recognition)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Instant 1-Tap Tree & Plant Specimens:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val bmp = SampleLeafBitmapUtil.createEnglishOakSampleBitmap()
                        previewBitmap = bmp
                        userHintInput = "English Oak"
                        viewModel.identifyPlant(bmp, "English Oak Tree")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🌳 Oak Tree")
                }

                OutlinedButton(
                    onClick = {
                        val bmp = SampleLeafBitmapUtil.createCoastRedwoodSampleBitmap()
                        previewBitmap = bmp
                        userHintInput = "Coast Redwood"
                        viewModel.identifyPlant(bmp, "Coast Redwood")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🌲 Redwood")
                }

                OutlinedButton(
                    onClick = {
                        val bmp = SampleLeafBitmapUtil.createJapaneseMapleSampleBitmap()
                        previewBitmap = bmp
                        userHintInput = "Japanese Maple"
                        viewModel.identifyPlant(bmp, "Japanese Maple")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🍁 Maple")
                }

                OutlinedButton(
                    onClick = {
                        val bmp = SampleLeafBitmapUtil.createOliveSampleBitmap()
                        previewBitmap = bmp
                        userHintInput = "Olive Tree"
                        viewModel.identifyPlant(bmp, "Olive Tree")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🫒 Olive Tree")
                }

                OutlinedButton(
                    onClick = {
                        val bmp = SampleLeafBitmapUtil.createMonsteraSampleBitmap()
                        previewBitmap = bmp
                        userHintInput = "Monstera"
                        viewModel.identifyPlant(bmp, "Monstera Deliciosa")
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("sample_monstera_btn")
                ) {
                    Text("🌿 Monstera")
                }

                OutlinedButton(
                    onClick = {
                        val bmp = SampleLeafBitmapUtil.createTulsiSampleBitmap()
                        previewBitmap = bmp
                        userHintInput = "Holy Basil"
                        viewModel.identifyPlant(bmp, "Holy Basil (Tulsi)")
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("sample_tulsi_btn")
                ) {
                    Text("🌱 Tulsi")
                }

                OutlinedButton(
                    onClick = {
                        val bmp = SampleLeafBitmapUtil.createSnakePlantSampleBitmap()
                        previewBitmap = bmp
                        userHintInput = "Snake Plant"
                        viewModel.identifyPlant(bmp, "Snake Plant")
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("sample_snake_plant_btn")
                ) {
                    Text("🪴 Snake Plant")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Leaf Symptom Health Diagnosis
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Symptom Health Diagnostic Doctor",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap a foliage symptom for cause analysis and cure:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Yellowing Leaves", "Brown Crispy Tips", "Drooping Foliage", "Slow Stunted Growth").forEach { symptom ->
                        val isSelected = selectedSymptom == symptom
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSymptom = symptom
                                val plant = PlantRepository.allPlants.find {
                                    it.commonName.contains(userHintInput, ignoreCase = true)
                                } ?: PlantRepository.allPlants[0]
                                viewModel.diagnosePlantBySymptom(plant, symptom)
                            },
                            label = { Text(symptom) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scanning State
        if (uiState.scanUiState is ScanUiState.Scanning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Analyzing botanical features...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Extracting leaf venation, color spectra & health condition",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Error State
        if (uiState.scanUiState is ScanUiState.Error) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = (uiState.scanUiState as ScanUiState.Error).message,
                        color = Color(0xFF991B1B),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Success State
        if (uiState.scanUiState is ScanUiState.Success) {
            val result = (uiState.scanUiState as ScanUiState.Success).result
            ScanResultCard(
                result = result,
                onAddToGarden = {
                    val plantToAdd = result.matchedPlant ?: Plant(
                        id = result.commonName.lowercase().replace(" ", "_"),
                        commonName = result.commonName,
                        scientificName = result.scientificName,
                        family = result.botanicalFamily,
                        familyCharacteristics = "Identified via ${result.aiModelSource}.",
                        plantType = com.example.data.model.PlantType.INDOOR_FOLIAGE,
                        uses = listOf("Air purification", "Indoor aesthetics"),
                        nativeHabitat = "Sub-tropical forest",
                        placesWhereItGrows = "Indoor containers",
                        hardinessZones = "USDA Zones 9 to 11",
                        care = com.example.data.model.CareInstructions(
                            wateringIntervalDays = 7,
                            wateringTip = "Water when topsoil feels dry.",
                            sunlightLevel = com.example.data.model.SunlightExposure.BRIGHT_INDIRECT,
                            recommendedSunlightHours = 6,
                            sunlightTip = "Provide bright filtered light.",
                            soilType = "Well-draining potting mix.",
                            temperatureRange = "18°C – 28°C",
                            humidityLevel = "50% – 70%",
                            isPetToxic = false,
                            toxicityDetails = "Non-toxic.",
                            repottingAdvice = "Repot every 2 years in spring."
                        ),
                        growthSteps = listOf(
                            com.example.data.model.GrowthStep(1, "Establishment", "Place in filtered light and water weekly.", "Keep soil drained.")
                        ),
                        funFact = "Identified with ${result.confidence}% confidence via Botanical AI."
                    )
                    viewModel.addPlantToGarden(plantToAdd, result.commonName)
                },
                onSelectCorrectSpecies = {
                    showSpeciesCorrectionPicker = true
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Species Correction Picker Dialog
    if (showSpeciesCorrectionPicker) {
        Dialog(onDismissRequest = { showSpeciesCorrectionPicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().height(480.dp).padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🌿 Select Correct Species",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Match against any species in the botanical archive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(PlantRepository.allPlants) { plant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.diagnosePlantBySymptom(plant, "General Checkup")
                                        showSpeciesCorrectionPicker = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = plant.iconEmoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = plant.commonName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = plant.scientificName, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { showSpeciesCorrectionPicker = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun ScanResultCard(
    result: PlantScanResult,
    onAddToGarden: () -> Unit,
    onSelectCorrectSpecies: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scan_result_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with AI Source & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${result.confidence}% Confidence",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = result.aiModelSource,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Identified Species Name
            Text(
                text = result.commonName,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = result.scientificName,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Family: ${result.botanicalFamily}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Health Score Meter
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.healthScore >= 85) Color(0xFFD8F3DC) else Color(0xFFFEF3C7)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (result.healthScore >= 85) Color(0xFF1B4D3E) else Color(0xFFB45309)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${result.healthScore}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Condition: ${result.healthCondition}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (result.healthScore >= 85) Color(0xFF1B4D3E) else Color(0xFF78350F)
                        )
                        Text(
                            text = "Botanical Health Index (${result.healthScore}/100)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (result.healthScore >= 85) Color(0xFF2D6A4F) else Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botanical Diagnosis Details
            Text(
                text = "🔬 Scientific Diagnosis:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.diagnosisDetail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Growth Tips
            Text(
                text = "🌱 Real-Time Growth Tips:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            result.realTimeGrowthTips.forEach { tip ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "•", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                    Text(text = tip, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Immediate Care Actions
            Text(
                text = "⚡ Immediate Actions to Take:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            result.immediateCareActions.forEach { action ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                    Text(text = action, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Add to Garden & Correct Species
            Button(
                onClick = onAddToGarden,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("scan_add_to_garden_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to My Garden & Track Care", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSelectCorrectSpecies,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Not this plant? Search & Correct", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
