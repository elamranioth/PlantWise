package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PlantAppTab
import com.example.ui.PlantViewModel
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.IdentifyScreen
import com.example.ui.screens.MyGardenScreen
import com.example.ui.screens.PlantInformationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PlantMint
import com.example.ui.theme.PlantPrimary
import com.example.ui.theme.PlantTextSecondary

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val plantViewModel: PlantViewModel = viewModel()
            val uiState by plantViewModel.uiState.collectAsState()
            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }

            // Notification permission launcher for Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* permission handled */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // Toast feedback
            LaunchedEffect(uiState.toastMessage) {
                uiState.toastMessage?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    plantViewModel.clearToast()
                }
            }

            // Handle back navigation
            BackHandler(enabled = uiState.activePlantInfo != null || uiState.selectedTab != PlantAppTab.MY_GARDEN) {
                when {
                    uiState.activePlantInfo != null -> plantViewModel.closeActivePlantInfo()
                    uiState.selectedTab != PlantAppTab.MY_GARDEN -> plantViewModel.selectTab(PlantAppTab.MY_GARDEN)
                }
            }

            val isDarkTheme = uiState.nightModeEnabled || isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (uiState.activePlantInfo != null) {
                    PlantInformationScreen(
                        plantInfo = uiState.activePlantInfo!!,
                        userPlants = uiState.userPlants,
                        onBack = { plantViewModel.closeActivePlantInfo() },
                        onAddToGarden = { info, nickname, notes ->
                            plantViewModel.addPlantToGardenFromInfo(info, nickname, notes)
                        },
                        onProvideFollowUpPhoto = { additionalBmp, additionalPath ->
                            plantViewModel.refineIdentificationWithFollowUpPhoto(additionalBmp, additionalPath)
                        },
                        isIdentifying = uiState.isIdentifying,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            PlantTopAppBar(
                                isNightMode = uiState.nightModeEnabled,
                                onToggleNightMode = { plantViewModel.toggleNightMode() },
                                onSettingsClick = { plantViewModel.openSettings(true) }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            PlantBottomNavigation(
                                selectedTab = uiState.selectedTab,
                                onTabSelected = { plantViewModel.selectTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        val screenModifier = Modifier.padding(innerPadding)
                        when (uiState.selectedTab) {
                            PlantAppTab.MY_GARDEN -> MyGardenScreen(
                                uiState = uiState,
                                viewModel = plantViewModel,
                                modifier = screenModifier
                            )
                            PlantAppTab.IDENTIFY -> IdentifyScreen(
                                uiState = uiState,
                                viewModel = plantViewModel,
                                modifier = screenModifier
                            )
                            PlantAppTab.SAVED -> HistoryScreen(
                                uiState = uiState,
                                viewModel = plantViewModel,
                                modifier = screenModifier
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlantTopAppBar(
    isNightMode: Boolean,
    onToggleNightMode: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = com.example.ui.theme.BotanicalBgPrimary,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PlantWise",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = com.example.ui.theme.BotanicalPrimaryActive
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "🌿", fontSize = 20.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Night Gardening Mode Toggle
                    IconButton(
                        onClick = onToggleNightMode,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("topbar_night_mode_toggle")
                    ) {
                        Icon(
                            if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Night Gardening Mode",
                            tint = com.example.ui.theme.BotanicalTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Settings & Updates Button
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("topbar_settings_btn")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings and App Updates",
                            tint = com.example.ui.theme.BotanicalTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            HorizontalDivider(
                color = com.example.ui.theme.BotanicalBorder,
                thickness = 0.8.dp
            )
        }
    }
}

/**
 * Modern Botanical Bottom Navigation
 * Perfectly redistributes 5 items evenly across width (weight 1f).
 * Custom stroke-unified vector icons, 11sp single-line typography,
 * compact vertical footprint, and full bottom-safe-area support.
 */
@Composable
fun PlantBottomNavigation(
    selectedTab: PlantAppTab,
    onTabSelected: (PlantAppTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = com.example.ui.theme.BotanicalBgSecondary,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                color = com.example.ui.theme.BotanicalBorder,
                thickness = 0.8.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlantAppTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab

                    if (tab == PlantAppTab.IDENTIFY) {
                        // Visually prominent Primary Action
                        val isIdentifySelected = selectedTab == PlantAppTab.IDENTIFY
                        Column(
                            modifier = Modifier
                                .weight(1.15f)
                                .fillMaxSize()
                                .clickable(
                                    role = Role.Tab,
                                    onClick = { onTabSelected(tab) }
                                )
                                .testTag("nav_tab_identify"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isIdentifySelected) {
                                            com.example.ui.theme.BotanicalButtonPrimaryBg
                                        } else {
                                            com.example.ui.theme.BotanicalSurfaceElevated
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = tab.iconResId),
                                        contentDescription = tab.label,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (isIdentifySelected) {
                                            com.example.ui.theme.BotanicalButtonPrimaryText
                                        } else {
                                            com.example.ui.theme.BotanicalNavActive
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tab.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIdentifySelected) {
                                            com.example.ui.theme.BotanicalButtonPrimaryText
                                        } else {
                                            com.example.ui.theme.BotanicalNavActive
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        val tabContentColor = if (isSelected) {
                            com.example.ui.theme.BotanicalNavActive
                        } else {
                            com.example.ui.theme.BotanicalNavInactive
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable(
                                    role = Role.Tab,
                                    onClick = { onTabSelected(tab) }
                                )
                                .testTag("nav_tab_${tab.name.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Icon Pill Container
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) com.example.ui.theme.BotanicalSurfaceElevated else Color.Transparent
                                    )
                                    .padding(horizontal = 12.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = tab.iconResId),
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(22.dp),
                                    tint = tabContentColor
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // Compact Navigation Label
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = tabContentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
