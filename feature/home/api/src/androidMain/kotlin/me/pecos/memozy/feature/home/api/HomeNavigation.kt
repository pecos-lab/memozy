package me.pecos.memozy.feature.home.api

import androidx.navigation.NavGraphBuilder

/**
 * Android NavGraph에 홈 화면 그래프를 등록하기 위한 네비게이션 계약.
 *
 * androidx.navigation-compose는 KMP 미지원(2.8.x 라인은 Android-only)이므로
 * 이 인터페이스는 androidMain에 둔다. iOS는 iosMain에서 별도 네비게이션
 * 계약을 정의하되, [HomeRoute] 상수는 commonMain을 통해 공유한다.
 */
interface HomeNavigation {
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onNavigateToMemo: (String) -> Unit,
        onNavigateToDonation: () -> Unit,
        onBack: () -> Unit
    )
}
