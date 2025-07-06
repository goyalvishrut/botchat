package com.example.botchat.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.botchat.presentation.common.NetworkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidNetworkMonitor(private val context: Context) : NetworkMonitor {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _networkStatus = MutableStateFlow(NetworkStatus.OFFLINE)

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _networkStatus.value = NetworkStatus.ONLINE
                }

                override fun onLost(network: Network) {
                    _networkStatus.value = NetworkStatus.OFFLINE
                }
            })
        _networkStatus.value = if (isOnline()) NetworkStatus.ONLINE else NetworkStatus.OFFLINE
    }

    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()

    override fun isOnline(): Boolean {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
