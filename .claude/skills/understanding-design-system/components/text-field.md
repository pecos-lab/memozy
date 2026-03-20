# Text Field

일정 높이의 필드 안에 한 줄 이하의 값을 입력할 수 있는 컴포넌트로 Heading, Placeholder, Description 등의 요소를 활용하여 사용자가 입력할 내용의 형식을 보다 명확하게 안내합니다.

---

## Anatomy

1. Heading
2. Placeholder
3. Description
4. Leading icon
5. Field
6. Required badge
7. Trailing contents
8. Trailing button

---

## Variants

| 속성 | 옵션 |
|---|---|
| Heading | 텍스트 입력 (필수) |
| Placeholder | 텍스트 입력 |
| Description | 텍스트 입력 |
| Leading icon | None, Icon |
| Trailing button | None, Button |
| Trailing contents | None / Custom / Badge / Text / Icon / Timer |

---

## Input Mask

### Call Number
휴대전화 번호 입력 시 4자 단위로 하이픈(-)이 포함된 형식을 권장합니다. 컴포넌트 자체 지원이 아닌 프로젝트 담당자 간 커뮤니케이션으로 적용합니다.

### Card Number
카드번호 16자 입력 시 4자 단위로 하이픈(-)이 포함된 형식을 권장합니다. 컴포넌트 자체 지원이 아닌 프로젝트 담당자 간 커뮤니케이션으로 적용합니다.

### Date
날짜를 직접 입력할 시 `YYYY. MM. DD` 양식 사용을 권장하며, 가급적 Text input 기반의 Date picker 컴포넌트 사용을 권장합니다.

---

## Gap

필드 간의 간격은 기본적으로 Description 활성 여부와 관계없이 동일한 간격을 유지합니다. 단, 레이아웃이 제한적인 상황일 경우 필드가 고정으로 위치하여 Description만큼 간격이 줄어들 수 있습니다.

---

## Validation Check

값을 입력하고 확인 버튼을 눌러 유효성 검사를 실시하도록 설계하는 것을 권장합니다.

---

## Usage

### Password
패스워드 입력 시 Hidden/Visible 옵션 제공을 권장하며, 기본값은 **Hidden** 상태입니다.

### Certifications
Trailing button을 활성화하여 사용합니다.
- 인증번호 입력 필드가 노출될 때 포커스가 이동합니다.
- 발송, 재발송과 같이 행동을 유도하는 버튼은 **Normal** 사용
- '확인'과 같이 행동을 종료하는 버튼은 **Assistive** 권장
- 일정 시간 이후 필드의 버튼과 타이머가 비활성화됩니다.

### Unit
Trailing content의 `Text`를 활용하여 특정 고정 값에 대한 Suffix 값을 둘 수 있습니다.

---

## How to Use

### Do
- 실패 사유를 명확하게 안내하여 사용자에게 명확한 행동을 유도합니다.

### Don't
- 버튼명을 6자 이상으로 길게 적용하지 않습니다.
