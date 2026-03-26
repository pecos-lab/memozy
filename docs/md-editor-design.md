# Markdown 에디터 도입 설계 문서

> 대상 이슈: MemoScreen MD 에디터 / 프리뷰 모드 전환 UX 구현
> 작성일: 2026-03-26

---

## 1. 현황 분석

### 현재 구조

```
MemoScreen.kt
├── WantedTextField          // 제목 입력 (외부 라이브러리: montage-android:3.3.0)
├── WantedTextArea           // 본문 입력 (외부 라이브러리: montage-android:3.3.0)
├── Category FlowRow         // 카테고리 선택
└── WantedButton             // 저장 버튼
```

- `bodyText: String` 상태를 `WantedTextArea`에 직접 바인딩
- WantedTextArea는 외부 Wanted Design System 컴포넌트 → MD 렌더링 불가
- 편집 모드 / 프리뷰 모드 구분 없음

---

## 2. MD 에디터 라이브러리 비교

### 후보군

| 항목 | **Markwon** | **compose-markdown** | **richeditor-compose** |
|------|------------|---------------------|----------------------|
| 유지 관리 | ✅ 활발 (v4.6.x) | ⚠️ 보통 (Markwon 래핑) | ⚠️ 소규모 |
| Compose 지원 | ❌ View 기반 → `AndroidView` 필요 | ✅ Compose-native | ✅ Compose-native |
| 렌더링 품질 | ✅ 업계 표준 (GFM 완전 지원) | ✅ (Markwon 기반) | ⚠️ 기본 수준 |
| 에디터 기능 | ❌ 렌더러 전용 | ❌ 렌더러 전용 | ✅ WYSIWYG 에디터 |
| 이미지 / 표 지원 | ✅ 플러그인 확장 | ✅ | ❌ |
| 번들 크기 | 중간 | 중간 | 작음 |
| 스타크 수 (GitHub) | ~3,000 | ~1,000 | ~500 |

### 추천: `compose-markdown` (프리뷰) + `BasicTextField` (편집)

**이유:**
- Compose-native → `AndroidView` 래퍼 불필요, 테마 통합 자연스러움
- 내부적으로 Markwon을 사용하므로 렌더링 품질 보장
- 에디터는 Compose 기본 `BasicTextField`로 커서/선택 영역 제어 → 툴바에서 MD 태그 삽입 용이

**의존성 추가 (app/build.gradle.kts):**
```kotlin
// MD 프리뷰 렌더러
implementation("com.github.jeziellago:compose-markdown:0.5.4")
```

---

## 3. 편집 모드 / 프리뷰 모드 상태 관리 설계

### 3-1. EditorMode Sealed Class

```kotlin
// 신규 파일: me/pecos/nota/editor/EditorMode.kt
sealed interface EditorMode {
    data object Edit    : EditorMode   // 텍스트 편집 상태
    data object Preview : EditorMode   // MD 렌더링 프리뷰 상태
}
```

### 3-2. MemoScreen 상태 변경 포인트

현재 MemoScreen의 상태:
```kotlin
var bodyText by remember { mutableStateOf(existingMemo.killThePecos) }
```

추가할 상태:
```kotlin
var editorMode by remember { mutableStateOf<EditorMode>(EditorMode.Edit) }
var bodyTextFieldValue by remember {
    mutableStateOf(TextFieldValue(existingMemo.killThePecos))
}
```

> `String` → `TextFieldValue` 교체 이유: 툴바에서 MD 태그 삽입 시 커서 위치(`selection`)를 유지해야 하므로.

### 3-3. 모드 전환 흐름

```
[Edit Mode]
  사용자 텍스트 입력
  툴바 버튼 클릭 → MD 태그 삽입
  "프리뷰" 토글 클릭
       ↓
[Preview Mode]
  bodyText를 MD 렌더러에 전달 → 렌더링 표시
  "편집" 토글 클릭
       ↑
[Edit Mode]
```

---

## 4. 컴포넌트 분리 설계

### 4-1. 구조 개요

```
MemoScreen.kt
├── MemoTopBar                        // 기존 TopBar (뒤로가기 + 제목)
├── WantedTextField                   // 기존 제목 입력 (변경 없음)
├── MemoBodyEditor  ← 신규 컴포넌트   // WantedTextArea 대체
│   ├── EditorToolbar                 // 편집 모드에서만 표시
│   ├── MemoEditArea                  // BasicTextField 래핑
│   └── MemoPreviewArea              // compose-markdown Markdown()
├── Category FlowRow                  // 기존 카테고리 선택 (변경 없음)
└── WantedButton                      // 기존 저장 버튼 (변경 없음)
```

### 4-2. MemoBodyEditor 컴포넌트 인터페이스

```kotlin
// 신규 파일: me/pecos/nota/editor/MemoBodyEditor.kt

@Composable
fun MemoBodyEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    mode: EditorMode,
    onModeChange: (EditorMode) -> Unit,
    modifier: Modifier = Modifier
)
```

**내부 구성:**
```kotlin
@Composable
fun MemoBodyEditor(...) {
    Column(modifier = modifier) {
        // 편집/프리뷰 모드 탭 토글
        EditorModeToggle(mode = mode, onModeChange = onModeChange)

        when (mode) {
            EditorMode.Edit -> {
                // 툴바 + BasicTextField
                EditorToolbar(
                    textFieldValue = value,
                    onValueChange = onValueChange
                )
                MemoEditArea(
                    value = value,
                    onValueChange = onValueChange
                )
            }
            EditorMode.Preview -> {
                // compose-markdown 렌더러
                MemoPreviewArea(markdownText = value.text)
            }
        }
    }
}
```

### 4-3. MemoScreen에서의 교체 포인트

```kotlin
// 기존 (제거 대상)
WantedTextArea(
    text = bodyText,
    placeholder = stringResource(R.string.memo_content_placeholder),
    title = stringResource(R.string.memo_content_label),
    onValueChange = { bodyText = it }
)

// 교체 후
MemoBodyEditor(
    value = bodyTextFieldValue,
    onValueChange = { bodyTextFieldValue = it },
    mode = editorMode,
    onModeChange = { editorMode = it }
)
```

> `onSave` 콜백의 `killThePecos` 인자는 `bodyTextFieldValue.text`로 전달.

---

## 5. 툴바 UI 설계

### 5-1. EditorToolbar 컴포넌트

```kotlin
// me/pecos/nota/editor/EditorToolbar.kt

@Composable
fun EditorToolbar(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
)
```

### 5-2. 툴바 버튼 목록

| 버튼 | 아이콘 | 삽입 로직 | 결과 예시 |
|------|--------|----------|----------|
| 볼드 | `FormatBold` | 선택 텍스트 `**` 감싸기 | `**텍스트**` |
| 이탤릭 | `FormatItalic` | 선택 텍스트 `*` 감싸기 | `*텍스트*` |
| 취소선 | `FormatStrikethrough` | 선택 텍스트 `~~` 감싸기 | `~~텍스트~~` |
| H1 헤더 | `Title` | 커서 행 앞에 `# ` 삽입 | `# 제목` |
| H2 헤더 | `TextFields` | 커서 행 앞에 `## ` 삽입 | `## 소제목` |
| 불릿 리스트 | `FormatListBulleted` | 커서 행 앞에 `- ` 삽입 | `- 항목` |
| 번호 리스트 | `FormatListNumbered` | 커서 행 앞에 `1. ` 삽입 | `1. 항목` |
| 인라인 코드 | `Code` | 선택 텍스트 `` ` `` 감싸기 | `` `코드` `` |
| 인용구 | `FormatQuote` | 커서 행 앞에 `> ` 삽입 | `> 인용` |
| 수평선 | `HorizontalRule` | 줄바꿈 후 `---` 삽입 | `---` |

### 5-3. 툴바 레이아웃

```
┌─────────────────────────────────────────────────┐
│ [B] [I] [S]  │  [H1] [H2]  │  [•] [1.] [>] [</>] [—] │
└─────────────────────────────────────────────────┘
```

- `LazyRow` 사용 → 좁은 화면에서 가로 스크롤 지원
- 아이콘: `androidx.compose.material.icons.Icons.Default.*` 활용
- 배경: `colors.screenBackground` + 하단 구분선 1dp

### 5-4. MD 태그 삽입 핵심 로직

```kotlin
// 텍스트 감싸기 (볼드, 이탤릭, 취소선, 코드)
fun TextFieldValue.wrapSelection(tag: String): TextFieldValue {
    val start = selection.min
    val end = selection.max
    val selected = text.substring(start, end)
    val newText = text.substring(0, start) + "$tag$selected$tag" + text.substring(end)
    val newCursor = if (start == end) start + tag.length
                    else end + tag.length * 2
    return copy(
        text = newText,
        selection = TextRange(newCursor)
    )
}

// 행 앞에 접두어 삽입 (헤더, 리스트, 인용구)
fun TextFieldValue.prefixCurrentLine(prefix: String): TextFieldValue {
    val lineStart = text.lastIndexOf('\n', selection.min - 1) + 1
    val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
    val newCursor = selection.min + prefix.length
    return copy(
        text = newText,
        selection = TextRange(newCursor)
    )
}
```

---

## 6. EditorModeToggle UI 설계

```
┌─────────────────────────────────┐
│  [✏️ 편집]          [👁 프리뷰]  │
└─────────────────────────────────┘
```

- `Row` + 두 개의 텍스트 버튼 / 탭 형태
- 선택된 탭: `colors.chipBackground` + `colors.chipText`
- 미선택: `Color.Transparent` + `colors.textSecondary`
- `RoundedCornerShape(8.dp)` 적용

---

## 7. 파일 구조 (신규 생성 대상)

```
app/src/main/java/me/pecos/nota/
└── editor/
    ├── EditorMode.kt          // sealed interface 정의
    ├── MemoBodyEditor.kt      // 최상위 에디터 컴포넌트
    ├── EditorModeToggle.kt    // 편집/프리뷰 탭 토글
    ├── EditorToolbar.kt       // MD 서식 툴바
    ├── MemoEditArea.kt        // BasicTextField 래핑 편집 영역
    ├── MemoPreviewArea.kt     // compose-markdown 렌더링 영역
    └── MarkdownInsertUtils.kt // wrapSelection, prefixCurrentLine 등 유틸
```

---

## 8. MemoScreen 변경 최소화 전략

변경되는 부분만 나열:

| 위치 | 변경 내용 |
|------|----------|
| `import` 추가 | `TextFieldValue`, `TextRange`, `MemoBodyEditor`, `EditorMode` |
| 상태 추가 | `editorMode`, `bodyTextFieldValue` (기존 `bodyText` 대체) |
| `WantedTextArea` 교체 | `MemoBodyEditor(...)` 로 교체 (1개 컴포넌트 교체) |
| `onSave` 콜백 | `bodyText` → `bodyTextFieldValue.text` |

기존 기능 (제목 입력, 카테고리 선택, 저장 버튼, 뒤로가기) **모두 유지**.

---

## 9. 미결 사항 (구현 전 확인 필요)

1. **compose-markdown 버전 호환성**: 현재 프로젝트의 Compose BOM 버전 확인 필요 (`libs.versions.toml`)
2. **저장 형식**: MD 원문(`**bold**`) 그대로 DB 저장 vs 별도 변환 여부 → 원문 저장 권장
3. **메인 화면(목록) 렌더링**: 카드에서 MD 프리뷰 표시 여부 → 별도 이슈로 분리 권장
4. **AppPopup.kt**: 최근 커밋에 추가된 파일, 팝업에서도 MD 렌더링 필요 여부 확인
