package me.pecos.memozy.data.repository.chat

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.chat.entity.ChatMessage
import me.pecos.memozy.data.datasource.local.chat.entity.ChatSession

interface ChatRepository {

    fun getSessions(): Flow<List<ChatSession>>

    suspend fun getSessionById(id: Int): ChatSession?

    suspend fun createSession(title: String, category: String = "general"): Long

    suspend fun updateSession(session: ChatSession)

    suspend fun deleteSession(id: Int)

    fun getMessages(sessionId: Int): Flow<List<ChatMessage>>

    suspend fun sendMessage(sessionId: Int, role: String, content: String, metadata: String? = null): Long

    suspend fun deleteMessage(id: Int)

    suspend fun clearAllSessions()
}
