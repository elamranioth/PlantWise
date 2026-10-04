package com.example.data.model

data class GroundingSource(
    val title: String,
    val url: String
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestampFormatted: String,
    val sources: List<GroundingSource> = emptyList(),
    val isGrounded: Boolean = false,
    val searchQuery: String? = null
)
