package com.example.botchat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.botchat.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversation(conversationId: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations")
    fun getAllConversations(): Flow<List<ConversationEntity>>
}
