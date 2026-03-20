---
name: adding-localization
description: 새 언어를 추가하거나 기존 문자열 리소스에 번역을 추가할 때 사용. "새 언어 추가", "번역 추가", "values-xx 만들어줘", "언어 지원 추가" 등의 요청에 활성화됨.
version: 1.0.0
---

# Adding Localization 스킬

이 프로젝트는 한국어(기본), 영어(`values-en`), 일본어(`values-ja`)를 지원함.

## 현재 지원 언어

| 언어 | 리소스 폴더 | TimeZone |
|------|------------|----------|
| 한국어 (기본) | `values/` | Asia/Seoul |
| 영어 | `values-en/` | America/New_York |
| 일본어 | `values-ja/` | Asia/Tokyo |

## 새 언어 추가 절차

### 1. strings.xml 생성

`app/src/main/res/values-{languageCode}/strings.xml` 생성.

기존 `values/strings.xml`의 모든 키를 유지하고 값만 번역:

```xml
<resources>
    <string name="app_name">pecos.nota</string>

    <!-- Settings -->
    <string name="settings">{번역}</string>
    <string name="language_settings">{번역}</string>
    <string name="open_source_license">{번역}</string>
    <string name="reset_memos">{번역}</string>
    <string name="reset_confirm">{번역}</string>
    <string name="reset">{번역}</string>
    <string name="cancel">{번역}</string>
    <string name="close">{번역}</string>
    <string name="theme_settings">{번역}</string>
    <string name="theme_light">{번역}</string>
    <string name="theme_dark">{번역}</string>
    <string name="theme_system">{번역}</string>
    <string name="license_content">• Jetpack Compose - Apache 2.0\n• Room Database - Apache 2.0\n• Kotlin Coroutines - Apache 2.0\n• Navigation Compose - Apache 2.0</string>

    <!-- Memo -->
    <string name="memo_title_label">{번역}</string>
    <string name="memo_title_placeholder">{번역}</string>
    <string name="memo_content_label">{번역}</string>
    <string name="memo_content_placeholder">{번역}</string>
    <string name="save">{번역}</string>
    <string name="add_memo">{번역}</string>
    <string name="man">{번역}</string>
    <string name="woman">{번역}</string>
</resources>
```

> ⚠️ `values/strings.xml`에 새 키를 추가했다면 모든 언어 파일에도 해당 키 추가 필요.

### 2. SettingsViewModel에 언어 추가

`SettingsViewModel.kt`의 `Language` enum에 새 언어 추가:

```kotlin
enum class Language(val code: String, val displayName: String) {
    KOREAN("ko", "한국어"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    // 새 언어 추가
    {NEW_LANG}("{code}", "{표시명}")
}
```

### 3. TimeZone 추가

`SettingsViewModel.kt` 또는 날짜/시간 처리 부분에서 언어별 TimeZone 매핑 업데이트:

```kotlin
fun timezoneForLanguage(languageCode: String): TimeZone = when(languageCode) {
    "ko" -> TimeZone.getTimeZone("Asia/Seoul")
    "en" -> TimeZone.getTimeZone("America/New_York")
    "ja" -> TimeZone.getTimeZone("Asia/Tokyo")
    "{code}" -> TimeZone.getTimeZone("{TimeZone ID}")
    else -> TimeZone.getDefault()
}
```

### 4. 기존 언어 파일에 새 키 추가

`values/strings.xml`에 새 키를 추가한 경우:
- `values-en/strings.xml`
- `values-ja/strings.xml`
- 이후 추가한 모든 `values-{code}/strings.xml`

동일한 key로 번역값 추가.

## 주요 TimeZone 참고

```
한국: Asia/Seoul
일본: Asia/Tokyo
미국 동부: America/New_York
미국 서부: America/Los_Angeles
영국: Europe/London
독일/프랑스: Europe/Paris
중국: Asia/Shanghai
```
