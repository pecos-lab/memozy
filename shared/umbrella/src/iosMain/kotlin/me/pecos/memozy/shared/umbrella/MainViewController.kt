package me.pecos.memozy.shared.umbrella

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

// Swift(UIViewControllerRepresentable)에서 호출할 CMP 진입점.
// Koin 초기화(`doInitKoin`)는 iOSApp.swift에서 선행 호출됨.
fun MainViewController(): UIViewController = ComposeUIViewController {
    MainScreen()
}
