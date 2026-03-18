package com.example.killsunghun

interface MemoRepository {
    fun getMemos(): List<Memo>
    fun addMemo(memo: Memo)
    fun deleteMemo(id: Int)

    // 🔥 이거 추가
    fun updateMemo(memo: Memo)
}