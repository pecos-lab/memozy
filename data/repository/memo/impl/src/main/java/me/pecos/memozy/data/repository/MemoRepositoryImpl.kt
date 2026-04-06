package me.pecos.memozy.data.repository

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.MemoDao
import javax.inject.Inject

class MemoRepositoryImpl @Inject constructor(private val memoDao: MemoDao) : MemoRepository {

    override fun getMemos(): Flow<List<Memo>> = memoDao.getAllMemos()

    override suspend fun addMemo(memo: Memo): Long {
        return memoDao.insertMemo(memo)
    }

    override suspend fun deleteMemo(id: Int) {
        memoDao.deleteMemoById(id)
    }

    override suspend fun updateMemo(memo: Memo) {
        memoDao.updateMemo(memo.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun clearAllMemos() {
        memoDao.clearAllMemos()
    }
}
