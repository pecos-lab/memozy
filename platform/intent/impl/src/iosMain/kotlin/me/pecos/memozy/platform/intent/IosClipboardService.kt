package me.pecos.memozy.platform.intent

import platform.UIKit.UIPasteboard

class IosClipboardService : ClipboardService {
    override fun copyPlainText(label: String, text: String) {
        UIPasteboard.generalPasteboard.setString(text)
    }

    override fun readPrimaryText(): String? = UIPasteboard.generalPasteboard.string
}
