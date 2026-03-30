# Color — ShadcnColors

Shadcn Compose는 `ShadcnColors` 인터페이스를 통해 색상 스킴을 관리합니다.

---

## ShadcnColors 속성

| 속성 | 설명 |
|------|------|
| `background` | 앱 전체 배경색 |
| `foreground` | 기본 텍스트/전경색 |
| `card` | 카드 배경색 |
| `cardForeground` | 카드 텍스트색 |
| `popover` | 팝오버 배경색 |
| `popoverForeground` | 팝오버 텍스트색 |
| `primary` | 주요 브랜드색 |
| `primaryForeground` | primary 위 텍스트색 |
| `secondary` | 보조색 |
| `secondaryForeground` | secondary 위 텍스트색 |
| `muted` | 비활성/음소거 상태색 |
| `mutedForeground` | muted 위 텍스트색 |
| `accent` | 강조색 |
| `accentForeground` | accent 위 텍스트색 |
| `destructive` | 위험/삭제 동작색 |
| `destructiveForeground` | destructive 위 텍스트색 |
| `border` | 테두리색 |
| `input` | 입력 필드 테두리색 |
| `ring` | 포커스 링 색상 |
| `chart1` ~ `chart5` | 차트 시각화 색상 |
| `sidebar` | 사이드바 배경색 |
| `sidebarForeground` | 사이드바 텍스트색 |
| `sidebarPrimary` | 사이드바 주요색 |
| `sidebarPrimaryForeground` | 사이드바 주요색 위 텍스트 |
| `sidebarAccent` | 사이드바 강조색 |
| `sidebarAccentForeground` | 사이드바 강조색 위 텍스트 |
| `sidebarBorder` | 사이드바 테두리색 |
| `sidebarRing` | 사이드바 포커스 링 |
| `snackbar` | 스낵바 배경색 |

---

## 커스텀 색상 정의 예시

```kotlin
object ShadcnLightColors : ShadcnColors {
    override val background: Color = Color(0xFFF7F3F9)
    override val foreground: Color = Color(0xFF374151)
    override val card: Color = Color(0xFFFFFFFF)
    override val cardForeground: Color = Color(0xFF374151)
    override val popover: Color = Color(0xFFFFFFFF)
    override val popoverForeground: Color = Color(0xFF374151)
    override val primary: Color = Color(0xFFA78BFA)
    override val primaryForeground: Color = Color(0xFFFFFFFF)
    override val secondary: Color = Color(0xFFE9D8FD)
    override val secondaryForeground: Color = Color(0xFF4B5563)
    override val muted: Color = Color(0xFFF3E8FF)
    override val mutedForeground: Color = Color(0xFF6B7280)
    override val accent: Color = Color(0xFFF3E5F5)
    override val accentForeground: Color = Color(0xFF374151)
    override val destructive: Color = Color(0xFFFCA5A5)
    override val destructiveForeground: Color = Color(0xFFFFFFFF)
    override val border: Color = Color(0xFFE9D8FD)
    override val input: Color = Color(0xFFE9D8FD)
    override val ring: Color = Color(0xFFA78BFA)
    override val chart1: Color = Color.Unspecified
    override val chart2: Color = Color.Unspecified
    override val chart3: Color = Color.Unspecified
    override val chart4: Color = Color.Unspecified
    override val chart5: Color = Color.Unspecified
    override val sidebar: Color = Color(0xFFE9D8FD)
    override val sidebarForeground: Color = Color(0xFF374151)
    override val sidebarPrimary: Color = Color(0xFFA78BFA)
    override val sidebarPrimaryForeground: Color = Color(0xFFFFFFFF)
    override val sidebarAccent: Color = Color(0xFFF3E5F5)
    override val sidebarAccentForeground: Color = Color(0xFF374151)
    override val sidebarBorder: Color = Color(0xFFE9D8FD)
    override val sidebarRing: Color = Color(0xFFA78BFA)
    override val snackbar = Color(0xFFF7F3F9)
}
```

---

## 사용법

테마 내에서 색상 접근:

```kotlin
ShadcnTheme.colorScheme.primary
ShadcnTheme.colorScheme.background
ShadcnTheme.colorScheme.destructive
```