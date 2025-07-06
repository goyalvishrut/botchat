package com.example.botchat.data.model

data class Conversation(
    val id: String,
    val latestMessagePreview: String?,
    val unreadCount: Int = 0
)
