package com.example.botchat.presentation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.botchat.R
import com.example.botchat.databinding.ActivityMainBinding
import com.example.botchat.presentation.chatlist.ChatListFragment
import com.example.botchat.presentation.common.NetworkStatus
import com.example.botchat.presentation.conversationlist.ConversationListFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = checkNotNull(_binding)

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, ConversationListFragment.newInstance())
                .commit()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.networkStatus.collectLatest { status ->
                    when (status) {
                        NetworkStatus.ONLINE -> {
                            binding.networkStatusTextView.visibility = View.GONE
                        }

                        NetworkStatus.OFFLINE -> {
                            binding.networkStatusTextView.visibility = View.VISIBLE
                            binding.networkStatusTextView.text =
                                "Offline. Messages will be sent when online."
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.isWebSocketConnected.collectLatest { isConnected ->
                    if (isConnected) {
                        binding.websocketStatusTextView.visibility = View.GONE
                    } else {
                        binding.websocketStatusTextView.visibility = View.VISIBLE
                        binding.websocketStatusTextView.text = "Connecting to chat server..."
                    }
                }
            }
        }
    }

    fun navigateToChat(conversationId: String) {
        val chatListFragment = ChatListFragment.newInstance(conversationId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, chatListFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.clearSentMessages()
        _binding = null
    }
}
