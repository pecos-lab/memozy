package me.pecos.memozy.platform.intent

interface UrlLauncher {
    fun open(url: String): Boolean

    fun openPreferringPackage(url: String, preferredPackage: String): Boolean

    /**
     * 플랫폼별 구독 관리 화면 열기.
     * Android → Google Play 구독 페이지
     * iOS → Apple ID 구독 관리 (앱이 설치된 채로 자동 진입)
     */
    fun openManageSubscriptions(): Boolean
}
