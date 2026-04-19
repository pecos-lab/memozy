package me.pecos.memozy.platform.intent

/**
 * 언어 전환 후처럼 루트 UI 를 새로 만들어야 할 때 사용.
 * Android 는 Activity.recreate, iOS 는 SceneDelegate 기반 재로딩(후속 구현).
 */
interface AppRestarter {
    fun restart()
}
