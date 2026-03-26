# [#116] 필드명 리팩토링 설계 문서

## 개요

`sex`, `killThePecos` 라는 의미불명 필드명을 `category`, `content`로 변경한다.
DB 컬럼명은 `@ColumnInfo`로 고정하여 **마이그레이션 없음**.

---

## 영향 파일 목록

| 파일 | 경로 |
|------|------|
| `Memo.kt` | `app/src/main/java/me/pecos/nota/Memo.kt` |
| `MemoUiState.kt` | `app/src/main/java/me/pecos/nota/MemoUiState.kt` |
| `MainViewModel.kt` | `app/src/main/java/me/pecos/nota/MainViewModel.kt` |

---

## 변경 상세

### 1. `Memo.kt` — Entity

```kotlin
// Before
@Entity(tableName = "memo")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val sex: String,
    val killThePecos: String,
    val createdAt: Long = System.currentTimeMillis()
)

// After
@Entity(tableName = "memo")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "sex") val category: String,
    @ColumnInfo(name = "killThePecos") val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

> ⚠️ `@ColumnInfo` 필수. 없으면 Room이 컬럼명을 `category`, `content`로 인식해서 크래시.

---

### 2. `MemoUiState.kt` — UI 상태

```kotlin
// Before
data class MemoUiState(
    val id: Int,
    val name: String,
    val sex: String,
    val killThePecos: String,
    val createdAt: Long = 0L
)

// After
data class MemoUiState(
    val id: Int,
    val name: String,
    val category: String,
    val content: String,
    val createdAt: Long = 0L
)
```

---

### 3. `MainViewModel.kt` — ViewModel + 매핑 함수

#### `addMemo()` 시그니처

```kotlin
// Before
fun addMemo(name: String, sex: String, killThePecos: String)

// After
fun addMemo(name: String, category: String, content: String)
```

#### `addMemo()` 내부 Memo 생성

```kotlin
// Before
Memo(name = name, sex = sex, killThePecos = killThePecos, ...)

// After
Memo(name = name, category = category, content = content, ...)
```

#### `updateMemo()` — 변경 없음 (MemoUiState.toMemo() 위임이므로 자동 반영)

#### `toMemo()` Extension

```kotlin
// Before
fun MemoUiState.toMemo(): Memo = Memo(
    id = this.id, name = this.name,
    sex = this.sex, killThePecos = this.killThePecos,
    createdAt = this.createdAt
)

// After
fun MemoUiState.toMemo(): Memo = Memo(
    id = this.id, name = this.name,
    category = this.category, content = this.content,
    createdAt = this.createdAt
)
```

#### `toUiState()` Extension

```kotlin
// Before
fun Memo.toUiState(): MemoUiState = MemoUiState(
    id = this.id, name = this.name,
    sex = this.sex, killThePecos = this.killThePecos,
    createdAt = this.createdAt
)

// After
fun Memo.toUiState(): MemoUiState = MemoUiState(
    id = this.id, name = this.name,
    category = this.category, content = this.content,
    createdAt = this.createdAt
)
```

---

## `addMemo()` 호출부 변경

`addMemo(name, sex, killThePecos)` 를 호출하는 UI 레이어(Composable 등)에서
파라미터명을 `category`, `content`로 맞춰야 함.
**컴파일 에러로 자동 탐지되므로 누락 불가.**

---

## DB 마이그레이션 불필요 근거

| 항목 | 값 |
|------|----|
| 실제 DB 컬럼명 | `sex`, `killThePecos` (변경 없음) |
| Room이 참조하는 이름 | `@ColumnInfo(name = ...)` 지정값 |
| 코드 변수명 | `category`, `content` (변경됨) |

Room은 `@ColumnInfo(name)` 값을 컬럼명으로 사용하므로 DB 스키마는 동일.

---

## 작업 순서

1. `Memo.kt` 수정 → `@ColumnInfo` 추가 + 필드명 변경
2. `MemoUiState.kt` 수정 → 필드명 변경
3. `MainViewModel.kt` 수정 → `addMemo()`, `toMemo()`, `toUiState()` 수정
4. 빌드 → 컴파일 에러로 호출부 위치 확인 후 수정
5. 앱 실행 → 기존 데이터 조회/추가/삭제/수정 정상 여부 확인

---

## 체크리스트

- [ ] `Memo.category`에 `@ColumnInfo(name = "sex")` 적용
- [ ] `Memo.content`에 `@ColumnInfo(name = "killThePecos")` 적용
- [ ] `MemoUiState` 필드명 변경
- [ ] `addMemo()` 파라미터명 변경
- [ ] `toMemo()` 매핑 수정
- [ ] `toUiState()` 매핑 수정
- [ ] UI 호출부 파라미터명 수정
- [ ] 빌드 성공 확인
- [ ] 기존 데이터 정상 조회 확인 (크래시 없음)
