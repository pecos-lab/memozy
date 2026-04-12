package me.pecos.memozy.data.datasource.local.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.chat.entity.ChatSession

@Dao
interface ChatSessionDao {

    @Query("SELECT * FROM chat_session ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_session ORDER BY updatedAt DESC")
    suspend fun getAllSessionsOnce(): List<ChatSession>

    @Query("SELECT * FROM chat_session WHERE id = :id")
    suspend fun getSessionById(id: Int): ChatSession?

    @Insert
    suspend fun insertSession(session: ChatSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ChatSession>)

    @Update
    suspend fun updateSession(session: ChatSession)

    @Query("DELETE FROM chat_session WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("DELETE FROM chat_session")
    suspend fun clearAllSessions()
}
