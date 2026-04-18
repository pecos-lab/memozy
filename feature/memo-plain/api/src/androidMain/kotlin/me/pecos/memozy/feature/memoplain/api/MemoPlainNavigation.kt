package me.pecos.memozy.feature.memoplain.api

import androidx.navigation.NavGraphBuilder

/**
 * 일반 메모 화면의 NavGraph 등록 계약. androidx.navigation 의존으로 androidMain 전용.
 */
interface MemoPlainNavigation {
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onNavigateToHome: () -> Unit,
        onBack: () -> Unit
    )
}
