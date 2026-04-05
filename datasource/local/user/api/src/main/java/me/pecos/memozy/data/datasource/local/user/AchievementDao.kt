package me.pecos.memozy.data.datasource.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.user.entity.Achievement

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievement ORDER BY unlockedAt DESC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievement WHERE id = :id")
    suspend fun getAchievementById(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAchievement(achievement: Achievement)

    @Query("DELETE FROM achievement WHERE id = :id")
    suspend fun deleteAchievementById(id: String)

    @Query("DELETE FROM achievement")
    suspend fun clearAllAchievements()
}
