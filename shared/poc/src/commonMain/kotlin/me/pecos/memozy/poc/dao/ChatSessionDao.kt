package me.pecos.memozy.poc.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ChatSessionDao {
    @Query("SELECT COUNT(*) FROM chat_session")
    suspend fun count(): Int
}
