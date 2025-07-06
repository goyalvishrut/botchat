package com.example.botchat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val text: String,
    val timestamp: Long,
    val isSent: Boolean
)
