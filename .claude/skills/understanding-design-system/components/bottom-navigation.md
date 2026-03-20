# Bottom Navigation

모바일 앱의 하단에 고정되어 주요 섹션 간 빠른 전환을 제공하는 네비게이션 요소입니다. 엄지로 쉽게 접근할 수 있는 위치에 3-5개의 핵심 기능을 배치하여 앱의 주요 영역으로 한 번에 이동할 수 있게 합니다.

---

## Anatomy

1. Container
2. Active tab
3. Inactive tab
4. Divider

---

## Size

| 플랫폼 | 높이 |
|---|---|
| Web | 58px 고정, 너비는 해상도에 비례하여 넓어짐 |
| iOS (iPhone 8 이하) | 49pt |
| iOS (iPhone X 이상) | 83pt (49 + Safe area 34pt) |
| Android (기본) | 64dp |
| Android (Safe area 있을 시) | 78dp (64 + 14dp) |

---

## Login

로그인 시 Avatar가 활성화되며 Fallback이 아닌 개인이 설정한 프로필 이미지가 표현됩니다.

---

## Background

### Web / iOS
Chrome 효과가 적용되어 뒷 배경의 색상 등이 자연스럽게 비칩니다.

### Android
흰 배경색이 적용되어 뒷 배경이 보이지 않습니다.

---

## Max Tab and Label

- 배치 가능한 최대 탭 개수: **5개**
- Label은 최대 **6자**를 넘지 않도록 권장합니다.

---

## How to Use

### Do
- 각 탭에서 처음 노출되는 **1Depth 페이지**에서만 위치합니다.

### Don't
- 개별적으로 Active 색상을 변경하지 않습니다.
