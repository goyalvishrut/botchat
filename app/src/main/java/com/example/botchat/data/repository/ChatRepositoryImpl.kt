package com.example.botchat.data.repository

import android.util.Log
import com.example.botchat.data.local.ChatLocalDataSource
import com.example.botchat.data.model.Conversation
import com.example.botchat.data.model.Message
import com.example.botchat.data.remote.ChatRemoteDataSource
import com.example.botchat.presentation.common.NetworkStatus
import com.example.botchat.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepositoryImpl(
    private val localDataSource: ChatLocalDataSource,
    private val remoteDataSource: ChatRemoteDataSource,
    private val networkMonitor: NetworkMonitor,
    private val applicationScope: CoroutineScope,
) : ChatRepository {
    private var webSocketConnectionJob: Job? = null

    init {
        observeNetworkAndWebSocketStatus()
        observeIncomingWebSocketMessages()
    }

    private fun observeNetworkAndWebSocketStatus() {
        applicationScope.launch {
            combine(
                networkMonitor.observeNetworkStatus().distinctUntilChanged(),
                remoteDataSource.isConnected,
            ) { networkStatus, isWebSocketConnected ->
                Pair(networkStatus, isWebSocketConnected)
            }.collect { (networkStatus, isWebSocketConnected) ->
                Log.d(
                    "ChatRepositoryImpl",
                    "Combined status: Network=$networkStatus, WebSocket=$isWebSocketConnected",
                )

                when (networkStatus) {
                    NetworkStatus.ONLINE -> {
                        if (!isWebSocketConnected && (webSocketConnectionJob == null || webSocketConnectionJob?.isActive == false)) {
                            Log.d(
                                "ChatRepositoryImpl",
                                "Network online but WebSocket disconnected. Attempting to reconnect.",
                            )
                            startWebSocketConnection()
                        }
                        retryFailedMessages()
                    }

                    NetworkStatus.OFFLINE -> {
                        Log.d(
                            "ChatRepositoryImpl",
                            "Network offline. Disconnecting WebSocket if active.",
                        )
                        remoteDataSource.disconnectWebSocket()
                        webSocketConnectionJob?.cancel()
                        webSocketConnectionJob = null
                    }
                }
            }
        }
    }

    private fun startWebSocketConnection() {
        webSocketConnectionJob?.cancel()
        webSocketConnectionJob =
            applicationScope.launch(Dispatchers.IO) {
                var attempt = 0
                var delayTime = WEBSOCKET_RETRY_DELAY_MS
                while (networkMonitor.isOnline() && !remoteDataSource.isConnected.value) {
                    attempt++
                    Log.d(
                        "ChatRepositoryImpl",
                        "Attempting WebSocket connection (Attempt $attempt)...",
                    )
                    try {
                        remoteDataSource.connectWebSocket(
                            host = SOCKET_HOST,
                            path = SOCKET_PATH,
                        )
                        break
                    } catch (e: Exception) {
                        Log.e(
                            "ChatRepositoryImpl",
                            "Failed to establish WebSocket connection on attempt $attempt: ${e.message}",
                        )
                        if (networkMonitor.isOnline()) {
                            Log.d(
                                "ChatRepositoryImpl",
                                "Retrying in ${delayTime / 1000} seconds...",
                            )
                            delay(delayTime)
                            delayTime =
                                (delayTime * 1.5)
                                    .toLong()
                                    .coerceAtMost(60000L)
                        } else {
                            Log.d(
                                "ChatRepositoryImpl",
                                "Network went offline during retry, stopping attempts.",
                            )
                            break
                        }
                    }
                }
            }
    }

    private fun observeIncomingWebSocketMessages() {
        applicationScope.launch {
            remoteDataSource.incomingMessages.collect { incomingMessage ->
                Log.d("ChatRepositoryImpl", "Received incoming message: ${incomingMessage.text}")

                localDataSource.saveMessage(
                    incomingMessage.copy(
                        id = "server-${UUID.randomUUID()}",
                        isSent = true,
                        timestamp = System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    override fun getAllConversations(): Flow<List<Conversation>> =
        localDataSource.getAllDistinctConversationIds().map { conversationIds ->
            conversationIds.map { id ->
                Conversation(
                    id = id,
                    latestMessagePreview = "Last message in $id...",
                    unreadCount = 0,
                )
            }
        }

    override fun getMessages(conversationId: String): Flow<List<Message>> = localDataSource.getMessages(conversationId)

    override suspend fun sendMessage(
        conversationId: String,
        text: String,
    ) {
        val message =
            Message(
                id = "client-${UUID.randomUUID()}",
                conversationId = conversationId,
                text = text,
                timestamp = System.currentTimeMillis(),
                isSent = false,
            )
        localDataSource.saveMessage(message)

        if (networkMonitor.isOnline() && remoteDataSource.isConnected.value) {
            try {
                remoteDataSource.sendMessage(message)
                localDataSource.updateMessageStatus(message.id, true)
                Log.d("ChatRepository", "Message sent and status updated to true: ${message.id}")
            } catch (e: Exception) {
                Log.e("ChatRepository", "Failed to send message immediately: ${e.message}")
                // Message remains unsent (isSent=false) in local DB for retry
            }
        } else {
            Log.d(
                "ChatRepository",
                "Offline or WebSocket disconnected: Message queued for sending. ID: ${message.id}",
            )
        }
    }

    private suspend fun retryFailedMessages() {
        val failedMessages = localDataSource.getUnsentMessages()
        if (failedMessages.isEmpty()) {
            return
        }

        if (networkMonitor.isOnline() && remoteDataSource.isConnected.value) {
            failedMessages.forEach { message ->
                try {
                    remoteDataSource.sendMessage(message)
                    localDataSource.updateMessageStatus(message.id, true)
                    Log.d(
                        "ChatRepository",
                        "Message retried and status updated to true: ${message.id}",
                    )
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Failed to retry message: ${message.id}, ${e.message}")
                    // Message remains unsent if retry fails again
                }
            }
        } else {
            Log.d(
                "ChatRepository",
                "Cannot retry messages: Network offline or WebSocket disconnected.",
            )
        }
    }

    override fun getNetworkStatus(): Flow<NetworkStatus> = networkMonitor.observeNetworkStatus()

    override suspend fun clearSentMessagesOnLaunch() {
        localDataSource.deleteSentMessages()
    }

    override fun getWebSocketConnectionStatus(): StateFlow<Boolean> = remoteDataSource.isConnected

    override suspend fun createNewConversation(): String {
        val newConversationId = UUID.randomUUID().toString()
        return newConversationId
    }

    companion object {
        private const val SOCKET_HOST = "192.168.29.221"
        private const val SOCKET_PATH = "/chat"
        private const val WEBSOCKET_RETRY_DELAY_MS = 5000L
    }
}
