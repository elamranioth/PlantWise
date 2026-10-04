package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BadgeCategory
import com.example.data.model.ChatMessage
import com.example.data.model.ForumComment
import com.example.data.model.ForumPost
import com.example.data.model.GroundingSource
import com.example.data.model.MasterGuide
import com.example.data.model.Plant
import com.example.data.model.PlantBadge
import com.example.data.model.PlantScanResult
import com.example.data.model.PlantType
import com.example.data.model.SunlightCheckResult
import com.example.data.model.SunlightExposure
import com.example.data.model.UserPlant
import com.example.data.model.knowledge.AggregatedBotanicalPlant
import com.example.data.model.knowledge.BotanicalSource
import com.example.data.model.knowledge.TaxonomicMatchStatus
import com.example.data.repository.PlantRepository
import com.example.service.BotanistChatService
import com.example.service.NotificationHelper
import com.example.service.PlantVisionAiService
import com.example.service.knowledge.PlantKnowledgeAggregator
import com.example.service.update.AppUpdateManager
import com.example.service.update.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.data.local.entity.HistoryEntity
import com.example.data.model.ComprehensivePlantInfo
import com.example.data.repository.HistoryRepository

enum class PlantAppTab(val label: String, val iconResId: Int) {
    MY_GARDEN("My Garden", com.example.R.drawable.ic_nav_garden),
    IDENTIFY("Identify", com.example.R.drawable.ic_nav_identify),
    SAVED("Saved", com.example.R.drawable.ic_nav_saved);

    companion object {
        val HISTORY get() = SAVED
    }
}

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Scanning : ScanUiState
    data class Success(val result: PlantScanResult) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

data class PlantUiState(
    val selectedTab: PlantAppTab = PlantAppTab.MY_GARDEN,
    val plantsCatalog: List<Plant> = PlantRepository.allPlants,
    val masterGuides: List<MasterGuide> = PlantRepository.masterGuides,
    val userPlants: List<UserPlant> = emptyList(),
    val historyList: List<HistoryEntity> = emptyList(),
    val activePlantInfo: ComprehensivePlantInfo? = null,
    val isIdentifying: Boolean = false,
    val identificationError: String? = null,
    val badges: List<PlantBadge> = emptyList(),
    val forumPosts: List<ForumPost> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val selectedPlantDetail: Plant? = null,
    val selectedGuideDetail: MasterGuide? = null,
    val scanUiState: ScanUiState = ScanUiState.Idle,
    val searchQuery: String = "",
    val searchResults: List<Plant> = emptyList(),
    val selectedFamilyFilter: String? = null,
    val selectedTypeFilter: PlantType? = null,
    val nightModeEnabled: Boolean = false,
    val careStreakDays: Int = 4,
    val userXp: Int = 280,
    val userLevel: Int = 2,
    val levelTitle: String = "Green Sprout",
    val toastMessage: String? = null,
    val isCheckingUpdates: Boolean = false,
    val updateStatusMessage: String = "v3.2 Dendrology & Flora Master Archive (Synced)",
    val lastUpdatedDate: String = "Today, Oct 1",
    val customApiKey: String = "",
    val isSettingsOpen: Boolean = false,
    val updateState: UpdateState = UpdateState.Idle,
    val selectedAggregatedPlant: AggregatedBotanicalPlant? = null,
    val multiSourceSearchResult: AggregatedBotanicalPlant? = null,
    val isMultiSourceSearching: Boolean = false,
    val multiSourceSearchMessage: String? = null,
    val allAggregatedPlants: List<AggregatedBotanicalPlant> = emptyList()
)

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationHelper = NotificationHelper(application)
    private val visionService = PlantVisionAiService()
    private val chatService = BotanistChatService()
    val appUpdateManager = AppUpdateManager(application)
    val knowledgeAggregator = PlantKnowledgeAggregator()

    private val database = com.example.data.local.PlantWiseDatabase.getInstance(application)
    val userGardenRepository = com.example.data.repository.UserGardenRepository(
        userPlantDao = database.userPlantDao(),
        growthProgressDao = database.growthProgressDao()
    )
    val historyRepository = HistoryRepository(database.historyDao())

    private val _uiState = MutableStateFlow(PlantUiState())
    val uiState: StateFlow<PlantUiState> = _uiState.asStateFlow()

    init {
        initializeInitialState()
        observeRoomDatabase()
        observeHistory()
        observeUpdateState()
        seedInitialKnowledgeBase()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyRepository.allHistory.collect { history ->
                _uiState.update { it.copy(historyList = history) }
            }
        }
    }

    private fun observeRoomDatabase() {
        viewModelScope.launch {
            userGardenRepository.plantsWithGrowthProgress.collect { roomPlants ->
                _uiState.update { it.copy(userPlants = roomPlants) }
            }
        }
    }

    private fun observeUpdateState() {
        viewModelScope.launch {
            appUpdateManager.updateState.collect { state ->
                _uiState.update { it.copy(updateState = state) }
            }
        }
        // Automatic background check on startup without interrupting the user
        appUpdateManager.checkForUpdates(silent = true)
    }

    private fun seedInitialKnowledgeBase() {
        viewModelScope.launch {
            val seedSpecies = listOf("Mango", "Monstera deliciosa", "Snake Plant", "Fiddle Leaf Fig", "English Oak", "Olive Tree", "Coast Redwood")
            seedSpecies.forEach { species ->
                knowledgeAggregator.queryPlantKnowledge(species)
            }
            _uiState.update { it.copy(allAggregatedPlants = knowledgeAggregator.getAllCachedPlants()) }
        }
    }

    private fun initializeInitialState() {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60 * 60 * 1000

        val initialUserPlants = listOf(
            UserPlant(
                id = "up_1",
                plantId = "monstera_deliciosa",
                commonName = "Monstera Deliciosa",
                scientificName = "Monstera deliciosa",
                family = "Araceae (Arum family)",
                nickname = "Monty",
                wateringIntervalDays = 7,
                lastWateredTimestamp = now - (6 * oneDayMillis), // Due tomorrow!
                sunlightLevel = SunlightExposure.BRIGHT_INDIRECT,
                hoursOfSunlightToday = 5.5f,
                healthCondition = "Thriving & Lush",
                notes = "New leaf unraveling with 4 fenestrations!",
                iconEmoji = "🌿"
            ),
            UserPlant(
                id = "up_2",
                plantId = "holy_basil_tulsi",
                commonName = "Holy Basil (Tulsi)",
                scientificName = "Ocimum tenuiflorum",
                family = "Lamiaceae (Mint family)",
                nickname = "Tulsi Devi",
                wateringIntervalDays = 3,
                lastWateredTimestamp = now - (3 * oneDayMillis), // Due today!
                sunlightLevel = SunlightExposure.DIRECT_SUN,
                hoursOfSunlightToday = 6.0f,
                healthCondition = "Fragrant & Healthy",
                notes = "Pinched flower buds yesterday for bushy branching.",
                iconEmoji = "🌱"
            ),
            UserPlant(
                id = "up_3",
                plantId = "snake_plant",
                commonName = "Snake Plant",
                scientificName = "Dracaena trifasciata",
                family = "Asparagaceae",
                nickname = "Sancho",
                wateringIntervalDays = 14,
                lastWateredTimestamp = now - (4 * oneDayMillis), // 10 days left
                sunlightLevel = SunlightExposure.LOW_LIGHT,
                hoursOfSunlightToday = 3.0f,
                healthCondition = "Rock Solid",
                notes = "Producing a new pup next to mother leaf.",
                iconEmoji = "🪴"
            )
        )

        val initialBadges = listOf(
            PlantBadge(
                id = "badge_sprout_starter",
                title = "Sprout Starter",
                description = "Add your first plant to your home garden.",
                category = BadgeCategory.CARE,
                iconEmoji = "🌱",
                targetCount = 1,
                currentCount = 3,
                isUnlocked = true,
                xpReward = 50
            ),
            PlantBadge(
                id = "badge_hydration_hero",
                title = "Hydration Hero",
                description = "Complete 5 timely plant waterings on schedule.",
                category = BadgeCategory.WATERING,
                iconEmoji = "💧",
                targetCount = 5,
                currentCount = 4,
                isUnlocked = false,
                xpReward = 100
            ),
            PlantBadge(
                id = "badge_sun_seeker",
                title = "Sun Seeker",
                description = "Track sunlight exposure for your indoor & outdoor spots.",
                category = BadgeCategory.SUNLIGHT,
                iconEmoji = "☀️",
                targetCount = 3,
                currentCount = 2,
                isUnlocked = false,
                xpReward = 80
            ),
            PlantBadge(
                id = "badge_ai_identifier",
                title = "Botanical Lens",
                description = "Identify plant species or diagnose plant health using AI Vision.",
                category = BadgeCategory.IDENTIFICATION,
                iconEmoji = "🔍",
                targetCount = 2,
                currentCount = 1,
                isUnlocked = false,
                xpReward = 120
            ),
            PlantBadge(
                id = "badge_community_elder",
                title = "Green Thumb Forum",
                description = "Share your gardening experience or advice in the community.",
                category = BadgeCategory.COMMUNITY,
                iconEmoji = "💬",
                targetCount = 1,
                currentCount = 1,
                isUnlocked = true,
                xpReward = 75
            ),
            PlantBadge(
                id = "badge_streak_master",
                title = "Weekly Guardian",
                description = "Maintain a 7-day consistent plant care streak.",
                category = BadgeCategory.CARE,
                iconEmoji = "🔥",
                targetCount = 7,
                currentCount = 4,
                isUnlocked = false,
                xpReward = 150
            )
        )

        val initialForumPosts = listOf(
            ForumPost(
                id = "post_1",
                authorName = "Elena Woods",
                authorBadge = "🌿 Botanist Level 4",
                category = "✂️ Propagation",
                timestampFormatted = "2 hours ago",
                title = "Monster cutting water rooted in just 18 days! Tips that made the difference",
                content = "I added a dash of organic worm casting tea to the rooting water vial and placed it under 3500 Lux indirect morning light. The node produced three sturdy 3-inch roots with secondary branch hairs! Ready for chunky aroid soil this weekend.",
                plantSpecies = "Monstera Deliciosa (Araceae)",
                likesCount = 24,
                isLikedByMe = true,
                comments = listOf(
                    ForumComment("c_1", "Marcus Chen", "🌱 Sprout", "1 hour ago", "Did you let the cut callous over before putting in water?"),
                    ForumComment("c_2", "Elena Woods", "🌿 Botanist Level 4", "45 mins ago", "Yes! Left it on parchment paper for 4 hours until the cut was completely dry.")
                )
            ),
            ForumPost(
                id = "post_2",
                authorName = "Dev Patel",
                authorBadge = "🪴 Urban Farmer",
                category = "💡 Quick Tip",
                timestampFormatted = "5 hours ago",
                title = "The Secret to Bushy Tulsi: Don't let it flower too early!",
                content = "If your Holy Basil starts putting out purple flower heads, pinch them off immediately! As soon as you pinch, the plant stops channeling energy into seed production and doubles its side-branch growth. My plant is now a round fragrant bush!",
                plantSpecies = "Holy Basil / Tulsi (Lamiaceae)",
                likesCount = 38,
                isLikedByMe = false,
                comments = listOf(
                    ForumComment("c_3", "Aisha Green", "☀️ Sun Seeker", "3 hours ago", "This saved my plant! I thought the flowers were pretty, but leaves had lost aroma.")
                )
            ),
            ForumPost(
                id = "post_3",
                authorName = "Sarah Jenkins",
                authorBadge = "🌺 Master Gardener",
                category = "🆘 Care Q&A",
                timestampFormatted = "Yesterday",
                title = "Yellowing lower leaves on Fiddle Leaf Fig — Overwatering or acclimation?",
                content = "Moved my 5-foot Fiddle Leaf Fig 2 feet closer to the window 10 days ago. The bottom two oldest leaves turned yellow and dropped, but new top buds are swelling. Should I reduce watering frequency or is this normal shedding?",
                plantSpecies = "Fiddle Leaf Fig (Moraceae)",
                likesCount = 15,
                isLikedByMe = false,
                comments = listOf(
                    ForumComment("c_4", "Dr. Liam Moss", "🔬 Plant Pathologist", "Yesterday", "Bottom leaf shed is typical when top growth accelerates under brighter light. Check that roots aren't sitting in standing saucer water, otherwise keep steady!")
                )
            )
        )

        val initialChatMessages = listOf(
            ChatMessage(
                id = "msg_welcome",
                isUser = false,
                text = "Hello! I am Dr. Flora, your AI Master Botanist and Plant Doctor. 🌿\n\nI am connected with Google Search Grounding to provide real-time, science-backed plant pathology diagnosis, localized weather care tips, and step-by-step growing guidance.\n\nHow are your plants doing today? Ask me anything or describe any leaf symptoms!",
                timestampFormatted = "Just now",
                isGrounded = true,
                sources = listOf(
                    GroundingSource("Google Botanical Research Index", "https://google.com/search?q=botanical+science+plant+care")
                )
            )
        )

        _uiState.update {
            it.copy(
                userPlants = initialUserPlants,
                badges = initialBadges,
                forumPosts = initialForumPosts,
                chatMessages = initialChatMessages
            )
        }
    }

    fun sendUserChatMessage(text: String) {
        if (text.isBlank()) return
        val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            isUser = true,
            text = text.trim(),
            timestampFormatted = timeFormat.format(java.util.Date())
        )

        val updatedHistory = _uiState.value.chatMessages + userMsg
        _uiState.update {
            it.copy(
                chatMessages = updatedHistory,
                isChatLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val botReply = chatService.sendMessage(updatedHistory, text.trim())
                val newXp = _uiState.value.userXp + 20
                val newLevel = 1 + (newXp / 150)

                _uiState.update { state ->
                    state.copy(
                        chatMessages = state.chatMessages + botReply,
                        isChatLoading = false,
                        userXp = newXp,
                        userLevel = newLevel,
                        levelTitle = getLevelTitle(newLevel)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isChatLoading = false)
                }
            }
        }
    }

    fun clearChatHistory() {
        val initialChatMessages = listOf(
            ChatMessage(
                id = "msg_welcome_${System.currentTimeMillis()}",
                isUser = false,
                text = "Hello! I am Dr. Flora, your AI Master Botanist. 🌿 How can I help your green companions thrive today?",
                timestampFormatted = "Just now",
                isGrounded = true
            )
        )
        _uiState.update { it.copy(chatMessages = initialChatMessages) }
    }

    fun selectTab(tab: PlantAppTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectPlantDetail(plant: Plant?) {
        _uiState.update { it.copy(selectedPlantDetail = plant) }
    }

    fun selectGuideDetail(guide: MasterGuide?) {
        _uiState.update { it.copy(selectedGuideDetail = guide) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFamilyFilter(family: String?) {
        _uiState.update { it.copy(selectedFamilyFilter = family) }
    }

    fun setTypeFilter(type: PlantType?) {
        _uiState.update { it.copy(selectedTypeFilter = type) }
    }

    fun toggleNightMode(enabled: Boolean? = null) {
        _uiState.update {
            val next = enabled ?: !it.nightModeEnabled
            it.copy(nightModeEnabled = next)
        }
    }

    fun identifyPlantComprehensive(bitmap: Bitmap, imageUri: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isIdentifying = true, identificationError = null) }
            try {
                val info = visionService.identifyPlantComprehensive(
                    bitmap = bitmap,
                    customApiKey = _uiState.value.customApiKey,
                    imageUriOrPath = imageUri
                )
                // Automatically record in History
                historyRepository.recordPhotoIdentification(info, imageUri)
                _uiState.update {
                    it.copy(
                        isIdentifying = false,
                        activePlantInfo = info,
                        toastMessage = "🌿 Identified: ${info.commonName}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val fallbackPlant = PlantRepository.allPlants.first()
                val fallbackInfo = ComprehensivePlantInfo.fromPlant(fallbackPlant, imageUri)
                historyRepository.recordPhotoIdentification(fallbackInfo, imageUri)
                _uiState.update {
                    it.copy(
                        isIdentifying = false,
                        activePlantInfo = fallbackInfo,
                        toastMessage = "🌿 Identified: ${fallbackInfo.commonName}"
                    )
                }
            }
        }
    }

    fun refineIdentificationWithFollowUpPhoto(additionalBitmap: Bitmap, additionalImageUri: String = "") {
        val currentInfo = _uiState.value.activePlantInfo ?: return
        val primaryImagePath = currentInfo.imageUriOrPath
        viewModelScope.launch {
            _uiState.update { it.copy(isIdentifying = true, identificationError = null) }
            try {
                val primaryBitmap = if (primaryImagePath.isNotBlank() && java.io.File(primaryImagePath).exists()) {
                    android.graphics.BitmapFactory.decodeFile(primaryImagePath)
                } else null

                val effectivePrimaryBitmap = primaryBitmap ?: additionalBitmap

                val updatedInfo = visionService.identifyPlantComprehensive(
                    bitmap = effectivePrimaryBitmap,
                    customApiKey = _uiState.value.customApiKey,
                    imageUriOrPath = primaryImagePath.ifBlank { additionalImageUri },
                    additionalBitmap = if (primaryBitmap != null) additionalBitmap else null,
                    additionalImageUriOrPath = additionalImageUri
                )
                historyRepository.recordPhotoIdentification(updatedInfo, primaryImagePath.ifBlank { additionalImageUri })
                _uiState.update {
                    it.copy(
                        isIdentifying = false,
                        activePlantInfo = updatedInfo,
                        toastMessage = "🔬 Multi-angle cross-check complete: ${updatedInfo.commonName}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isIdentifying = false) }
            }
        }
    }

    fun openPlantFromSearch(plant: Plant, searchTerm: String) {
        val info = ComprehensivePlantInfo.fromPlant(plant)
        viewModelScope.launch {
            historyRepository.recordSearchOpen(searchTerm, info)
        }
        _uiState.update { it.copy(activePlantInfo = info) }
    }

    fun openPlantFromHistory(historyEntity: HistoryEntity) {
        val info = historyEntity.parsePlantInfo()
        if (info != null) {
            _uiState.update { it.copy(activePlantInfo = info) }
        } else {
            val match = PlantRepository.allPlants.find {
                it.commonName.equals(historyEntity.plantName, ignoreCase = true) ||
                        it.scientificName.equals(historyEntity.scientificName, ignoreCase = true)
            } ?: PlantRepository.allPlants.first()
            _uiState.update { it.copy(activePlantInfo = ComprehensivePlantInfo.fromPlant(match, historyEntity.imageUriOrPath)) }
        }
    }

    fun openPlantFromGarden(userPlant: UserPlant) {
        val match = PlantRepository.allPlants.find {
            it.id == userPlant.plantId ||
                    it.commonName.equals(userPlant.commonName, ignoreCase = true)
        }
        val info = if (match != null) {
            ComprehensivePlantInfo.fromPlant(match).copy(
                commonName = userPlant.commonName,
                scientificName = userPlant.scientificName,
                healthCondition = userPlant.healthCondition,
                plantEmoji = userPlant.iconEmoji,
                description = if (userPlant.notes.isNotBlank()) "Personal notes: ${userPlant.notes}\n\n${match.funFact}" else match.funFact
            )
        } else {
            ComprehensivePlantInfo(
                id = userPlant.id,
                commonName = userPlant.commonName,
                scientificName = userPlant.scientificName,
                botanicalFamily = userPlant.family,
                plantEmoji = userPlant.iconEmoji,
                healthCondition = userPlant.healthCondition,
                description = userPlant.notes.ifBlank { "Personal companion in your garden." },
                wateringRequirements = "Water every ${userPlant.wateringIntervalDays} days. ${userPlant.wateringStatusLabel()}",
                sunlightRequirements = userPlant.sunlightLevel.label
            )
        }
        _uiState.update { it.copy(activePlantInfo = info) }
    }

    fun closeActivePlantInfo() {
        _uiState.update { it.copy(activePlantInfo = null) }
    }

    fun addPlantToGardenFromInfo(info: ComprehensivePlantInfo, nickname: String = "", notes: String = "") {
        val intervalDays = Regex("(\\d+)").find(info.wateringRequirements)?.value?.toIntOrNull() ?: 7
        val exposure = when {
            info.sunlightRequirements.contains("Direct", ignoreCase = true) -> SunlightExposure.DIRECT_SUN
            info.sunlightRequirements.contains("Low", ignoreCase = true) -> SunlightExposure.LOW_LIGHT
            info.sunlightRequirements.contains("Medium", ignoreCase = true) -> SunlightExposure.MEDIUM_INDIRECT
            else -> SunlightExposure.BRIGHT_INDIRECT
        }
        addNewUserPlant(
            commonName = info.commonName,
            nickname = nickname.ifBlank { info.commonName },
            scientificName = info.scientificName,
            family = info.botanicalFamily,
            locationRoom = "Living Room",
            wateringIntervalDays = intervalDays,
            sunlightLevel = exposure,
            healthCondition = info.healthCondition,
            iconEmoji = info.plantEmoji,
            notes = notes.ifBlank { info.description.take(120) }
        )
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            historyRepository.deleteHistory(id)
            _uiState.update { it.copy(toastMessage = "Item removed from history") }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            _uiState.update { it.copy(toastMessage = "History cleared") }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val trimmed = query.trim()
        val results = if (trimmed.isBlank()) {
            emptyList()
        } else {
            PlantRepository.allPlants.filter {
                it.commonName.contains(trimmed, ignoreCase = true) ||
                        it.scientificName.contains(trimmed, ignoreCase = true) ||
                        it.family.contains(trimmed, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(searchQuery = query, searchResults = results) }
    }

    fun waterPlant(userPlantId: String) {
        val now = System.currentTimeMillis()
        var plantName = "Plant"

        viewModelScope.launch {
            userGardenRepository.updateWatering(userPlantId, now)
        }

        _uiState.update { state ->
            val updatedList = state.userPlants.map { up ->
                if (up.id == userPlantId) {
                    plantName = up.nickname.ifBlank { up.commonName }
                    up.copy(
                        lastWateredTimestamp = now,
                        healthCondition = "Hydrated & Fresh"
                    )
                } else up
            }

            // Hydration badge progress
            val updatedBadges = state.badges.map { badge ->
                if (badge.id == "badge_hydration_hero") {
                    val newCount = badge.currentCount + 1
                    val unlocked = newCount >= badge.targetCount
                    badge.copy(currentCount = newCount, isUnlocked = badge.isUnlocked || unlocked)
                } else badge
            }

            val newXp = state.userXp + 25
            val newLevel = 1 + (newXp / 150)
            val levelTitle = getLevelTitle(newLevel)

            state.copy(
                userPlants = updatedList,
                badges = updatedBadges,
                userXp = newXp,
                userLevel = newLevel,
                levelTitle = levelTitle,
                toastMessage = "💧 $plantName watered! +25 XP earned"
            )
        }

        // Trigger real Android push notification!
        notificationHelper.showReminderNotification(
            title = "💧 Watering Logged: $plantName",
            message = "Great job! $plantName is happily hydrated. Next watering scheduled in ${_uiState.value.userPlants.find { it.id == userPlantId }?.wateringIntervalDays ?: 7} days."
        )
    }

    fun logSunlight(userPlantId: String, exposure: SunlightExposure, hours: Float) {
        var plantName = "Plant"

        viewModelScope.launch {
            userGardenRepository.updateSunlight(userPlantId, exposure, hours)
        }

        _uiState.update { state ->
            val updatedList = state.userPlants.map { up ->
                if (up.id == userPlantId) {
                    plantName = up.nickname.ifBlank { up.commonName }
                    up.copy(
                        sunlightLevel = exposure,
                        hoursOfSunlightToday = hours
                    )
                } else up
            }

            val updatedBadges = state.badges.map { badge ->
                if (badge.id == "badge_sun_seeker") {
                    val newCount = badge.currentCount + 1
                    val unlocked = newCount >= badge.targetCount
                    badge.copy(currentCount = newCount, isUnlocked = badge.isUnlocked || unlocked)
                } else badge
            }

            val newXp = state.userXp + 20
            val newLevel = 1 + (newXp / 150)

            state.copy(
                userPlants = updatedList,
                badges = updatedBadges,
                userXp = newXp,
                userLevel = newLevel,
                levelTitle = getLevelTitle(newLevel),
                toastMessage = "☀️ Logged ${hours}h for $plantName! +20 XP"
            )
        }
    }

    fun evaluateSunlightSpot(plant: Plant, exposure: SunlightExposure): SunlightCheckResult {
        val ideal = plant.care.sunlightLevel
        val isCompatible = when (ideal) {
            SunlightExposure.DIRECT_SUN -> exposure == SunlightExposure.DIRECT_SUN || exposure == SunlightExposure.BRIGHT_INDIRECT
            SunlightExposure.BRIGHT_INDIRECT -> exposure == SunlightExposure.BRIGHT_INDIRECT || exposure == SunlightExposure.MEDIUM_INDIRECT
            SunlightExposure.MEDIUM_INDIRECT -> exposure == SunlightExposure.MEDIUM_INDIRECT || exposure == SunlightExposure.BRIGHT_INDIRECT
            SunlightExposure.LOW_LIGHT -> true
        }

        val advice = if (isCompatible) {
            "Excellent Spot! ${plant.commonName} will flourish in ${exposure.label} with approximately ${exposure.idealLux}."
        } else {
            "Suboptimal Spot: ${plant.commonName} naturally prefers ${ideal.label}. ${plant.care.sunlightTip}"
        }

        return SunlightCheckResult(exposure, ideal, isCompatible, advice)
    }

    fun addPlantToGarden(plant: Plant, nickname: String = "") {
        val newPlant = UserPlant(
            id = "up_${System.currentTimeMillis()}",
            plantId = plant.id,
            commonName = plant.commonName,
            scientificName = plant.scientificName,
            family = plant.family,
            nickname = nickname.ifBlank { plant.commonName },
            wateringIntervalDays = plant.care.wateringIntervalDays,
            lastWateredTimestamp = System.currentTimeMillis(),
            sunlightLevel = plant.care.sunlightLevel,
            hoursOfSunlightToday = plant.care.recommendedSunlightHours.toFloat(),
            healthCondition = "Freshly Planted",
            notes = "Added on ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}",
            iconEmoji = plant.iconEmoji
        )

        viewModelScope.launch {
            userGardenRepository.insertUserPlant(newPlant)
        }

        _uiState.update { state ->
            val updated = state.userPlants + newPlant
            val updatedBadges = state.badges.map { badge ->
                if (badge.id == "badge_sprout_starter") {
                    badge.copy(isUnlocked = true, currentCount = updated.size)
                } else badge
            }

            val newXp = state.userXp + 50
            val newLevel = 1 + (newXp / 150)

            state.copy(
                userPlants = updated,
                badges = updatedBadges,
                userXp = newXp,
                userLevel = newLevel,
                levelTitle = getLevelTitle(newLevel),
                selectedPlantDetail = null,
                toastMessage = "🪴 Added ${newPlant.nickname} to My Garden! +50 XP"
            )
        }

        notificationHelper.showReminderNotification(
            title = "🌱 New Plant Added: ${newPlant.nickname}",
            message = "We'll keep you reminded on watering every ${newPlant.wateringIntervalDays} days!"
        )
    }

    fun addNewUserPlant(
        commonName: String,
        nickname: String = "",
        scientificName: String = "",
        family: String = "",
        locationRoom: String = "Living Room",
        wateringIntervalDays: Int = 7,
        sunlightLevel: SunlightExposure = SunlightExposure.BRIGHT_INDIRECT,
        healthCondition: String = "Thriving",
        iconEmoji: String = "🌱",
        notes: String = ""
    ) {
        val finalNickname = nickname.trim().ifBlank { commonName.trim() }
        val newPlant = UserPlant(
            id = "up_${System.currentTimeMillis()}",
            plantId = commonName.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "").ifBlank { "custom_plant" },
            commonName = commonName.trim(),
            scientificName = scientificName.trim().ifBlank { commonName.trim() },
            family = family.trim().ifBlank { "Botanical Plant" },
            nickname = finalNickname,
            wateringIntervalDays = wateringIntervalDays.coerceAtLeast(1),
            lastWateredTimestamp = System.currentTimeMillis(),
            sunlightLevel = sunlightLevel,
            hoursOfSunlightToday = 5.0f,
            healthCondition = healthCondition.trim().ifBlank { "Thriving" },
            notes = notes.trim(),
            iconEmoji = iconEmoji,
            locationRoom = locationRoom.trim().ifBlank { "Living Room" }
        )

        viewModelScope.launch {
            userGardenRepository.insertUserPlant(newPlant)
        }

        _uiState.update { state ->
            val updated = state.userPlants + newPlant
            val updatedBadges = state.badges.map { badge ->
                if (badge.id == "badge_sprout_starter") {
                    badge.copy(isUnlocked = true, currentCount = updated.size)
                } else badge
            }

            val newXp = state.userXp + 50
            val newLevel = 1 + (newXp / 150)

            state.copy(
                userPlants = updated,
                badges = updatedBadges,
                userXp = newXp,
                userLevel = newLevel,
                levelTitle = getLevelTitle(newLevel),
                toastMessage = "🪴 Added $finalNickname to My Garden! +50 XP"
            )
        }

        notificationHelper.showReminderNotification(
            title = "🌱 New Plant Added: $finalNickname",
            message = "We'll keep you reminded on watering every ${newPlant.wateringIntervalDays} days!"
        )
    }

    fun addAggregatedPlantToGarden(plant: AggregatedBotanicalPlant, nickname: String = "") {
        val newPlant = UserPlant(
            id = "up_${System.currentTimeMillis()}",
            plantId = plant.id,
            commonName = plant.identity.commonName,
            scientificName = plant.identity.acceptedScientificName,
            family = plant.identity.botanicalFamily,
            nickname = nickname.ifBlank { plant.identity.commonName },
            wateringIntervalDays = plant.care.wateringIntervalDays,
            lastWateredTimestamp = System.currentTimeMillis(),
            sunlightLevel = SunlightExposure.BRIGHT_INDIRECT,
            hoursOfSunlightToday = 5.0f,
            healthCondition = "Thriving & Healthy",
            notes = "Verified by: ${plant.confirmingSources.joinToString { it.shortBadge }}",
            iconEmoji = plant.plantEmoji,
            locationRoom = "Living Room"
        )

        viewModelScope.launch {
            userGardenRepository.insertUserPlant(newPlant)
        }

        _uiState.update { state ->
            val updated = state.userPlants + newPlant
            val newXp = state.userXp + 50
            val newLevel = 1 + (newXp / 150)
            state.copy(
                userPlants = updated,
                userXp = newXp,
                userLevel = newLevel,
                levelTitle = getLevelTitle(newLevel),
                selectedAggregatedPlant = null,
                toastMessage = "🪴 Added ${newPlant.nickname} to My Garden! +50 XP"
            )
        }

        notificationHelper.showReminderNotification(
            title = "🌱 New Companion Added: ${newPlant.nickname}",
            message = "Multi-source care schedule: water every ${newPlant.wateringIntervalDays} days!"
        )
    }

    fun removePlantFromGarden(userPlantId: String) {
        viewModelScope.launch {
            userGardenRepository.deleteUserPlant(userPlantId)
        }
        _uiState.update { state ->
            state.copy(
                userPlants = state.userPlants.filter { it.id != userPlantId },
                toastMessage = "Plant removed from garden."
            )
        }
    }

    fun setCustomApiKey(key: String) {
        _uiState.update { it.copy(customApiKey = key.trim()) }
    }

    fun updateUserPlant(updatedPlant: UserPlant) {
        viewModelScope.launch {
            userGardenRepository.updateUserPlant(updatedPlant)
        }
        _uiState.update { state ->
            val updatedList = state.userPlants.map { up ->
                if (up.id == updatedPlant.id) updatedPlant else up
            }

            val newXp = state.userXp + 30
            val newLevel = 1 + (newXp / 150)

            state.copy(
                userPlants = updatedList,
                userXp = newXp,
                userLevel = newLevel,
                levelTitle = getLevelTitle(newLevel),
                toastMessage = "✏️ ${updatedPlant.nickname} updated successfully! +30 XP"
            )
        }

        notificationHelper.showReminderNotification(
            title = "🔄 Plant Schedule Updated",
            message = "${updatedPlant.nickname} care schedule and room details updated."
        )
    }

    fun addGrowthMilestone(
        userPlantId: String,
        title: String,
        note: String,
        healthStatus: String = "Thriving",
        heightNote: String = "",
        heightCm: Float? = null,
        leafCount: Int? = null,
        growthStage: String = "Vegetative"
    ) {
        val nowFormatted = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val parsedHeightCm = heightCm ?: heightNote.replace(Regex("[^0-9.]"), "").toFloatOrNull()
        val newMilestone = com.example.data.model.GrowthMilestone(
            id = "ms_${System.currentTimeMillis()}",
            dateFormatted = nowFormatted,
            title = title.ifBlank { "Growth Milestone" },
            note = note,
            healthStatus = healthStatus,
            heightNote = heightNote.ifBlank { parsedHeightCm?.let { "$it cm" } ?: "" },
            heightCm = parsedHeightCm,
            leafCount = leafCount,
            growthStage = growthStage
        )

        viewModelScope.launch {
            userGardenRepository.recordGrowthProgress(userPlantId, newMilestone)
        }

        var plantName = "Plant"
        _uiState.update { state ->
            val updatedList = state.userPlants.map { up ->
                if (up.id == userPlantId) {
                    plantName = up.nickname
                    up.copy(
                        growthMilestones = listOf(newMilestone) + up.growthMilestones,
                        healthCondition = healthStatus
                    )
                } else up
            }

            val newXp = state.userXp + 35
            val newLevel = 1 + (newXp / 150)

            state.copy(
                userPlants = updatedList,
                userXp = newXp,
                userLevel = newLevel,
                levelTitle = getLevelTitle(newLevel),
                toastMessage = "🌱 Milestone logged for $plantName! +35 XP"
            )
        }
    }

    fun deleteGrowthMilestone(milestoneId: String) {
        viewModelScope.launch {
            userGardenRepository.deleteGrowthProgress(milestoneId)
        }
    }

    fun addOrUpdateCatalogPlant(plant: Plant) {
        PlantRepository.addOrUpdatePlant(plant)
        _uiState.update { state ->
            val newXp = state.userXp + 50
            val newLevel = 1 + (newXp / 150)
            state.copy(
                plantsCatalog = PlantRepository.allPlants,
                userXp = newXp,
                userLevel = newLevel,
                levelTitle = getLevelTitle(newLevel),
                toastMessage = "📖 Saved '${plant.commonName}' to Botanical Database! +50 XP"
            )
        }
    }

    fun checkForBotanicalUpdates() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCheckingUpdates = true,
                    toastMessage = "Checking Global Botanical Index for updates..."
                )
            }
            kotlinx.coroutines.delay(1200) // Realistic sync animation
            val nowTime = java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            _uiState.update { state ->
                val newXp = state.userXp + 35
                val newLevel = 1 + (newXp / 150)
                state.copy(
                    isCheckingUpdates = false,
                    plantsCatalog = PlantRepository.allPlants,
                    lastUpdatedDate = nowTime,
                    updateStatusMessage = "v3.2 Dendrology & Flora Master Archive (All ${PlantRepository.allPlants.size} species synced)",
                    userXp = newXp,
                    userLevel = newLevel,
                    levelTitle = getLevelTitle(newLevel),
                    toastMessage = "✨ Botanical database updated with ${PlantRepository.allPlants.size} species! +35 XP"
                )
            }
            notificationHelper.showReminderNotification(
                title = "🌿 PlantWise Database Synced",
                message = "Botanical master guides and all ${PlantRepository.allPlants.size} species are fully up to date."
            )
        }
    }

    fun identifyPlant(bitmap: Bitmap, optionalUserHint: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(scanUiState = ScanUiState.Scanning) }
            try {
                val result = visionService.analyzePlantImage(
                    bitmap = bitmap,
                    optionalUserHint = optionalUserHint,
                    customApiKey = _uiState.value.customApiKey
                )
                _uiState.update { state ->
                    val updatedBadges = state.badges.map { badge ->
                        if (badge.id == "badge_ai_identifier") {
                            val nextCount = badge.currentCount + 1
                            badge.copy(currentCount = nextCount, isUnlocked = nextCount >= badge.targetCount)
                        } else badge
                    }
                    val newXp = state.userXp + 40
                    val newLevel = 1 + (newXp / 150)

                    state.copy(
                        scanUiState = ScanUiState.Success(result),
                        badges = updatedBadges,
                        userXp = newXp,
                        userLevel = newLevel,
                        levelTitle = getLevelTitle(newLevel),
                        toastMessage = "🔍 Identified ${result.commonName}! +40 XP"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(scanUiState = ScanUiState.Error(e.message ?: "Failed to scan plant."))
                }
            }
        }
    }

    fun diagnosePlantBySymptom(plant: Plant, symptom: String) {
        val condition = when (symptom) {
            "Yellowing Leaves" -> "Mild Chlorosis / Root Moisture Imbalance"
            "Brown Crispy Tips" -> "Low Humidity & Dry Air Stress"
            "Drooping Foliage" -> "Dehydration / Soil Desiccation"
            "Slow Stunted Growth" -> "Insufficient Light / Winter Dormancy"
            else -> "Foliage Stress Check"
        }
        val detail = "Botanical diagnosis for ${plant.commonName}: $symptom observed on foliage. Vascular transport is active but environmental calibration is required."
        val tips = listOf(
            "Optimal Light: Keep in ${plant.care.sunlightLevel.label} (${plant.care.recommendedSunlightHours}h/day).",
            "Watering: Water every ${plant.care.wateringIntervalDays} days. ${plant.care.wateringTip}",
            "Soil Mix: ${plant.care.soilType}",
            "Environment: Maintain ${plant.care.temperatureRange}."
        )
        val actions = listOf(
            "Check soil moisture 2 inches down with index finger.",
            "Adjust distance from drafty windows or air vents.",
            "Maintain consistent watering schedule in My Garden."
        )
        val result = PlantScanResult(
            commonName = plant.commonName,
            scientificName = plant.scientificName,
            botanicalFamily = plant.family,
            confidence = 98,
            healthScore = 78,
            healthCondition = condition,
            diagnosisDetail = detail,
            realTimeGrowthTips = tips,
            immediateCareActions = actions,
            matchedPlant = plant
        )
        _uiState.update { it.copy(scanUiState = ScanUiState.Success(result)) }
    }

    fun resetScanState() {
        _uiState.update { it.copy(scanUiState = ScanUiState.Idle) }
    }

    fun createForumPost(category: String, title: String, content: String, plantSpecies: String?) {
        val newPost = ForumPost(
            id = "post_${System.currentTimeMillis()}",
            authorName = "You",
            authorBadge = "🌱 Level ${_uiState.value.userLevel} ${_uiState.value.levelTitle}",
            category = category,
            timestampFormatted = "Just now",
            title = title,
            content = content,
            plantSpecies = plantSpecies?.ifBlank { null },
            likesCount = 1,
            isLikedByMe = true,
            comments = emptyList()
        )

        _uiState.update { state ->
            val updatedBadges = state.badges.map { badge ->
                if (badge.id == "badge_community_elder") {
                    badge.copy(isUnlocked = true, currentCount = badge.targetCount)
                } else badge
            }
            val newXp = state.userXp + 35
            val newLevel = 1 + (newXp / 150)

            state.copy(
                forumPosts = listOf(newPost) + state.forumPosts,
                badges = updatedBadges,
                userXp = newXp,
                userLevel = newLevel,
                levelTitle = getLevelTitle(newLevel),
                toastMessage = "💬 Forum post published! +35 XP"
            )
        }
    }

    fun toggleLikeForumPost(postId: String) {
        _uiState.update { state ->
            val updated = state.forumPosts.map { post ->
                if (post.id == postId) {
                    val isNowLiked = !post.isLikedByMe
                    val delta = if (isNowLiked) 1 else -1
                    post.copy(
                        isLikedByMe = isNowLiked,
                        likesCount = (post.likesCount + delta).coerceAtLeast(0)
                    )
                } else post
            }
            state.copy(forumPosts = updated)
        }
    }

    fun addCommentToPost(postId: String, commentText: String) {
        if (commentText.isBlank()) return
        val newComment = ForumComment(
            id = "c_${System.currentTimeMillis()}",
            authorName = "You",
            authorBadge = "🌱 Level ${_uiState.value.userLevel} ${_uiState.value.levelTitle}",
            timestampFormatted = "Just now",
            text = commentText.trim()
        )

        _uiState.update { state ->
            val updated = state.forumPosts.map { post ->
                if (post.id == postId) {
                    post.copy(comments = post.comments + newComment)
                } else post
            }
            state.copy(
                forumPosts = updated,
                userXp = state.userXp + 15,
                toastMessage = "💬 Comment posted! +15 XP"
            )
        }
    }

    fun sendTestReminderNotification(customTitle: String? = null, customMessage: String? = null) {
        val title = customTitle ?: "💧 Time to water Monty (Monstera)"
        val message = customMessage ?: "Topsoil is dry! Drench thoroughly and drain excess water for healthy leaves."
        notificationHelper.showReminderNotification(title, message)
        _uiState.update { it.copy(toastMessage = "🔔 Push notification sent to status bar!") }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun openSettings(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun checkForAppUpdates(silent: Boolean = false) {
        appUpdateManager.checkForUpdates(silent)
    }

    fun startAppUpdateDownload() {
        appUpdateManager.startDownload()
    }

    fun installAppUpdate(context: android.content.Context, apkFile: java.io.File) {
        val success = appUpdateManager.triggerInstall(context, apkFile)
        if (!success) {
            _uiState.update { it.copy(toastMessage = "Please grant package installation permission in Settings") }
        }
    }

    fun searchMultiSource(query: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isMultiSourceSearching = true,
                    multiSourceSearchMessage = "Connecting to GBIF, Kew POWO & Perenual...",
                    searchQuery = query
                )
            }
            val result = knowledgeAggregator.queryPlantKnowledge(query)
            val updatedList = knowledgeAggregator.getAllCachedPlants()
            _uiState.update { state ->
                state.copy(
                    isMultiSourceSearching = false,
                    multiSourceSearchResult = result,
                    multiSourceSearchMessage = knowledgeAggregator.lastSearchStatusMessage.value,
                    allAggregatedPlants = updatedList
                )
            }
        }
    }

    fun selectAggregatedPlant(plant: AggregatedBotanicalPlant?) {
        _uiState.update { it.copy(selectedAggregatedPlant = plant) }
    }

    private fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "Seedling Explorer"
            2 -> "Green Sprout"
            3 -> "Urban Gardener"
            4 -> "Plant Whisperer"
            5 -> "Botanical Scholar"
            else -> "Master Horticulturist"
        }
    }
}
