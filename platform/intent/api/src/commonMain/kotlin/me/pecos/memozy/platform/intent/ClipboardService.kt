package me.pecos.memozy.platform.intent

interface ClipboardService {
    fun copyPlainText(label: String, text: String)

    fun readPrimaryText(): String?
}
