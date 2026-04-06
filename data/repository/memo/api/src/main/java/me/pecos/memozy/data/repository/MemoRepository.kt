package me.pecos.memozy.data.repository

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.Memo

interface MemoRepository {
    fun getMemos(): Flow<List<Memo>>
    suspend fun addMemo(memo: Memo): Long
    suspend fun deleteMemo(id: Int)
    suspend fun updateMemo(memo: Memo)
    suspend fun clearAllMemos()
}
