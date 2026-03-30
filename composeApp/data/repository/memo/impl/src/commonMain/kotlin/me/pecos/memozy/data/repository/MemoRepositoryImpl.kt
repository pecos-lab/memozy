package me.pecos.memozy.data.repository

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.MemoDao

internal expect fun currentTimeMillis(): Long

class MemoRepositoryImpl(private val memoDao: MemoDao) : MemoRepository {

    override fun getMemos(): Flow<List<Memo>> = memoDao.getAllMemos()

    override suspend fun addMemo(memo: Memo) {
        memoDao.insertMemo(memo)
    }

    override suspend fun deleteMemo(id: Int) {
        memoDao.deleteMemoById(id)
    }

    override suspend fun updateMemo(memo: Memo) {
        memoDao.updateMemo(memo.copy(updatedAt = currentTimeMillis()))
    }

    override suspend fun clearAllMemos() {
        memoDao.clearAllMemos()
    }
}
