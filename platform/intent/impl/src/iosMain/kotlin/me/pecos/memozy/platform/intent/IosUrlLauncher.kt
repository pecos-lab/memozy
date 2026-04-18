package me.pecos.memozy.platform.intent

// TODO(C-1): UIApplication.sharedApplication.openURL / canOpenURL 기반 구현.
// 현재 스파이크 단계: 호출은 실패(false)로 처리하고 로그만 남긴다.
class IosUrlLauncher : UrlLauncher {
    override fun open(url: String): Boolean {
        println("[platform-intent] IosUrlLauncher.open stub: $url")
        return false
    }

    override fun openPreferringPackage(url: String, preferredPackage: String): Boolean {
        println("[platform-intent] IosUrlLauncher.openPreferringPackage stub: $url ($preferredPackage)")
        return false
    }
}
