package me.pecos.memozy.feature.home.api

/**
 * 홈 화면으로 진입하기 위한 네비게이션 라우트 정의.
 * 플랫폼 중립(common) — iOS/Android가 각자의 Navigation에서 공유한다.
 */
object HomeRoute {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val TRASH = "trash"
}
