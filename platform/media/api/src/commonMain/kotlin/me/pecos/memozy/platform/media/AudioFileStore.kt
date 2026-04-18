package me.pecos.memozy.platform.media

/**
 * 오디오 파일 저장 경로/복사 추상화. 기존 java.io.File 직접 접근을 대체하기 위한 plat-agnostic 계약.
 * - cachePath: 녹음 임시 파일용 캐시 경로
 * - permanentPath: 영구 보존용 경로 (앱 내부 저장소)
 * - downloadsPath: 공용 Downloads 경로. iOS 등 샌드박스 환경에서는 null 반환 가능.
 */
interface AudioFileStore {
    fun cachePath(name: String): String

    fun permanentPath(safeName: String): String

    fun downloadsPath(name: String): String?

    fun exists(path: String): Boolean

    fun copy(srcPath: String, dstPath: String): Boolean
}
