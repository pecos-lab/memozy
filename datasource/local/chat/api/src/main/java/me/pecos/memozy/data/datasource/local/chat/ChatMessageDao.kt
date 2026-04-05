package me.pecos.memozy.data.datasource.local.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.chat.entity.ChatMessage

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_message WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: Int): Flow<List<ChatMessage>>

    @Insert
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_message WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    @Query("DELETE FROM chat_message WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: Int)
}
