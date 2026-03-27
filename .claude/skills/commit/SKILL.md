---
name: commit
description: 변경사항을 git commit할 때 사용. "커밋해줘", "commit", "저장해줘" 요청에 활성화됨. git user.name이 pecos인지 확인하고 staged 파일 목록을 보여준 후 확인받아 커밋.
version: 1.0.0
---

# 커밋 스킬

## 실행 순서

### 1. git user 확인

```bash
git config user.name
git config user.email
```

- `user.name`이 `pecos`가 아니면 **반드시 수정 후 진행**:
  ```bash
  git config user.name "pecos"
  ```

### 2. 변경사항 파악

```bash
git status
git diff --staged
```

### 3. 사용자에게 staged 파일 목록 보여주고 확인받기

커밋 전 반드시 staged 파일 목록을 사용자에게 보여주고 OK 받을 것.
확인 없이 커밋하지 말 것.

### 4. 커밋 메시지 작성 규칙

- `Add:` — 추가 관련 (새 기능, 파일, 의존성 등)
- `Update:` — 모든 수정 (기능 개선, 리팩터링, 설정 변경 등)
- `Fix:` — 버그 대응
- `Move:` — 이동 관련 (파일/코드 이동, 구조 변경)
- `Remove:` — 제거 관련 (파일, 기능, 코드 삭제)

이슈 번호가 있으면 포함: `Fix: #143 메모 추가 화면 이전 내용 잔존 수정`

### 5. 커밋 실행

```bash
git commit -m "$(cat <<'EOF'
<타입>: <#이슈번호> <설명>

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"
```

## 주의사항

- `git add -A` 또는 `git add .` 사용 금지 → 파일명 명시해서 add
- `.env`, 키스토어, 시크릿 파일 절대 포함 금지
- 커밋 후 `git status`로 성공 여부 확인
- 푸시는 사용자가 명시적으로 요청할 때만
