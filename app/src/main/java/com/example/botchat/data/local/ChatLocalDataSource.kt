package com.example.botchat.data.local

import com.example.botchat.data.model.Conversation
import com.example.botchat.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatLocalDataSource {
    fun getConversationsWithUnsentMessages(): Flow<List<Conversation>>

    fun getMessages(conversationId: String): Flow<List<Message>>

    fun getUnsentMessagesForConversation(conversationId: String): Flow<List<Message>>

    fun getAllDistinctConversationIds(): Flow<List<String>>

    suspend fun saveMessage(message: Message)

    suspend fun getUnsentMessages(): List<Message>

    suspend fun deleteSentMessages()

    suspend fun updateMessageStatus(
        messageId: String,
        isSent: Boolean,
    )
}
