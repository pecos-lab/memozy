package me.pecos.memozy.poc.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ChatMessageDao {
    @Query("SELECT COUNT(*) FROM chat_message")
    suspend fun count(): Int
}
