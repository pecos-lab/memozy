package me.pecos.memozy.di

import me.pecos.memozy.BuildConfig
import me.pecos.memozy.platform.ads.AdsService
import me.pecos.memozy.platform.ads.provideAdsService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val adsPlatformModule = module {
    single<AdsService> {
        val testDeviceIds = BuildConfig.ADMOB_TEST_DEVICE_IDS
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        provideAdsService(
            context = androidContext(),
            adUnitId = BuildConfig.ADMOB_REWARD_AD_UNIT_ID,
            testDeviceIds = testDeviceIds,
        )
    }
}
