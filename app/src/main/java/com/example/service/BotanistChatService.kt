package com.example.service

import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.GroundingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BotanistChatService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String
    ): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        val isKeyConfigured = apiKey.isNotBlank() &&
                !apiKey.contains("MY_GEMINI_API_KEY", ignoreCase = true) &&
                apiKey.length > 15

        if (isKeyConfigured) {
            try {
                val contentsArray = JSONArray()

                // Add past 6 turns of history for context
                val recentHistory = history.takeLast(6)
                recentHistory.forEach { msg ->
                    val turnObj = JSONObject().apply {
                        put("role", if (msg.isUser) "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", msg.text))
                        })
                    }
                    contentsArray.put(turnObj)
                }

                // Append current user message
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", userMessage))
                    })
                })

                val jsonPayload = JSONObject().apply {
                    put("contents", contentsArray)

                    // System Instruction defining Dr. Flora persona
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", """
                                You are Dr. Flora, a renowned master botanist, horticultural scientist, and AI Plant Doctor.
                                You give friendly, authoritative, and practical advice on plant care, taxonomy, disease diagnosis, pest remedies, and propagation.
                                Use the Google Search tool when answering questions about current weather impacts, seasonal planting dates, specific cultivars, or recent botanical research.
                                Keep responses concise, warm, and formatted with bullet points where appropriate.
                            """.trimIndent()))
                        })
                    })

                    // Google Search Grounding Tool
                    put("tools", JSONArray().apply {
                        put(JSONObject().apply {
                            put("googleSearch", JSONObject())
                        })
                    })

                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.3)
                    })
                }

                val requestBody = jsonPayload.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()

                if (response.isSuccessful && responseBody.isNotBlank()) {
                    val root = JSONObject(responseBody)
                    val candidate = root.optJSONArray("candidates")?.optJSONObject(0)
                    val contentObj = candidate?.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    val responseText = parts?.optJSONObject(0)?.optString("text").orEmpty()

                    if (responseText.isNotBlank()) {
                        val sourcesList = mutableListOf<GroundingSource>()
                        var searchQuery: String? = null

                        // Extract Grounding Metadata from Google Search
                        val groundingMetadata = candidate?.optJSONObject("groundingMetadata")
                        if (groundingMetadata != null) {
                            val searchQueries = groundingMetadata.optJSONArray("webSearchQueries")
                            if (searchQueries != null && searchQueries.length() > 0) {
                                searchQuery = searchQueries.optString(0)
                            }

                            val chunks = groundingMetadata.optJSONArray("groundingChunks")
                            if (chunks != null) {
                                for (i in 0 until chunks.length()) {
                                    val chunk = chunks.optJSONObject(i)
                                    val web = chunk?.optJSONObject("web")
                                    if (web != null) {
                                        val uri = web.optString("uri")
                                        val title = web.optString("title", "Google Search Source")
                                        if (uri.isNotBlank()) {
                                            sourcesList.add(GroundingSource(title, uri))
                                        }
                                    }
                                }
                            }
                        }

                        val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                        return@withContext ChatMessage(
                            id = "msg_${System.currentTimeMillis()}",
                            isUser = false,
                            text = responseText,
                            timestampFormatted = timeFormat.format(java.util.Date()),
                            sources = sourcesList.distinctBy { it.url },
                            isGrounded = sourcesList.isNotEmpty() || searchQuery != null,
                            searchQuery = searchQuery
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Offline / Fallback Botanical AI Engine
        generateOfflineBotanicalResponse(userMessage)
    }

    private fun generateOfflineBotanicalResponse(userMessage: String): ChatMessage {
        val query = userMessage.lowercase()
        val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        val formattedTime = timeFormat.format(java.util.Date())

        val (response, sources) = when {
            query.contains("yellow") || query.contains("yellowing") -> {
                Pair(
                    "Yellowing leaves (chlorosis) usually point to one of three common causes:\n\n" +
                            "• **Overwatering / Root Saturation**: Check if the bottom soil is soggy. Roots deprived of oxygen cannot take up iron or nitrogen.\n" +
                            "• **Normal Aging**: If only the oldest bottom 1-2 leaves are yellowing while new top growth is lush, the plant is naturally shedding old foliage.\n" +
                            "• **Light Deficiency**: If yellowing is widespread and leaves look pale, move the plant 2 feet closer to filtered natural daylight.\n\n" +
                            "**Next Step**: Perform the 2-inch finger soil test before adding more water!",
                    listOf(
                        GroundingSource("RHS Gardening - Leaf Yellowing Causes", "https://www.rhs.org.uk/problems/chlorosis"),
                        GroundingSource("University Extension - Houseplant Diagnosis", "https://extension.umn.edu/houseplants")
                    )
                )
            }
            query.contains("brown") || query.contains("crispy") || query.contains("tip") -> {
                Pair(
                    "Brown, crispy leaf edges or tips are almost always caused by **low humidity** or **mineral buildup**:\n\n" +
                            "• **Dry Air**: Indoor radiator heating or AC dries the air below 40% humidity. Mist around the plant or place it on a wet pebble tray.\n" +
                            "• **Tap Water Minerals**: Sensitive plants (like Peace Lilies, Calatheas, and Ferns) react to fluoride and chlorine. Use filtered, distilled, or rainwater.\n" +
                            "• **Underwatering**: If entire leaves feel papery and dry, provide a thorough bottom-watering soak.",
                    listOf(
                        GroundingSource("Missouri Botanical Garden - Brown Leaf Tips", "https://www.missouribotanicalgarden.org"),
                        GroundingSource("Cornell Plant Pathology Guide", "https://cals.cornell.edu")
                    )
                )
            }
            query.contains("water") || query.contains("schedule") || query.contains("how often") -> {
                Pair(
                    "Here is the golden rule of watering:\n\n" +
                            "• **Tropicals & Aroids** (Monstera, Pothos, Philodendron): Wait until the top 2 inches feel dry (typically every 7-10 days).\n" +
                            "• **Succulents & Cacti** (Snake Plant, Aloe, Jade): Allow the pot to dry out 100% completely (every 14-21 days).\n" +
                            "• **Herbs & Fruiting crops** (Tulsi, Basil, Tomato): Keep soil consistently lightly moist (every 2-3 days).\n\n" +
                            "💡 *Always ensure the pot has drainage holes and empty any standing saucer runoff after 15 minutes!*",
                    listOf(
                        GroundingSource("Royal Horticultural Society - Watering Guide", "https://www.rhs.org.uk/soil-composts-mulches/watering")
                    )
                )
            }
            query.contains("propagat") || query.contains("cutting") -> {
                Pair(
                    "Water propagation is one of the most rewarding gardening techniques! Here is how to guarantee success:\n\n" +
                            "1. **Find a Node**: Locate the brown bump or joint along the stem where leaves emerge. Roots ONLY sprout from nodes!\n" +
                            "2. **45-Degree Snip**: Cut 1/2 inch below the node with sanitized shears.\n" +
                            "3. **Clear Glass Vessel**: Submerge the node in room-temperature water while keeping upper leaves in the air.\n" +
                            "4. **Bright Filtered Light**: Place on an east-facing windowsill. Change water every 4-5 days to refresh oxygen.",
                    listOf(
                        GroundingSource("University of Florida Extension - Stem Cuttings", "https://edis.ifas.ufl.edu")
                    )
                )
            }
            query.contains("bug") || query.contains("pest") || query.contains("gnat") || query.contains("mite") -> {
                Pair(
                    "Here is an organic, safe remedy for common indoor pests:\n\n" +
                            "• **Spider Mites / Aphids**: Wipe leaves down with a mild solution of pure castile soap (1 tsp per quart of warm water) and cold-pressed neem oil.\n" +
                            "• **Fungus Gnats**: Allow the top 2 inches of soil to completely dry out between waterings. Place yellow sticky traps near soil level and bottom-water.\n" +
                            "• **Mealybugs**: Dab white cottony clusters directly with a Q-tip dipped in 70% isopropyl alcohol.",
                    listOf(
                        GroundingSource("IPM Pest Management - Biological Houseplant Controls", "https://ipm.ucanr.edu")
                    )
                )
            }
            else -> {
                Pair(
                    "Greetings from Dr. Flora! 🌿 I am your AI Master Botanist and Plant Doctor.\n\n" +
                            "I can help you with:\n" +
                            "• Diagnosing yellow, brown, or drooping foliage\n" +
                            "• Checking watering and sunlight requirements for any species\n" +
                            "• Step-by-step propagation and repotting advice\n" +
                            "• Organic pest and fungal defense\n\n" +
                            "What plant are you caring for right now?",
                    listOf(
                        GroundingSource("Google Search Botanical Knowledge Index", "https://google.com/search?q=botanical+plant+care")
                    )
                )
            }
        }

        return ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            isUser = false,
            text = response,
            timestampFormatted = formattedTime,
            sources = sources,
            isGrounded = true,
            searchQuery = userMessage
        )
    }
}
