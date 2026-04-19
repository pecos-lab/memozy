package me.pecos.memozy.platform.intent

/**
 * iOS no-op 정책 (공식).
 *
 * WHY: iOS 는 앱 프로세스 스스로 자신을 재시작할 공식 수단이 없다.
 * UIApplication 수준에서 Activity.recreate 같은 훅이 제공되지 않으며,
 * exit(0) 같은 강제 종료는 App Store 리젝 사유가 된다.
 * 따라서 언어 변경 등 루트 UI 를 새로 구성해야 하는 상황은 "사용자가 직접
 * 앱을 종료 → 재실행" 하는 방식으로만 반영된다.
 *
 * restart() 는 호출돼도 아무 것도 하지 않고, 호출 측이 [isRestartSupported]
 * 를 체크해 안내 문구(예: ios_restart_required)를 노출해야 한다.
 */
class IosAppRestarter : AppRestarter {
    override val isRestartSupported: Boolean = false

    override fun restart() {
        // no-op (정책: 위 주석 참조)
    }
}
