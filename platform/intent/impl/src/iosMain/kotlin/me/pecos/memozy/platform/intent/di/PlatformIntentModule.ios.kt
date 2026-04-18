package me.pecos.memozy.platform.intent.di

import me.pecos.memozy.platform.intent.AppInfo
import me.pecos.memozy.platform.intent.ClipboardService
import me.pecos.memozy.platform.intent.HapticService
import me.pecos.memozy.platform.intent.IosAppInfo
import me.pecos.memozy.platform.intent.IosClipboardService
import me.pecos.memozy.platform.intent.IosHapticService
import me.pecos.memozy.platform.intent.IosSharer
import me.pecos.memozy.platform.intent.IosToastPresenter
import me.pecos.memozy.platform.intent.IosUrlLauncher
import me.pecos.memozy.platform.intent.Sharer
import me.pecos.memozy.platform.intent.ToastPresenter
import me.pecos.memozy.platform.intent.UrlLauncher
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformIntentModule(): Module = module {
    single<UrlLauncher> { IosUrlLauncher() }
    single<ClipboardService> { IosClipboardService() }
    single<Sharer> { IosSharer() }
    single<ToastPresenter> { IosToastPresenter() }
    single<HapticService> { IosHapticService() }
    single<AppInfo> { IosAppInfo() }
}
