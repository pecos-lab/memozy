package me.pecos.nota

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo ORDER BY id DESC")
    fun getAllMemos(): Flow<List<Memo>>

    @Insert
    suspend fun insertMemo(memo: Memo)

    @Update
    suspend fun updateMemo(memo: Memo)

    @Query("DELETE FROM memo WHERE id = :id")
    suspend fun deleteMemoById(id: Int)
}
