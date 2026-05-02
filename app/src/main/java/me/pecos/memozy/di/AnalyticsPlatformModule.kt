package me.pecos.memozy.di

import me.pecos.memozy.platform.analytics.AnalyticsService
import me.pecos.memozy.platform.analytics.provideAnalyticsService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val analyticsPlatformModule = module {
    single<AnalyticsService> { provideAnalyticsService(androidContext()) }
}
