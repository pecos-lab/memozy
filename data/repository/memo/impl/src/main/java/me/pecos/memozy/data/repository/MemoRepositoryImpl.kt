package me.pecos.memozy.data.repository

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.MemoDao
import javax.inject.Inject

class MemoRepositoryImpl @Inject constructor(private val memoDao: MemoDao) : MemoRepository {

    override fun getMemos(): Flow<List<Memo>> = memoDao.getAllMemos()

    override suspend fun getMemoById(id: Int): Memo? = memoDao.getMemoById(id)

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

    override suspend fun getRecentMemos(limit: Int): List<Memo> {
        return memoDao.getRecentMemos(limit)
    }

    override suspend fun getMemosOnce(): List<Memo> = memoDao.getAllMemosOnce()

    // ── Trash ──

    override suspend fun softDeleteMemo(id: Int) {
        memoDao.softDeleteMemoById(id)
    }

    override suspend fun softDeleteMemos(ids: List<Int>) {
        memoDao.softDeleteMemosByIds(ids)
    }

    override suspend fun restoreMemo(id: Int) {
        memoDao.restoreMemoById(id)
    }

    override fun getDeletedMemos(): Flow<List<Memo>> = memoDao.getDeletedMemos()

    override suspend fun emptyTrash() {
        memoDao.emptyTrash()
    }

    override suspend fun purgeOldTrash(threshold: Long) {
        memoDao.purgeOldTrash(threshold)
    }

    override fun getTrashCount(): Flow<Int> = memoDao.getTrashCount()
}
