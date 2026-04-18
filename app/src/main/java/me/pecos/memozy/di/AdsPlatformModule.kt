package me.pecos.memozy.di

import me.pecos.memozy.platform.ads.AdsService
import me.pecos.memozy.platform.ads.AndroidAdsService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val adsPlatformModule = module {
    single<AdsService> { AndroidAdsService(androidContext()) }
}
