# Theme Application — ShadcnTheme

`ShadcnTheme` 컴포저블을 통해 색상과 반경을 앱 전체에 일관되게 적용합니다.

---

## 기본 적용

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShadcnTheme {
                // 기본 테마로 앱 구성
            }
        }
    }
}
```

---

## 커스텀 테마 적용

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShadcnTheme(
                shadcnLightColors = ShadcnLightColors,  // 커스텀 라이트 색상
                shadcnRadius = Radius,                    // 커스텀 반경
            ) {
                // 커스텀 테마가 적용된 앱 구성
            }
        }
    }
}
```

---

## 테마 값 접근

컴포저블 내에서 현재 테마 값에 접근:

```kotlin
// 색상
ShadcnTheme.colorScheme.primary
ShadcnTheme.colorScheme.background
ShadcnTheme.colorScheme.foreground

// 반경은 ShadcnRadius 객체를 통해 접근
```
