package me.pecos.memozy.platform.intent

// TODO(C-1): UIActivityViewController 기반 구현. root UIViewController 획득 경로 C-1 에서 결정.
class IosSharer : Sharer {
    override fun shareText(text: String, chooserTitle: String?) {
        println("[platform-intent] IosSharer.shareText stub: ${text.take(32)}...")
    }

    override fun shareFile(path: String, mimeType: String, chooserTitle: String?) {
        println("[platform-intent] IosSharer.shareFile stub: $path ($mimeType)")
    }
}
