package me.pecos.memozy.platform.intent

interface Sharer {
    fun shareText(text: String, chooserTitle: String? = null)

    fun shareFile(path: String, mimeType: String, chooserTitle: String? = null)
}
