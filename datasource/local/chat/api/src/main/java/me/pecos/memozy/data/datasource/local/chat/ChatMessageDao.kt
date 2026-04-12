package me.pecos.memozy.data.datasource.local.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.chat.entity.ChatMessage

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_message WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: Int): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_message ORDER BY timestamp ASC")
    suspend fun getAllMessagesOnce(): List<ChatMessage>

    @Insert
    suspend fun insertMessage(message: ChatMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)

    @Query("DELETE FROM chat_message WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    @Query("DELETE FROM chat_message WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: Int)

    @Query("DELETE FROM chat_message")
    suspend fun clearAllMessages()
}
