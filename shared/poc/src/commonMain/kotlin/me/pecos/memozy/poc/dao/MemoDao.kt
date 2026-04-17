package me.pecos.memozy.poc.dao

import androidx.room.Dao
import androidx.room.Query
import me.pecos.memozy.poc.entity.Memo

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo WHERE deletedAt IS NULL ORDER BY isPinned DESC, id DESC")
    suspend fun getAllMemos(): List<Memo>

    @Query("SELECT COUNT(*) FROM memo")
    suspend fun count(): Int
}
