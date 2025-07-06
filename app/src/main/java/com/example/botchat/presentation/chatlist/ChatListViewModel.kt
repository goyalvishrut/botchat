package com.example.botchat.presentation.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botchat.data.model.Message
import com.example.botchat.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private var _currentConversationId: String? = null

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    fun setConversationId(conversationId: String) {
        if (_currentConversationId != conversationId) {
            _currentConversationId = conversationId
            loadMessagesForConversation()
        }
    }

    private fun loadMessagesForConversation() {
        viewModelScope.launch {
            _currentConversationId?.let { convoId ->
                chatRepository.getMessages(convoId).collect { messages ->
                    _messages.value = messages
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val conversationId = _currentConversationId
        if (conversationId == null) {
            viewModelScope.launch {
                _errorMessage.emit("Cannot send message: No conversation selected.")
            }
            return
        }

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(conversationId, text)
            } catch (e: Exception) {
                _errorMessage.emit("Failed to send message: ${e.message}")
            }
        }
    }
}
