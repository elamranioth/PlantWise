package com.example.data.model

data class ForumComment(
    val id: String,
    val authorName: String,
    val authorBadge: String = "🌱 Sprout",
    val timestampFormatted: String,
    val text: String
)

data class ForumPost(
    val id: String,
    val authorName: String,
    val authorBadge: String,
    val category: String, // "🌱 Showcase", "🆘 Care Q&A", "✂️ Propagation", "🌿 Plant Rescue", "💡 Quick Tip"
    val timestampFormatted: String,
    val title: String,
    val content: String,
    val plantSpecies: String? = null,
    val likesCount: Int,
    val isLikedByMe: Boolean = false,
    val comments: List<ForumComment> = emptyList(),
    val isEdited: Boolean = false
)

data class MasterGuide(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val readTimeMinutes: Int,
    val iconEmoji: String,
    val summary: String,
    val steps: List<GrowthStep>
)
