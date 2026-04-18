package me.pecos.memozy.di

import me.pecos.memozy.platform.billing.BillingService
import me.pecos.memozy.platform.billing.provideBillingService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val billingPlatformModule = module {
    single<BillingService> { provideBillingService(androidContext()) }
}
