package com.example.botchat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.botchat.data.model.Conversation

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lastMessagePreview: String?,
    val unreadCount: Int
) {
    fun toDomain(): Conversation {
        return Conversation(id, lastMessagePreview, unreadCount)
    }
}