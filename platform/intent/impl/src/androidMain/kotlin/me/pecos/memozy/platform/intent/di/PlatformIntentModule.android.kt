package me.pecos.memozy.platform.intent.di

import me.pecos.memozy.platform.intent.AndroidAppInfo
import me.pecos.memozy.platform.intent.AndroidClipboardService
import me.pecos.memozy.platform.intent.AndroidHapticService
import me.pecos.memozy.platform.intent.AndroidSharer
import me.pecos.memozy.platform.intent.AndroidToastPresenter
import me.pecos.memozy.platform.intent.AndroidUrlLauncher
import me.pecos.memozy.platform.intent.AppInfo
import me.pecos.memozy.platform.intent.ClipboardService
import me.pecos.memozy.platform.intent.HapticService
import me.pecos.memozy.platform.intent.Sharer
import me.pecos.memozy.platform.intent.ToastPresenter
import me.pecos.memozy.platform.intent.UrlLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformIntentModule(): Module = module {
    single<UrlLauncher> { AndroidUrlLauncher(androidContext()) }
    single<ClipboardService> { AndroidClipboardService(androidContext()) }
    single<Sharer> { AndroidSharer(androidContext()) }
    single<ToastPresenter> { AndroidToastPresenter(androidContext()) }
    single<HapticService> { AndroidHapticService(androidContext()) }
    single<AppInfo> { AndroidAppInfo(androidContext()) }
}
