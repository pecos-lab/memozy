---
name: adding-dark-mode
description: 새 색상을 추가하거나 기존 색상을 수정할 때 사용. "색상 추가", "다크모드 색상", "라이트/다크 color 추가", "values-night 업데이트" 등의 요청에 활성화됨. values/colors.xml과 values-night/colors.xml 두 파일을 항상 함께 업데이트해야 함.
version: 1.0.0
---

# Adding Dark Mode Colors 스킬

이 프로젝트는 Android의 리소스 qualifier 방식으로 다크모드를 지원함.

- **라이트 모드**: `app/src/main/res/values/colors.xml`
- **다크 모드**: `app/src/main/res/values-night/colors.xml`

> ⚠️ **항상 두 파일을 함께 수정.** 한 쪽만 추가하면 라이트/다크 전환 시 색상이 깨짐.

## 현재 색상 시스템

### 라이트 모드 (`values/colors.xml`)

```xml
<!-- TopBar -->
<color name="topbar_title">#1C1C1E</color>

<!-- Navigation -->
<color name="nav_background">#FFFFFF</color>
<color name="nav_border">#E0E0E0</color>
<color name="nav_icon_selected">#000000</color>
<color name="nav_icon_unselected">#66000000</color>

<!-- Semantic -->
<color name="screen_background">#FFFFFF</color>
<color name="card_background">#FFFFFF</color>
<color name="card_border">#E0E0E0</color>
<color name="text_title">#000000</color>
<color name="text_body">#616161</color>
<color name="text_secondary">#9E9E9E</color>
<color name="chip_background">#E8F0FE</color>
<color name="chip_text">#1D6BF3</color>
```

### 다크 모드 (`values-night/colors.xml`)

```xml
<!-- TopBar -->
<color name="topbar_title">#F2F2F7</color>

<!-- Navigation -->
<color name="nav_background">#1C1C1E</color>
<color name="nav_border">#3A3A3C</color>
<color name="nav_icon_selected">#F2F2F7</color>
<color name="nav_icon_unselected">#8E8E93</color>

<!-- Semantic -->
<color name="screen_background">#1C1C1E</color>
<color name="card_background">#2C2C2E</color>
<color name="card_border">#3A3A3C</color>
<color name="text_title">#F2F2F7</color>
<color name="text_body">#EBEBF5</color>
<color name="text_secondary">#8E8E93</color>
<color name="chip_background">#3A3A3C</color>
<color name="chip_text">#6B9FFF</color>
```

## 새 색상 추가 절차

### 1. 두 파일에 동시 추가

`values/colors.xml`:
```xml
<color name="{color_name}">{라이트 hex}</color>
```

`values-night/colors.xml`:
```xml
<color name="{color_name}">{다크 hex}</color>
```

### 2. Composable에서 사용

```kotlin
colorResource(R.color.{color_name})
```

시스템 테마에 따라 자동으로 라이트/다크 값이 선택됨.

## 색상 네이밍 컨벤션

- `{컴포넌트}_{역할}` 형태 사용
- 예: `topbar_title`, `nav_background`, `card_border`
- 전체 소문자 + underscore

## 다크모드 색상 가이드

| 역할 | 라이트 | 다크 |
|------|--------|------|
| 배경 (주) | `#FFFFFF` | `#1C1C1E` |
| 배경 (카드) | `#FFFFFF` | `#2C2C2E` |
| 테두리 | `#E0E0E0` | `#3A3A3C` |
| 텍스트 (강조) | `#000000` | `#F2F2F7` |
| 텍스트 (본문) | `#616161` | `#EBEBF5` |
| 텍스트 (보조) | `#9E9E9E` | `#8E8E93` |

> 이 프로젝트는 iOS 스타일 다크모드 팔레트(Apple HIG 기반)를 따름.
