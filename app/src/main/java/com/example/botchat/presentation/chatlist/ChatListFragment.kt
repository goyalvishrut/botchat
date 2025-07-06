package com.example.botchat.presentation.chatlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.botchat.databinding.FragmentChatListBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatListFragment : Fragment() {
    private var _binding: FragmentChatListBinding? = null
    private val binding: FragmentChatListBinding get() = checkNotNull(_binding)

    private val chatListViewModel: ChatListViewModel by viewModel()

    private var currentConversationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentConversationId = arguments?.getString(ARG_CONVERSATION_ID)
        currentConversationId?.let { chatListViewModel.setConversationId(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val messageAdapter = MessageAdapter()
        binding.messagesRecyclerView.layoutManager =
            LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        binding.messagesRecyclerView.adapter = messageAdapter

        binding.sendButton.setOnClickListener {
            val messageText =
                binding.messageEditText.text
                    .toString()
                    .trim()
            if (messageText.isNotEmpty()) {
                chatListViewModel.sendMessage(messageText)
                binding.messageEditText.text.clear()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatListViewModel.messages.collectLatest { messages ->
                    messageAdapter.submitList(messages) {
                        if (messages.isNotEmpty()) {
                            binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                        }
                        binding.emptyStateTextView.visibility =
                            if (messages.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatListViewModel.errorMessage.collectLatest { message ->
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONVERSATION_ID = "conversation_id"

        fun newInstance(conversationId: String): ChatListFragment =
            ChatListFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(ARG_CONVERSATION_ID, conversationId)
                    }
            }
    }
}
