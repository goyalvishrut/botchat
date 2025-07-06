package com.example.botchat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botchat.data.repository.ChatRepository
import com.example.botchat.presentation.common.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val _networkStatus = MutableStateFlow(NetworkStatus.ONLINE)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val _isWebSocketConnected = MutableStateFlow(false)
    val isWebSocketConnected: StateFlow<Boolean> = _isWebSocketConnected

    init {
        viewModelScope.launch {
            chatRepository.clearSentMessagesOnLaunch()
        }

        viewModelScope.launch {
            chatRepository.getNetworkStatus().collect { status ->
                _networkStatus.value = status
            }
        }

        viewModelScope.launch {
            chatRepository.getWebSocketConnectionStatus().collect { isConnected ->
                _isWebSocketConnected.value = isConnected
            }
        }
    }
}
