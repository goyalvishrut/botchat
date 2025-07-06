package com.example.botchat.presentation.conversationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botchat.data.model.Conversation
import com.example.botchat.data.repository.ChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationListViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {
    val conversations: StateFlow<List<Conversation>> =
        chatRepository.getAllConversations().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun createNewConversation(onConversationCreated: (String) -> Unit) {
        viewModelScope.launch {
            val newConversationId = chatRepository.createNewConversation()
            onConversationCreated(newConversationId)
        }
    }
}
