package me.pecos.nota.data.reposotory

import kotlinx.coroutines.flow.Flow
import me.pecos.nota.data.datasource.local.entity.Memo

interface MemoRepository {
    fun getMemos(): Flow<List<Memo>>
    suspend fun addMemo(memo: Memo)
    suspend fun deleteMemo(id: Int)
    suspend fun updateMemo(memo: Memo)
    suspend fun clearAllMemos()
}
