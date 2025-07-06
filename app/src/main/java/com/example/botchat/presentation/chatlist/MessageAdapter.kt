package com.example.botchat.presentation.chatlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.botchat.R
import com.example.botchat.data.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter :
    ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.id.startsWith("client-")) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutRes = if (viewType == VIEW_TYPE_SENT) {
            R.layout.item_chat_message_sent
        } else {
            R.layout.item_chat_message_received
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val statusTextView: TextView? =
            itemView.findViewById(R.id.statusTextView)

        fun bind(message: Message) {
            messageTextView.text = message.text
            timestampTextView.text =
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

            if (getItemViewType(bindingAdapterPosition) == VIEW_TYPE_SENT && statusTextView != null) {
                statusTextView.text = if (message.isSent) "Sent" else "Sending..."
                statusTextView.visibility = View.VISIBLE
            } else {
                statusTextView?.visibility = View.GONE
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}
