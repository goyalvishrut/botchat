package com.example.botchat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.botchat.data.model.Message

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val text: String,
    val timestamp: Long,
    val isSent: Boolean
) {
    fun toDomain(): Message {
        return Message(id, conversationId, text, timestamp, isSent)
    }
}