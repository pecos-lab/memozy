package me.pecos.memozy.shared.umbrella

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    AppNavHost(
        mainViewModel = provideMainViewModel(),
        trashViewModel = provideTrashViewModel(),
        settingsViewModel = provideSettingsViewModel(),
    )
}
