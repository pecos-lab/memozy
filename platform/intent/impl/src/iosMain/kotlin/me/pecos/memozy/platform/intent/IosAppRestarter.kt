package me.pecos.memozy.platform.intent

import platform.Foundation.NSUserDefaults

/**
 * iOS no-op 정책 (공식).
 *
 * WHY: iOS 는 앱 프로세스 스스로 자신을 재시작할 공식 수단이 없다.
 * UIApplication 수준에서 Activity.recreate 같은 훅이 제공되지 않으며,
 * exit(0) 같은 강제 종료는 App Store 리젝 사유가 된다.
 *
 * [restart] 자체는 no-op. 다만 [applyAppLanguage] 는 NSUserDefaults 의
 * "AppleLanguages" 키를 갱신해 **다음 cold start 부터** 새 언어가 시스템 locale 로 적용되도록 한다.
 * 호출 측은 [isRestartSupported] 가 false 이므로 `ios_restart_required` 안내를 노출한다.
 */
class IosAppRestarter : AppRestarter {
    override val isRestartSupported: Boolean = false

    override fun restart() {
        // no-op (App Store 정책상 exit() 금지)
    }

    override fun applyAppLanguage(code: String) {
        // iOS 표준: AppleLanguages 키를 갱신하면 다음 cold start 시 시스템이 해당 언어 locale 로 앱을 띄움.
        // CMP 의 Locale.current 가 NSLocale.preferredLanguages[0] 를 읽으므로 cold restart 후엔
        // stringResource 들이 자동으로 새 언어로 해석된다.
        // (현재 프로세스에서는 Locale 캐시가 갱신 안 돼 즉시 반영은 불가 — 사용자가 직접 종료 후 재실행 필요.)
        NSUserDefaults.standardUserDefaults.setObject(
            value = listOf(code),
            forKey = "AppleLanguages"
        )
        NSUserDefaults.standardUserDefaults.synchronize()
    }
}
