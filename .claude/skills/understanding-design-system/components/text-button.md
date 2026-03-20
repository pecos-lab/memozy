# Text Button

배경이나 테두리 없이 텍스트 라벨만으로 구성된 버튼으로 사용자의 행동을 유도합니다. 시각적 소음을 줄이고 싶거나, 우선순위가 낮은 보조 액션을 제공할 때 주로 사용합니다.

---

## Anatomy

1. Leading Icon
2. Label
3. Trailing Icon

---

## Variants

| 속성 | 옵션 |
|---|---|
| Color | Primary, Assistive |
| Leading icon | False, True |
| Trailing icon | False, True |

---

## Size

너비는 자유롭게 커스터마이징할 수 있으나 **높이는 사이즈별로 지정된 값을 고정**하여 사용합니다.

---

## Button Loading

파일 업로드와 같이 간단한 로딩 상태를 표현할 때 사용합니다. 로딩 중 페이지를 이탈할 경우 동작이 중단됩니다.

---

## Usage

### With Text
유도 문장과 함께 행동을 이끌어 낼 때 사용합니다.

### In Component
Text area와 같이 컴포넌트 내부에 Text button이 위치할 경우 인터랙션 영역을 고려하여 버튼을 배치합니다.

### Color Structure
| 플랫폼 | 동작 |
|---|---|
| Web | Icon, Label, Interaction이 기본적으로 동일한 컬러를 유지하며, 각 요소별 개별 컬러 지정이 가능합니다. |
| iOS | Icon, Label, Interaction이 항상 동일한 컬러를 유지합니다. |
| Android | Icon과 Label 각각 컬러 지정이 가능합니다. Primary 타입은 Interaction 색상이 Text 색상을 따라가며, Assistive 타입은 Text 색상에 관계없이 Interaction 색상이 Assistive로 고정됩니다. |

### Icon
- **Leading icon** — Label을 보조 설명하는 용도
- **Trailing icon** — 버튼의 직접적인 행동을 강조하는 용도

---

## How to Use

### Do
- Icon 요소를 활용하여 Button과 Text를 구분할 수 있도록 합니다.

### Don't
- 버튼을 좌우로 나란히 배치할 시 Text button과 일반 Button 타입을 함께 사용할 수 없습니다.

---

## Customize

### Primary
- `contentColor`
- `typography`

### Assistive
- `contentColor`
- `typography`
