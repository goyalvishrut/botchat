package com.example.botchat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.botchat.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessages(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isSent = 0 ORDER BY timestamp ASC")
    fun getUnsentMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE isSent = 0")
    suspend fun getUnsentMessages(): List<MessageEntity>

    @Query("DELETE FROM messages WHERE isSent = 1")
    suspend fun deleteSentMessages()

    @Query("UPDATE messages SET isSent = :isSent WHERE id = :messageId")
    suspend fun updateMessageStatus(
        messageId: String,
        isSent: Boolean,
    )

    @Query("SELECT DISTINCT conversationId FROM messages ORDER BY timestamp DESC")
    fun getAllDistinctConversationIds(): Flow<List<String>>

    @Query("SELECT DISTINCT conversationId FROM messages WHERE isSent = 0")
    fun getConversationsWithUnsentMessages(): Flow<List<String>>
}
