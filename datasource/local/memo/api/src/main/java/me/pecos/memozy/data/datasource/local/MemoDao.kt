package me.pecos.memozy.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.Memo

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo ORDER BY id DESC")
    fun getAllMemos(): Flow<List<Memo>>

    @Insert
    suspend fun insertMemo(memo: Memo): Long

    @Update
    suspend fun updateMemo(memo: Memo)

    @Query("DELETE FROM memo WHERE id = :id")
    suspend fun deleteMemoById(id: Int)

    @Query("DELETE FROM memo")
    suspend fun clearAllMemos()
}
