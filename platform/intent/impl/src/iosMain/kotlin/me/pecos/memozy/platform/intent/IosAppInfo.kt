package me.pecos.memozy.platform.intent

import platform.Foundation.NSBundle

class IosAppInfo : AppInfo {
    override val versionName: String =
        NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: ""

    override val packageName: String = NSBundle.mainBundle.bundleIdentifier ?: ""
}
