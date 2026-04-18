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

    @Query("SELECT COUNT(*) FROM ai_usage WHERE usedAt >= :startOfDay")
    suspend fun getTotalCountSince(startOfDay: Long): Int

    @Query("SELECT * FROM ai_usage ORDER BY usedAt DESC")
    suspend fun getAllOnce(): List<AiUsage>

    @Insert
    suspend fun insertAll(usages: List<AiUsage>)

    @Query("DELETE FROM ai_usage")
    suspend fun clearAll()
}
