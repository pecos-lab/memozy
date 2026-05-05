package me.pecos.memozy.platform.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

/**
 * RevenueCat SDK 부트스트랩.
 *
 * Android: `MemozyApplication.onCreate()` 에서 Koin start 직전에 호출.
 * iOS:     `initKoin()` 첫 줄에서 호출.
 *
 * `appUserId` 는 Supabase auth 가 준비되기 전에 호출되므로 보통 null.
 * 로그인/로그아웃 시점에 [RevenueCatBillingService.setAppUserId] 로 동기화.
 */
object RevenueCatInitializer {

    fun configure(apiKey: String, debug: Boolean) {
        if (Purchases.isConfigured) return
        if (apiKey.isEmpty()) return

        // RC SDK 는 release 빌드에서 `test_` prefix 키를 감지하면 안전 차원에서
        // 강제로 앱을 종료시킨다 ("Wrong API Key" 다이얼로그 후 finish).
        // RC 대시보드 셋업 + production `goog_*` 키 발급 전까진 release 빌드에서
        // 초기화를 스킵해 앱을 살린다. 결제/구독 화면만 비활성, 그 외 기능은 정상.
        if (!debug && apiKey.startsWith("test_")) {
            println(
                "[RevenueCat] Skipping init in release build with test API key. " +
                    "Provision production key (goog_*) in local.properties before release."
            )
            return
        }

        Purchases.logLevel = if (debug) LogLevel.DEBUG else LogLevel.ERROR
        Purchases.configure(
            PurchasesConfiguration.Builder(apiKey = apiKey).build()
        )
    }
}
