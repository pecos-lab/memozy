package me.pecos.memozy.platform.intent

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosUrlLauncher : UrlLauncher {
    override fun open(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        val app = UIApplication.sharedApplication
        if (!app.canOpenURL(nsUrl)) return false
        app.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
        return true
    }

    override fun openPreferringPackage(url: String, preferredPackage: String): Boolean = open(url)

    // itms-apps 스킴은 설치된 App Store 앱으로 직접 진입.
    // 일반 https://apps.apple.com/account/subscriptions 도 가능하지만 Safari 경유 가능성 있음.
    override fun openManageSubscriptions(): Boolean =
        open("https://apps.apple.com/account/subscriptions")
}
