package me.pecos.memozy.shared.umbrella

import androidx.compose.ui.window.ComposeUIViewController
import me.pecos.memozy.platform.ads.AdsService
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    AppNavHost(
        mainViewModel = provideMainViewModel(),
        trashViewModel = provideTrashViewModel(),
        settingsViewModel = provideSettingsViewModel(),
        memoPlainNavigation = provideMemoPlainNavigation(),
        adsService = KoinPlatform.getKoin().get<AdsService>(),
    )
}
