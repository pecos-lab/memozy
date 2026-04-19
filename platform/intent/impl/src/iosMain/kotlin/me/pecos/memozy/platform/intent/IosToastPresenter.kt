package me.pecos.memozy.platform.intent

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

@OptIn(ExperimentalForeignApi::class)
class IosToastPresenter : ToastPresenter {
    override fun show(text: String, duration: ToastDuration) {
        val parent = topViewController()?.view ?: return
        val seconds = when (duration) {
            ToastDuration.Short -> 2.0
            ToastDuration.Long -> 4.0
        }

        val (parentWidth, parentHeight) = parent.bounds.useContents { size.width to size.height }
        val horizontalPadding = 20.0
        val bottomInset = 100.0
        val height = 48.0
        val frame = CGRectMake(
            horizontalPadding,
            parentHeight - height - bottomInset,
            parentWidth - horizontalPadding * 2,
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

        parent.addSubview(label)
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

    private fun topViewController(): UIViewController? {
        var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (vc?.presentedViewController != null) {
            vc = vc.presentedViewController
        }
        return vc
    }
}
