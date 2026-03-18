package com.example.killsunghun

class MemoRepositoryImpl : MemoRepository {

    private val memoList = mutableListOf<Memo>()

    override fun getMemos(): List<Memo> {
        return memoList
    }

    override fun addMemo(memo: Memo) {
        memoList.add(memo)
    }

    override fun deleteMemo(id: Int) {
        memoList.removeIf { it.id == id }
    }

    override fun updateMemo(memo: Memo) {
        val index = memoList.indexOfFirst { it.id == memo.id }
        if (index != -1) {
            memoList[index] = memo
        }
    }
}