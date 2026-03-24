package me.pecos.nota

import kotlinx.coroutines.flow.Flow

class MemoRepositoryImpl(private val memoDao: MemoDao) : MemoRepository {

    override fun getMemos(): Flow<List<Memo>> = memoDao.getAllMemos()

    override suspend fun addMemo(memo: Memo) {
        memoDao.insertMemo(memo)
    }

    override suspend fun deleteMemo(id: Int) {
        memoDao.deleteMemoById(id)
    }

    override suspend fun updateMemo(memo: Memo) {
        memoDao.updateMemo(memo)
    }

    override suspend fun clearAllMemos() {
        memoDao.clearAllMemos()
    }

    override suspend fun migratePersonalToGeneral() {
        memoDao.migratePersonalToGeneral()
    }
}
