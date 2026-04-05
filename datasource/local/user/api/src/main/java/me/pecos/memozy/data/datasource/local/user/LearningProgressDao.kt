package me.pecos.memozy.data.datasource.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.user.entity.LearningProgress

@Dao
interface LearningProgressDao {

    @Query("SELECT * FROM learning_progress ORDER BY guideId ASC")
    fun getAllProgress(): Flow<List<LearningProgress>>

    @Query("SELECT * FROM learning_progress WHERE guideId = :guideId")
    suspend fun getProgressById(guideId: String): LearningProgress?

    @Query("SELECT * FROM learning_progress WHERE completed = 1")
    fun getCompletedProgress(): Flow<List<LearningProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LearningProgress)

    @Query("DELETE FROM learning_progress WHERE guideId = :guideId")
    suspend fun deleteProgressById(guideId: String)

    @Query("DELETE FROM learning_progress")
    suspend fun clearAllProgress()
}
