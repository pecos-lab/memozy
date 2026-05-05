package me.pecos.memozy.di

import me.pecos.memozy.platform.billing.BillingService
import me.pecos.memozy.platform.billing.RevenueCatBillingService
import org.koin.dsl.module

val billingPlatformModule = module {
    single<BillingService> { RevenueCatBillingService(authRepository = get()) }
}
