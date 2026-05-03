package me.pecos.memozy.platform.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitOfferings

object RevenueCatPoc {

    fun configure(apiKey: String, logLevel: LogLevel = LogLevel.ERROR) {
        Purchases.logLevel = logLevel
        Purchases.configure(PurchasesConfiguration.Builder(apiKey = apiKey).build())
    }

    fun isConfigured(): Boolean = Purchases.isConfigured

    suspend fun fetchCurrentOfferingId(): Result<String?> =
        runCatching { Purchases.sharedInstance.awaitOfferings().current?.identifier }
}
