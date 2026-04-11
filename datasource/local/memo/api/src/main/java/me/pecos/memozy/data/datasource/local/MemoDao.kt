package me.pecos.memozy.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.Memo

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo WHERE deletedAt IS NULL ORDER BY isPinned DESC, id DESC")
    fun getAllMemos(): Flow<List<Memo>>

    @Insert
    suspend fun insertMemo(memo: Memo): Long

    @Update
    suspend fun updateMemo(memo: Memo)

    @Query("SELECT * FROM memo WHERE id = :id")
    suspend fun getMemoById(id: Int): Memo?

    @Query("DELETE FROM memo WHERE id = :id")
    suspend fun deleteMemoById(id: Int)

    @Query("DELETE FROM memo")
    suspend fun clearAllMemos()

    @Query("SELECT * FROM memo WHERE deletedAt IS NULL ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentMemos(limit: Int): List<Memo>

    // ── Trash (Soft Delete) ──

    @Query("UPDATE memo SET deletedAt = :deletedAt, isPinned = 0 WHERE id = :id")
    suspend fun softDeleteMemoById(id: Int, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE memo SET deletedAt = :deletedAt, isPinned = 0 WHERE id IN (:ids)")
    suspend fun softDeleteMemosByIds(ids: List<Int>, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE memo SET deletedAt = NULL WHERE id = :id")
    suspend fun restoreMemoById(id: Int)

    @Query("SELECT * FROM memo WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getDeletedMemos(): Flow<List<Memo>>

    @Query("DELETE FROM memo WHERE deletedAt IS NOT NULL")
    suspend fun emptyTrash()

    @Query("DELETE FROM memo WHERE deletedAt IS NOT NULL AND deletedAt < :threshold")
    suspend fun purgeOldTrash(threshold: Long)

    @Query("SELECT COUNT(*) FROM memo WHERE deletedAt IS NOT NULL")
    fun getTrashCount(): Flow<Int>

    // ── Backup & Restore ──

    @Query("SELECT * FROM memo WHERE deletedAt IS NULL ORDER BY id ASC")
    suspend fun getAllMemosOnce(): List<Memo>

    @Query("SELECT * FROM memo ORDER BY id ASC")
    suspend fun getAllMemosForBackup(): List<Memo>

    @Insert
    suspend fun insertMemos(memos: List<Memo>)
}
