package com.example.botchat.data.remote

import android.util.Log
import com.example.botchat.data.model.Message
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicReference

class ChatRemoteDataSource(
    private val httpClient: HttpClient,
) {
    private val webSocketSession: AtomicReference<io.ktor.websocket.DefaultWebSocketSession?> =
        AtomicReference(null)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<Message>()
    val incomingMessages: SharedFlow<Message> = _incomingMessages.asSharedFlow()

    suspend fun connectWebSocket(
        host: String,
        port: Int = 80,
        path: String = "/chat",
    ) {
        try {
            httpClient.webSocket(
                method = io.ktor.http.HttpMethod.Get,
                host = host,
                port = port,
                path = path,
            ) {
                webSocketSession.set(this)
                _isConnected.value = true

                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    Log.d("ChatRemoteDataSource", "Received raw: $receivedText")
                    try {
                        val message = Json.decodeFromString<Message>(receivedText)
                        _incomingMessages.emit(message)
                        Log.d(
                            "ChatRemoteDataSource",
                            "Parsed incoming message: ID=${message.id}, Text=${message.text}",
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "ChatRemoteDataSource",
                            "Error parsing incoming message: ${e.message}. JSON input: $receivedText",
                        )
                    }
                }
            }
        } catch (e: Exception) {
            _isConnected.value = false
            Log.e("ChatRemoteDataSource", "Error during WebSocket connection: ${e.message}")
            webSocketSession.set(null)
            throw e
        } finally {
            _isConnected.value = false
            webSocketSession.set(null)
            Log.d("ChatRemoteDataSource", "WebSocket connection closed.")
        }
    }

    suspend fun sendMessage(message: Message) {
        val session = webSocketSession.get()
        if (session?.isActive == true) {
            val messageJson = Json.encodeToString(message)
            Log.d("ChatRemoteDataSource", "Sending: $messageJson")
            session.send(messageJson)
        } else {
            throw IllegalStateException("WebSocket not connected or active. Cannot send message.")
        }
    }

    fun disconnectWebSocket() {
        webSocketSession.getAndSet(null)?.cancel()
        _isConnected.value = false
        Log.d("ChatRemoteDataSource", "WebSocket disconnected.")
    }
}
