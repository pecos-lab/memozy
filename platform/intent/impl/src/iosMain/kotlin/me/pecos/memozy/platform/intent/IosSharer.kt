package me.pecos.memozy.platform.intent

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

class IosSharer : Sharer {
    override fun shareText(text: String, chooserTitle: String?) {
        present(UIActivityViewController(activityItems = listOf(text), applicationActivities = null))
    }

    override fun shareFile(path: String, mimeType: String, chooserTitle: String?) {
        val url = NSURL.fileURLWithPath(path)
        present(UIActivityViewController(activityItems = listOf(url), applicationActivities = null))
    }

    private fun present(vc: UIActivityViewController) {
        val root = topViewController() ?: return
        root.presentViewController(vc, animated = true, completion = null)
    }

    private fun topViewController(): UIViewController? {
        var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (vc?.presentedViewController != null) {
            vc = vc.presentedViewController
        }
        return vc
    }
}
