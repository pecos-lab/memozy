package me.pecos.memozy.feature.memo_plain.api

import androidx.navigation.NavGraphBuilder

/**
 * 일반 메모 화면으로 진입하기 위한 네비게이션 정의
 */
object MemoPlainRoute {
    const val MEMO = "Memo/{memoId}"
    
    fun createRoute(memoId: String) = "Memo/$memoId"
}

interface MemoPlainNavigation {
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onSave: () -> Unit,
        onBack: () -> Unit
    )
}
