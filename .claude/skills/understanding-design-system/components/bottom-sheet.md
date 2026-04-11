# Bottom Sheet

사용자가 현재 맥락과 밀접한 관련이 있는 범위가 정해진 작업을 수행할 수 있도록 돕는 요소. 모바일 화면 하단에 오버레이되어 추가적인 사항이나 액션을 표시한다.

---

## Anatomy

1. **Handle** — 드래그 핸들
2. **Content area** — 콘텐츠 영역
3. **Action area** — 액션 버튼 영역
4. **Scrim** — 딤 배경
5. **Container** — 컨테이너

---

## Handle

- `Handle=True` (기본): 바텀시트를 닫거나 축소/확장
- `Handle=False`: 특정 버튼으로만 닫기를 제한할 때 Handle 제거

---

## Max Height

- **Web**: 브라우저 화면 상단에서 40px 아래까지 확장 가능
- **App**: 상태바(Status bar)를 제외한 화면에서 상단 10px 아래까지 확장 가능

---

## Resize 타입

| 타입 | 설명 | 사용 시점 |
|------|------|----------|
| **Hug** | 콘텐츠 영역 높이만큼 컨테이너 높이 조정 | 여백 없이 콘텐츠만 명확하게 전달할 때 |
| **Flexible** | 최대 높이 50%로 시작, 스크롤/핸들로 최대까지 확장 | 리스트 탐색 시 |
| **Fill** | 콘텐츠 높이와 관계없이 항상 화면 전체 채움 | 긴 콘텐츠 (상세 정책, 줄 글 등) |
| **Fixed** | 8배수 단위로 높이 직접 지정 | 탭 등으로 화면 분할 시 높이 고정 필요할 때 |

---

## Action Area

| 타입 | 설명 | 사용 시점 |
|------|------|----------|
| **Strong** | 버튼 상하 배치 | Main action을 의도적으로 강조할 때 |
| **Neutral** | 버튼 양옆 배치 | Main/Sub action 선택을 대등하게 제공할 때 |
| **Cancel** | 취소/확인 단순 배치 | 단순 Bottomsheet 종료 용도 |

---

## Usage 패턴

| 패턴 | 설명 |
|------|------|
| **Full modal** | 편집 영역이 광범위하거나 Depth가 생길 시 Full modal로 확장성 있게 대응 |
| **Picker** | 모바일에서 Date/Time picker를 바텀시트로 호출 |
| **Menu** | 모바일에서 Menu 호출 시 Bottom sheet 활용 |
| **Action sheet** | App은 각 플랫폼 Action sheet 사용, Web은 Bottom sheet로 구성 |

---

## 가이드라인

- **Do**: Bottom sheet에서 높이 변화 없는 간단한 편집 수행 가능
- **Don't**: Bottom sheet에서 이중으로 모달을 띄우는 것 지양
