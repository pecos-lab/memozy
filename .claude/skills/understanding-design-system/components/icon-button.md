# Icon Button

텍스트 라벨 없이 아이콘만으로 특정 액션을 수행하는 버튼 요소입니다. 공간이 제한적이거나 사용자가 즉시 이해할 수 있는 표준적인 기능을 간결하게 제공할 때 사용합니다.

---

## Anatomy

1. Icon
2. Container

---

## Variants

| 속성 | 옵션 |
|---|---|
| Variant | Normal, Solid, Outlined |
| Background | False, True (Alternative) |

---

## Size

### Normal / Background
24px 기준으로 **4배수**로 크기를 줄이거나 늘려 커스터마이징합니다.

### Solid / Outlined
기본 제공 사이즈가 있으며, 아이콘 크기와 패딩을 조절하여 커스터마이징 가능합니다. 크기와 패딩 커스터마이징은 **2배수 간격**으로 제한합니다.

---

## Usage

### Background - Overlay with Image
배경이 있을 시 사용하는 배경 전용 버튼입니다.
- **Web / iOS** — Material 효과로 배경 색상이 자연스럽게 어우러집니다.
- **Android** — Alternative 옵션 사용을 권장합니다.

### Floating Button
화면의 특정 영역에 고정되어 중요한 행동을 강하게 유도하는 용도로 사용합니다.

### Conditional Button
화면의 특정 영역에 고정되어 중요한 행동을 강하게 유도하는 용도로 사용합니다.

### Gap
Normal은 인터랙션 영역을 포함하지 않기 때문에 아이콘 버튼을 나란히 여러 개 배치할 때 **최소 12px 이상**의 Gap을 유지합니다.

### Scale with Icon Padding
Solid, Outlined 커스터마이징 시 Padding을 함께 조절하면 아이콘 크기도 변경됩니다.
- Padding을 따로 조절하지 않으면 아이콘은 기본 사이즈를 유지합니다.
- Icon layer를 Fill로 변경하면 Padding 조절에 따라 아이콘 크기도 함께 바뀝니다.

---

## How to Use

### Do
- Solid는 Background 색상을 변경하여 사용할 수 있습니다.

### Don't
- Action area에 배치하여 사용하지 않습니다.

---

## Customize

### Normal
- `size`
- `iconColor`

### Background
- `size`
- `iconColor`

### Solid
- `padding`
- `backgroundColor`
- `iconColor`

### Outlined
- `padding`
- `backgroundColor`
- `borderColor`
- `iconColor`
