# Color

원티드의 컬러시스템은 시각적 일관성을 유지하고 효율적인 디자인 작업을 돕습니다. 시멘틱 컬러를 통해 상황에 맞는 적절한 색상을 일관되게 사용할 수 있습니다.

---

## 구조

컬러 시스템은 **Semantic**과 **Atomic** 두 계층으로 구성됩니다.

- **Semantic** — 사용 목적과 맥락에 따라 정의된 색상
- **Atomic** — 시스템의 원자 단위 색상값

---

## Semantic 컬러

### Primary
화면 내에서 가장 중요한 요소를 표현할 때 사용합니다. Normal, Strong, Heavy와 같이 3가지 타입이 존재하며 상황에 맞게 적절히 선택하여 사용합니다.

| Token | 용도 |
|---|---|
| Primary / Normal | 기본 강조 |
| Primary / Strong | 더 강한 강조 |
| Primary / Heavy | 가장 강한 강조 |

---

### Label
텍스트 등 레이블 요소에 사용합니다.

| Token | 용도 |
|---|---|
| Label / Normal | 기본 텍스트 |
| Label / Strong | 강조 텍스트 |
| Label / Neutral | 중립 텍스트 |
| Label / Alternative | 보조 텍스트 |
| Label / Assistive | 도움 텍스트 |
| Label / Disable | 비활성 텍스트 |

---

### Fill
어떠한 요소에서 배경 색상이 필요한 경우 사용하는 투명도가 포함된 색상입니다. 정보가 있는 패널과 배경을 구분해야할 때 패널에 색상을 채워 사용합니다.

| Token | 용도 |
|---|---|
| Fill / Normal | 기본 채우기 |
| Fill / Strong | 강조 채우기 |
| Fill / Alternative | 대안 채우기 |

---

### Line - Normal
Divider, Border 등 요소 간의 구분이 필요한 경우 사용합니다. 투명 값이 포함된 색상으로 라인을 중첩하여 사용하지 않도록 유의합니다.

| Token | 용도 |
|---|---|
| Line / Normal | 기본 구분선 |
| Line / Neutral | 중립 구분선 |
| Line / Alternative | 보조 구분선 |

### Line - Solid
Border와 Divider를 중첩하여 사용할 때 중첩을 방지하고자 사용합니다.

| Token | 용도 |
|---|---|
| Line Solid / Normal | 기본 |
| Line Solid / Neutral | 중립 |
| Line Solid / Alternative | 보조 |

---

### Background - Normal
일반적인 화면의 배경 색상으로 활용합니다. 카드 UI와 같이 요소와 배경의 구분을 분명히 둬야할 때 Alternative를 사용하여 대비를 줍니다.

| Token | 용도 |
|---|---|
| Background Normal / Normal | 기본 배경 |
| Background Normal / Alternative | 카드 등 구분이 필요한 배경 |

### Background - Elevated
모달과 같이 층위가 있는 페이지에 사용하는 배경 색상으로 다크 모드에서 Background-Normal과의 색상 차이가 있습니다.

| Token | 용도 |
|---|---|
| Background Elevated / Normal | 기본 |
| Background Elevated / Alternative | 보조 |

### Background - Transparent
Chrome 효과를 적용할 때 사용하는 투명도가 포함된 배경 색상입니다. Android에서만 Alternative를 사용합니다.

| Token | 용도 |
|---|---|
| Background Transparent / Normal | 기본 |
| Background Transparent / Alternative | Android 전용 |

---

### Static
Light, Dark 테마에 상관없이 고정된 고유 색으로 테마가 변경되더라도 색상을 유지합니다.

| Token | 용도 |
|---|---|
| Static / White | 항상 흰색 |
| Static / Black | 항상 검정색 |

---

### Inverse
테마의 대비가 반대인 요소에 적용하는 색상입니다. 주로 Tooltip과 같이 배경과 확실히 구분되어 정보를 인지시키는 용도로 사용합니다.

| Token | 용도 |
|---|---|
| Inverse / Primary | 반전 Primary |
| Inverse / Background | 반전 배경 |
| Inverse / Label | 반전 텍스트 |

---

### Interaction
상호작용 요소에서 활성화 가능하거나 상호작용이 불가능한 상태를 표현할 때 사용합니다.

| Token | 용도 |
|---|---|
| Interaction / Inactive | 비활성 상태 |
| Interaction / Disable | 사용 불가 상태 |

---

### Status
요소의 상태를 표현해야할 때 사용합니다.

| Token | 용도 |
|---|---|
| Status / Positive | 긍정/성공 상태 |
| Status / Cautionary | 주의 상태 |
| Status / Negative | 부정/오류 상태 |

---

### Accent - Foreground
시각적 대비를 명확하게 유지하기 위해 앞쪽 요소에 사용하는 색상입니다.

`Red` `Red Orange` `Orange` `Lime` `Green` `Cyan` `Light Blue` `Blue` `Violet` `Purple` `Pink`

### Accent - Background
시각적 대비를 명확하게 유지하기 위해 배경과 같은 뒤쪽 요소에 사용하는 색상입니다.

`Red Orange` `Lime` `Cyan` `Light Blue` `Violet` `Purple` `Pink`

---

### Material
모달과 같이 층위가 생길 때 배경과의 구분을 위해 어둡게 표시해야할 때 사용합니다.
