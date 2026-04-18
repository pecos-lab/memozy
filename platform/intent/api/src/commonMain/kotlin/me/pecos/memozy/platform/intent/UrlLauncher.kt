package me.pecos.memozy.platform.intent

interface UrlLauncher {
    fun open(url: String): Boolean

    fun openPreferringPackage(url: String, preferredPackage: String): Boolean
}
