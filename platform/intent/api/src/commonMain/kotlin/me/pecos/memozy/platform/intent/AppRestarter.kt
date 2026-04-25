package me.pecos.memozy.platform.intent

/**
 * 언어 전환 후처럼 루트 UI 를 새로 만들어야 할 때 사용.
 * Android 는 Activity.recreate, iOS 는 프로세스 재시작 수단이 없어 no-op.
 */
interface AppRestarter {
    /**
     * 이 플랫폼에서 [restart] 가 실제로 UI 를 재구성하는지 여부.
     * false 인 플랫폼에서는 호출 측이 사용자에게 "앱 재시작 후 반영됩니다"
     * 안내를 노출해야 한다.
     */
    val isRestartSupported: Boolean get() = true

    fun restart()

    /**
     * 앱 언어 즉시 반영. Android 는 AppCompatDelegate 로 per-app locale 적용 후 Activity 재구성,
     * iOS 는 no-op (호출 측이 ios_restart_required 안내 노출).
     */
    fun applyAppLanguage(code: String) { restart() }
}
