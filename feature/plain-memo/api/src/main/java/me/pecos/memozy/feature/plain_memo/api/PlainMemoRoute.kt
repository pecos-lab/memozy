package me.pecos.memozy.feature.plain_memo.api

object PlainMemoRoute {
    const val ROUTE = "Memo/{memoId}"
    fun navigate(memoId: Int) = "Memo/$memoId"
}
