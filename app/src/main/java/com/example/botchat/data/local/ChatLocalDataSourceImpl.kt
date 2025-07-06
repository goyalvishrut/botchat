package com.example.botchat.data.local

import com.example.botchat.data.local.dao.ConversationDao
import com.example.botchat.data.local.dao.MessageDao
import com.example.botchat.data.local.entity.MessageEntity
import com.example.botchat.data.model.Conversation
import com.example.botchat.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatLocalDataSourceImpl(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
) : ChatLocalDataSource {
    override fun getConversationsWithUnsentMessages(): Flow<List<Conversation>> =
        messageDao.getConversationsWithUnsentMessages().map { conversationIds ->
            conversationIds.map { id ->
                Conversation(id = id, latestMessagePreview = "Unsent messages...", unreadCount = 0)
            }
        }

    override fun getAllDistinctConversationIds(): Flow<List<String>> = messageDao.getAllDistinctConversationIds()

    override fun getMessages(conversationId: String): Flow<List<Message>> =
        messageDao.getMessages(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getUnsentMessagesForConversation(conversationId: String): Flow<List<Message>> =
        messageDao.getUnsentMessagesForConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun saveMessage(message: Message) {
        messageDao.saveMessage(
            MessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                text = message.text,
                timestamp = message.timestamp,
                isSent = message.isSent,
            ),
        )
    }

    override suspend fun getUnsentMessages(): List<Message> = messageDao.getUnsentMessages().map { it.toDomain() }

    override suspend fun deleteSentMessages() {
        messageDao.deleteSentMessages()
    }

    override suspend fun updateMessageStatus(
        messageId: String,
        isSent: Boolean,
    ) {
        messageDao.updateMessageStatus(messageId, isSent)
    }
}
