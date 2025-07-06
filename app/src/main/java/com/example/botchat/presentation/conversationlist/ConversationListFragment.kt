package com.example.botchat.presentation.conversationlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.botchat.databinding.FragmentConversationListBinding
import com.example.botchat.presentation.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConversationListFragment : Fragment() {
    private var _binding: FragmentConversationListBinding? = null
    private val binding: FragmentConversationListBinding get() = checkNotNull(_binding)

    private val conversationListViewModel: ConversationListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentConversationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val conversationAdapter =
            ConversationAdapter { conversation ->
                (activity as? MainActivity)?.navigateToChat(conversation.id)
            }

        binding.conversationsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.conversationsRecyclerView.adapter = conversationAdapter

        binding.startNewConversationButton.setOnClickListener {
            conversationListViewModel.createNewConversation { newConversationId ->
                (activity as? MainActivity)?.navigateToChat(newConversationId)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                conversationListViewModel.conversations.collectLatest { conversations ->
                    conversationAdapter.submitList(conversations)
                    binding.emptyStateTextView.visibility =
                        if (conversations.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): ConversationListFragment = ConversationListFragment()
    }
}
