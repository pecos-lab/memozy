package me.pecos.memozy.poc.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface YoutubeSummaryDao {
    @Query("SELECT COUNT(*) FROM youtube_summary")
    suspend fun count(): Int
}
