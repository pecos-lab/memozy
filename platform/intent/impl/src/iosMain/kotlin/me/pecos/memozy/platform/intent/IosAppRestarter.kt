package me.pecos.memozy.platform.intent

/**
 * iOS 에서는 현재 ComposeUIViewController 를 교체하는 훅이 없어 동작 안 함.
 * 언어 변경은 앱 재시작까지 반영되지 않음 (후속 과제).
 */
class IosAppRestarter : AppRestarter {
    override fun restart() {
        // no-op
    }
}
