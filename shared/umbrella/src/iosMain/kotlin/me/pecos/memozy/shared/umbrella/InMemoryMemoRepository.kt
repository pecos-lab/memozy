package me.pecos.memozy.shared.umbrella

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.repository.MemoRepository

// iOS shell용 stub. #221 Koin 마이그레이션으로 MemoRepositoryImpl(Room KMP)이 commonMain에 완전히
// 옮겨지면 이 구현은 제거하고 실제 Repository를 바인딩한다.
internal class InMemoryMemoRepository : MemoRepository {
    override fun getMemos(): Flow<List<Memo>> = flowOf(emptyList())
    override suspend fun getMemoById(id: Int): Memo? = null
    override suspend fun addMemo(memo: Memo): Long = 0L
    override suspend fun deleteMemo(id: Int) = Unit
    override suspend fun updateMemo(memo: Memo) = Unit
    override suspend fun clearAllMemos() = Unit
    override suspend fun getRecentMemos(limit: Int): List<Memo> = emptyList()
    override suspend fun getMemosOnce(): List<Memo> = emptyList()

    override suspend fun softDeleteMemo(id: Int) = Unit
    override suspend fun softDeleteMemos(ids: List<Int>) = Unit
    override suspend fun restoreMemo(id: Int) = Unit
    override fun getDeletedMemos(): Flow<List<Memo>> = flowOf(emptyList())
    override suspend fun emptyTrash() = Unit
    override suspend fun purgeOldTrash(threshold: Long) = Unit
    override fun getTrashCount(): Flow<Int> = flowOf(0)
}
