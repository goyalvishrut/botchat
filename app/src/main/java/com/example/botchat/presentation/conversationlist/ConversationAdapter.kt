package com.example.botchat.presentation.conversationlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.botchat.data.model.Conversation
import com.example.botchat.databinding.ItemConversationBinding

class ConversationAdapter(
    private val onItemClick: (Conversation) -> Unit,
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ConversationViewHolder {
        val view =
            ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ConversationViewHolder,
        position: Int,
    ) {
        val conversation = getItem(position)
        holder.bind(conversation)
    }

    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(conversation: Conversation) {
            binding.conversationIdTextView.text = "Chat with: ${conversation.id.take(8)}..."
            binding.lastMessagePreviewTextView.text =
                conversation.latestMessagePreview ?: "No messages yet."
            binding.clParent.setOnClickListener { onItemClick.invoke(conversation) }
        }
    }
}

class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(
        oldItem: Conversation,
        newItem: Conversation,
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: Conversation,
        newItem: Conversation,
    ): Boolean = oldItem == newItem
}
