package me.pecos.memozy.feature.home.api

import androidx.navigation.NavGraphBuilder

/**
 * 홈 화면으로 진입하기 위한 네비게이션 정의
 */
object HomeRoute {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val TRASH = "trash"
    const val QUIZ = "quiz/{memoId}"
    fun quizRoute(memoId: Int) = "quiz/$memoId"
}

interface HomeNavigation {
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onNavigateToMemo: (String) -> Unit,
        onNavigateToDonation: () -> Unit,
        onBack: () -> Unit
    )
}
