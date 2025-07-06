package com.example.botchat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.botchat.data.local.dao.ConversationDao
import com.example.botchat.data.local.dao.MessageDao
import com.example.botchat.data.local.entity.ConversationEntity
import com.example.botchat.data.local.entity.MessageEntity

@Database(
    entities = [MessageEntity::class, ConversationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
}
