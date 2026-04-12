package me.pecos.memozy.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import me.pecos.memozy.data.datasource.local.entity.YoutubeSummary

@Dao
interface YoutubeSummaryDao {
    @Query("SELECT * FROM youtube_summary WHERE videoId = :videoId AND mode = :mode AND language = :language")
    suspend fun getByKey(videoId: String, mode: String, language: String): YoutubeSummary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: YoutubeSummary)

    @Query("SELECT * FROM youtube_summary ORDER BY createdAt DESC")
    suspend fun getAllOnce(): List<YoutubeSummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(summaries: List<YoutubeSummary>)

    @Query("DELETE FROM youtube_summary")
    suspend fun clearAll()
}
