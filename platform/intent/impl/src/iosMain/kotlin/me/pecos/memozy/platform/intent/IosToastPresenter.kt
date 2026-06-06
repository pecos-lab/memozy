package me.pecos.memozy.platform.intent

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UILabel
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

@OptIn(ExperimentalForeignApi::class)
class IosToastPresenter : ToastPresenter {
    // SwiftUI 루트(UIHostingController.view)에 UIKit subview 직접 추가는 거부됨
    // ("Adding 'UILabel' as a subview of UIHostingController.view is not supported"
    //  콘솔 경고 + 실제 표시 안 됨 — #381 토스트 가 안 보였던 진짜 원인).
    // 윈도우에 직접 addSubview 하면 SwiftUI 계층 밖이라 안전하게 위에 떠 있는다.
    override fun show(text: String, duration: ToastDuration) {
        val window = findActiveWindow() ?: return
        val seconds = when (duration) {
            ToastDuration.Short -> 2.0
            ToastDuration.Long -> 4.0
        }

        val (windowWidth, windowHeight) = window.bounds.useContents { size.width to size.height }
        val horizontalPadding = 20.0
        val bottomInset = 100.0
        val height = 48.0
        val frame = CGRectMake(
            horizontalPadding,
            windowHeight - height - bottomInset,
            windowWidth - horizontalPadding * 2,
            height,
        )

        val label = UILabel(frame = frame)
        label.text = text
        label.textColor = UIColor.whiteColor
        label.backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.75)
        label.textAlignment = NSTextAlignmentCenter
        label.numberOfLines = 0
        label.layer.cornerRadius = 12.0
        label.clipsToBounds = true
        label.alpha = 0.0

        window.addSubview(label)
        UIView.animateWithDuration(0.2) { label.alpha = 1.0 }

        val delay = dispatch_time(DISPATCH_TIME_NOW, (seconds * NSEC_PER_SEC.toDouble()).toLong())
        dispatch_after(delay, dispatch_get_main_queue()) {
            UIView.animateWithDuration(
                duration = 0.2,
                animations = { label.alpha = 0.0 },
                completion = { _ -> label.removeFromSuperview() },
            )
        }
    }

    private fun findActiveWindow(): UIWindow? {
        val windowScenes = UIApplication.sharedApplication.connectedScenes.filterIsInstance<UIWindowScene>()
        val activeScene = windowScenes.firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?: windowScenes.firstOrNull()
            ?: return null
        val windows = activeScene.windows.filterIsInstance<UIWindow>()
        return windows.firstOrNull { it.isKeyWindow() } ?: windows.firstOrNull()
    }
}
