# Text Area

필드 내부에 2줄 이상의 텍스트를 입력할 때 사용하며 Heading, Placeholder, Description 등의 요소를 활용하여 사용자가 입력할 내용의 형식을 보다 명확하게 안내합니다.

---

## Anatomy

1. Heading
2. Placeholder
3. Leading content
4. Description
5. Required badge
6. Field
7. Trailing content

---

## Variants

| 속성 | 옵션 |
|---|---|
| Heading | 텍스트 입력 (필수) |
| Placeholder | 텍스트 입력 |
| Resize | Normal, Limited, Fixed |
| Leading contents | None / Character counter / Button / Icon button / Icon / Badge / Chip |
| Trailing contents | None / Character counter / Button / Icon button / Icon / Badge / Chip |

---

## Resize

### Normal
필드 내부의 텍스트 양과 비례하여 높이가 제한 없이 아래로 늘어납니다.

### Limited
기본 높이에서 텍스트 양에 비례하여 일정 높이까지 늘어나다가 고정됩니다.

| 플랫폼 | 기본 최대 높이 |
|---|---|
| iOS | 102px |
| Android | 3줄 |
| Web | 12줄 |

프로젝트 정책에 따라 최대 높이를 직접 지정할 수 있습니다.

### Fixed
텍스트 양에 관계없이 필드의 기본/최대 높이를 자유롭게 고정합니다. Text field와의 구분을 위해 최소 권장 높이를 지킵니다.

| 플랫폼 | 최소 권장 높이 |
|---|---|
| iOS | 76px |
| Android | 2줄 |
| Web | 2줄 |

---

## Bottom

### Leading Contents
Character counter, Text button, Icon button, Icon, Content badge, Filter button이 자리할 수 있습니다. 고정된 요소들만 들어갈 수 있으며 다른 요소가 필요할 경우 커스터마이징합니다. 메인 영역 이외에 2개의 Extra가 존재하며 간격을 자유롭게 조절할 수 있습니다.

### Trailing Contents
Bottom 영역은 비활성할 수 있으며, Character counter, Text button (Primary/Assistive), Icon button (Normal/Solid), Icon, Content badge, Filter button이 위치합니다. 다른 요소가 필요할 경우 커스터마이징합니다. 메인 영역 이외에 2개의 Extra가 존재하며 요소 간 간격을 **2배수**로 자유롭게 조절할 수 있습니다.

---

## Validation Icon

입력한 내용이 유효하지 않을 때 노출됩니다. 단, Trailing contents가 있는 경우 제외합니다.

---

## Keyboard Sticky

키보드와 함께 사용하는 타입으로 필드의 최대 높이는 기본 **5줄 이하**를 권장합니다.

---

## How to Use

### Do
- Bottom의 Contents는 인터랙션 영역을 고려하여 간격을 조절합니다.

### Don't
- 모바일에서 높이가 지속적으로 늘어나는 `Resize=Normal` 타입을 사용하여 사용성 혼란을 주지 않습니다.
