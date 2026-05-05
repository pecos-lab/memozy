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
        Purchases.logLevel = if (debug) LogLevel.DEBUG else LogLevel.ERROR
        Purchases.configure(
            PurchasesConfiguration.Builder(apiKey = apiKey).build()
        )
    }
}
