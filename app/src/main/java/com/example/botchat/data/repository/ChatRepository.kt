package com.example.botchat.data.repository

import com.example.botchat.data.model.Conversation
import com.example.botchat.data.model.Message
import com.example.botchat.presentation.common.NetworkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    fun getAllConversations(): Flow<List<Conversation>>

    fun getMessages(conversationId: String): Flow<List<Message>>

    suspend fun sendMessage(
        conversationId: String,
        text: String,
    )

    fun getNetworkStatus(): Flow<NetworkStatus>

    suspend fun clearSentMessagesOnLaunch()

    fun getWebSocketConnectionStatus(): StateFlow<Boolean>

    suspend fun createNewConversation(): String
}
