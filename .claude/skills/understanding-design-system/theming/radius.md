# Radius — ShadcnRadius

Shadcn Compose는 `ShadcnRadius` 인터페이스를 통해 컴포넌트 전체의 모서리 반경을 일관되게 관리합니다.

---

## ShadcnRadius 속성

| 속성 | 용도 | 기본값 |
|------|------|--------|
| `radius` | 기준 반경값 | `4.dp` |
| `sm` | 작은 컴포넌트 | `max(0.dp, radius - 4.dp)` |
| `md` | 중간 크기 요소 | `max(0.dp, radius - 2.dp)` |
| `lg` | 기본/표준 크기 | `radius` |
| `xl` | 큰 요소 | `max(0.dp, radius + 4.dp)` |
| `full` | 완전 원형 | `999.dp` |

---

## 커스텀 반경 정의 예시

```kotlin
object Radius : ShadcnRadius {
    override val radius: Dp = 4.dp
    override val sm: Dp = max(0.dp, radius - 4.dp)
    override val md: Dp = max(0.dp, radius - 2.dp)
    override val lg: Dp = radius
    override val xl: Dp = max(0.dp, radius + 4.dp)
    override val full: Dp = 999.dp
}
```

---

## 사용법

```kotlin
ShadcnTheme(
    shadcnRadius = Radius,
) {
    // 컴포넌트들이 자동으로 이 반경 설정을 사용
}
```
