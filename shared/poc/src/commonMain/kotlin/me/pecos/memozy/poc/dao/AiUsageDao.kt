package me.pecos.memozy.poc.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface AiUsageDao {
    @Query("SELECT COUNT(*) FROM ai_usage")
    suspend fun count(): Int
}
