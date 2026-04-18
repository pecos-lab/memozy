package me.pecos.memozy.feature.memoplain.api

/**
 * 일반 메모 화면으로 진입하기 위한 네비게이션 경로 정의
 */
object MemoPlainRoute {
    const val MEMO = "Memo/{memoId}"

    fun createRoute(memoId: String) = "Memo/$memoId"
}
