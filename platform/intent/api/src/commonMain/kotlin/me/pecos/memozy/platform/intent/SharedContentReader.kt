package me.pecos.memozy.platform.intent

/**
 * 공유 인텐트로 들어온 콘텐츠(이미지/PDF 등)를 URI 문자열로 읽어오기 위한 추상.
 * iOS 에는 같은 개념의 content:// URI 가 없어, 공유 확장이 없는 동안은
 * 두 메서드 모두 null 을 반환한다.
 */
interface SharedContentReader {
    fun readBytes(uri: String): ByteArray?

    fun getMimeType(uri: String): String?
}
