package me.pecos.memozy.shared.umbrella

import platform.Foundation.NSBundle

/**
 * iOS 측 secrets pipe — Android 의 BuildConfig.* 와 대응.
 *
 * 현재 구현: `iosApp/iosApp/Info.plist` 에서 키를 읽어옴. 빌드 자동화 (xcconfig + Info.plist
 * 변수 치환) 까지 가는 게 표준이지만, 일단은 Info.plist 에 직접 값을 박는 방식으로 시작.
 *
 * 필수 Info.plist 키 (값 미지정 시 빈 문자열 → Supabase / AI 호출 실패):
 *   - SUPABASE_URL
 *   - SUPABASE_ANON_KEY
 *   - WORKER_URL
 *   - APP_SECRET_KEY
 *
 * 운영 빌드는 xcconfig + Info.plist 변수 치환 으로 전환 필요. (별건)
 */
internal object IosSecrets {
    val supabaseUrl: String get() = readKey("SUPABASE_URL")
    val supabaseAnonKey: String get() = readKey("SUPABASE_ANON_KEY")
    val workerUrl: String get() = readKey("WORKER_URL")
    val appSecretKey: String get() = readKey("APP_SECRET_KEY")

    private fun readKey(key: String): String =
        NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String ?: ""
}
