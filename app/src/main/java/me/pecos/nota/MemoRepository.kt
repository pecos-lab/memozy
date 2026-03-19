package me.pecos.nota

import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    fun getMemos(): Flow<List<Memo>>
    suspend fun addMemo(memo: Memo)
    suspend fun deleteMemo(id: Int)
    suspend fun updateMemo(memo: Memo)
    suspend fun clearAllMemos()
}
