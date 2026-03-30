---
name: using-shadcn-compose
description: shadcn-compose(shadcn/ui의 Jetpack Compose 포트) 컴포넌트를 사용하거나 추가할 때 사용. "shadcn 버튼 써줘", "shadcn 컴포넌트 추가", "shadcn Input 쓰고 싶어" 요청에 활성화됨.
version: 1.0.0
---

# shadcn-compose 스킬

shadcn/ui의 Jetpack Compose 포트. 웹 shadcn/ui와 동일한 컴포넌트 철학, Kotlin/Compose 문법으로 제공.
공식 사이트: https://shadcn-compose.site/

---

## 설치

`app/build.gradle.kts`에 의존성 추가:

```kotlin
dependencies {
    implementation("io.github.derangga:shadcn-ui-kmp:0.2.0")
}
```

> 최신 버전은 https://github.com/derangga/shadcn-ui-kmp/releases 확인

---

## 테마 설정

`MainActivity.kt`에서 `ShadcnTheme`으로 감싸기:

```kotlin
setContent {
    ShadcnTheme {
        // 앱 컨텐츠
    }
}
```

### 커스텀 색상/라디우스 적용

```kotlin
ShadcnTheme(
    isDarkTheme = isSystemInDarkTheme(),
    shadcnLightColors = LightStyles,  // ShadcnStyles 타입 (ShadcnColors 아님)
    shadcnDarkColors = DarkStyles,
    shadcnRadius = Radius,            // ShadcnRadius 타입
) { /* content */ }
```

**색상 토큰** (`ShadcnStyles`):
`primary`, `primaryForeground`, `secondary`, `secondaryForeground`,
`background`, `foreground`, `card`, `cardForeground`,
`popover`, `popoverForeground`, `destructive`,
`muted`, `mutedForeground`, `accent`, `accentForeground`,
`border`, `input`, `ring`, `chart1`~`chart5`

**라디우스 토큰** (`ShadcnRadius`):
| 토큰 | 값 |
|------|----|
| `sm` | base - 4.dp |
| `md` | base - 2.dp |
| `lg` | base |
| `xl` | base + 4.dp |
| `full` | 999.dp |

---

## 사용 가능한 컴포넌트 (28+)

| 카테고리 | 컴포넌트 |
|----------|----------|
| 레이아웃 | Card, Sidebar, Drawer(Bottom Sheet) |
| 폼 컨트롤 | Button, Input, Checkbox, Radio Group, Select, Switch, Slider, Combobox |
| 데이터 표시 | Avatar, Badge, Calendar, Carousel, Progress |
| 오버레이 | Dialog, Dropdown Menu, Popover |
| 내비게이션 | Tabs |
| 피드백 | Alert, Skeleton, Sonner(toast) |
| 기타 | Accordion, Date Picker |

---

## 컴포넌트 사용 예시

### Button

```kotlin
Button(
    onClick = { /* handle click */ },
    variant = ButtonVariant.Default,  // Default, Destructive, Outline, Secondary, Ghost, Link
    size = ButtonSize.Lg              // Default, Sm, Xs, Lg, Icon, IconSm
) {
    Text("클릭")
}
```

### Input

```kotlin
var text by remember { mutableStateOf("") }
Input(
    value = text,
    onValueChange = { text = it },
    placeholder = "입력하세요",
    singleLine = true
)
```

### Card

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    radius = MaterialTheme.radius.lg,           // 선택: 모서리 반경
    shadow = MaterialTheme.styles.shadowMd(),   // 선택: shadow2xs, shadowXs, shadowSm, shadow, shadowMd, shadowLg, shadowXl, shadow2xl
) {
    // 컨텐츠
}
// ⚠️ background 파라미터 없음 — 배경색은 MaterialTheme.styles.card로 내부 처리됨
```

### Badge

```kotlin
Badge(
    variant = BadgeVariant.Default  // Default, Secondary, Destructive, Outline
) {
    Text("New")
}
```

---

## 임포트 패키지

```
com.shadcn.ui.components.*   // 모든 컴포넌트 (Button, Input, Card, Badge 등)
com.shadcn.ui.themes.*       // 테마 관련 (ShadcnTheme, ShadcnRadius, ShadcnStyles)
```

---

## 주의사항

- **Kotlin 2.2.0+** 필요
- 아직 v0.2.0 초기 버전 — 일부 컴포넌트 API 문서 미완성
- 공식 shadcn/ui와 무관한 커뮤니티 포트
- 색상 변환 도구: tweakcn.com CSS → Kotlin Color 변환 지원 (사이트 내 제공)
