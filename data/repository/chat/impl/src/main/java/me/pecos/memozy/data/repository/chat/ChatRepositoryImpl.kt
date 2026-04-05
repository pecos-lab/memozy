package me.pecos.memozy.data.repository.chat

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.chat.ChatMessageDao
import me.pecos.memozy.data.datasource.local.chat.ChatSessionDao
import me.pecos.memozy.data.datasource.local.chat.entity.ChatMessage
import me.pecos.memozy.data.datasource.local.chat.entity.ChatSession
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val sessionDao: ChatSessionDao,
    private val messageDao: ChatMessageDao
) : ChatRepository {

    override fun getSessions(): Flow<List<ChatSession>> = sessionDao.getAllSessions()

    override suspend fun getSessionById(id: Int): ChatSession? = sessionDao.getSessionById(id)

    override suspend fun createSession(title: String, category: String): Long {
        return sessionDao.insertSession(
            ChatSession(title = title, category = category)
        )
    }

    override suspend fun updateSession(session: ChatSession) {
        sessionDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteSession(id: Int) {
        sessionDao.deleteSessionById(id)
    }

    override fun getMessages(sessionId: Int): Flow<List<ChatMessage>> =
        messageDao.getMessagesBySession(sessionId)

    override suspend fun sendMessage(
        sessionId: Int,
        role: String,
        content: String,
        metadata: String?
    ): Long {
        return messageDao.insertMessage(
            ChatMessage(
                sessionId = sessionId,
                role = role,
                content = content,
                metadata = metadata
            )
        )
    }

    override suspend fun deleteMessage(id: Int) {
        messageDao.deleteMessageById(id)
    }

    override suspend fun clearAllSessions() {
        sessionDao.clearAllSessions()
    }
}
