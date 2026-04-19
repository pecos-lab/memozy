package me.pecos.memozy.platform.intent

/**
 * iOS 에는 안드로이드의 content:// URI 개념이 없음.
 * 공유 확장(Share Extension) 이 별도 구현되면 이 클래스를 실제 구현으로 대체.
 * 지금은 항상 null 을 반환해 공유 이미지 OCR 기능이 호출 없이 비활성 상태가 되도록.
 */
class IosSharedContentReader : SharedContentReader {
    override fun readBytes(uri: String): ByteArray? = null
    override fun getMimeType(uri: String): String? = null
}
