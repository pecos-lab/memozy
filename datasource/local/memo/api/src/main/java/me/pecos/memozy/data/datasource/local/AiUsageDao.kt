package me.pecos.memozy.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import me.pecos.memozy.data.datasource.local.entity.AiUsage

@Dao
interface AiUsageDao {

    @Insert
    suspend fun insert(usage: AiUsage)

    @Query("SELECT COUNT(*) FROM ai_usage WHERE feature = :feature AND usedAt >= :startOfDay")
    suspend fun getCountSince(feature: String, startOfDay: Long): Int
}
