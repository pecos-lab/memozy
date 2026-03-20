# Button

작업을 수행하는데 사용되는 클릭 가능한 요소입니다. 콘텐츠 혹은 화면의 하단 영역에 배치되며 다양한 방식으로 콘텐츠와 함께 상호 작용할 수 있도록 돕습니다.

---

## Anatomy

1. Leading icon
2. Label
3. Trailing icon
4. Container

---

## Variants

| 속성 | 옵션 |
|---|---|
| Variant | Solid, Outlined |
| Color | Primary, Assistive |
| Icon option | None / Leading icon with label / Trailing icon with label / Icon only |

---

## Size

너비는 자유롭게 커스터마이징하여 사용할 수 있으나 **높이는 고정**하여 사용합니다.

---

## Button Loading

파일 업로드와 같이 간단한 로딩 상태를 표현할 때 사용합니다. 로딩 중 페이지를 이탈할 경우 동작이 중단됩니다.

---

## Usage

### Icon Only
Label button과 함께 가벼운 행동을 제시할 때 사용합니다.

### Label with Icon
- **Leading icon** — Button label의 추가 설명을 도울 때 사용합니다.
- **Trailing icon** — Button의 행동 자체를 강하게 유도할 때 사용합니다.

---

## Application

### Conditional Button
입력한 값이 유효할 때 Button의 상태가 Inactive → Active로 변경됩니다.

### Toggle Button
클릭 시 Button 스타일이 변경됩니다. CTA를 유도한 이후 주목도를 떨어뜨리는 용도로 사용합니다.
- Primary → Primary 변경은 사용자에게 혼란을 줄 수 있으므로 지양합니다.

---

## Hierarchy

| 레벨 | 타입 | 용도 |
|---|---|---|
| Level 4 | Primary Solid | 화면 내에서 가장 중요한 메인 행동 유도 |
| Level 3 | Primary Outlined | 미리보기, 임시 저장 등 대체 행동 제안 |
| Level 2 | Assistive Solid | Toggle Button 보조 |
| Level 1 | Assistive Outlined | 닫기, 취소, 돌아가기 등 보조 행동 |

---

## Button Layout

### Priority > Strong
수직 배열. Label이 길거나, 메인과 서브의 비중 차이가 명확하거나, 유도 목적이 분명할 때 사용합니다.

### Priority > Neutral
수평 배열. Label이 짧고 명료하거나 Button의 역할이 비등한 경우 사용합니다. 주요 액션은 오른손 엄지 접근이 용이한 **오른쪽 하단**에 배치하는 것을 권장합니다.

---

## How to Use

### Do
- 커스텀을 통해 Destructive button을 표현할 수 있습니다.

### Don't
- Button의 높이와 둥글기를 임의로 변경하지 않습니다.

---

## Customize

### Solid Button
- `contentColor`
- `backgroundColor`

### Outlined Button
- `contentColor`
- `backgroundColor`
- `borderColor`
