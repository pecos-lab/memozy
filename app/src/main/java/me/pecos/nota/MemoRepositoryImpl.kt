package me.pecos.nota

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MemoRepositoryImpl @Inject constructor(private val memoDao: MemoDao) : MemoRepository {

    override fun getMemos(): Flow<List<Memo>> = memoDao.getAllMemos()

    override suspend fun addMemo(memo: Memo) {
        memoDao.insertMemo(memo)
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
