package me.pecos.memozy.platform.intent

// TODO(C-1): NSBundle.mainBundle.infoDictionary CFBundleShortVersionString / CFBundleIdentifier 매핑.
class IosAppInfo : AppInfo {
    override val versionName: String = ""

    override val packageName: String = ""
}
