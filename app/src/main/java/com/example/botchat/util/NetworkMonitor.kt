package com.example.botchat.util

import com.example.botchat.presentation.common.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    fun observeNetworkStatus(): Flow<NetworkStatus>
    fun isOnline(): Boolean
}