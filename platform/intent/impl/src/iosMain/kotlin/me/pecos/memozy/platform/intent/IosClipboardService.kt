package me.pecos.memozy.platform.intent

// TODO(C-1): UIPasteboard.generalPasteboard 기반 구현.
class IosClipboardService : ClipboardService {
    override fun copyPlainText(label: String, text: String) {
        println("[platform-intent] IosClipboardService.copyPlainText stub: [$label] ${text.take(32)}...")
    }

    override fun readPrimaryText(): String? {
        println("[platform-intent] IosClipboardService.readPrimaryText stub")
        return null
    }
}
