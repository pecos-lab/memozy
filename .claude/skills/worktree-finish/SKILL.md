---
name: worktree-finish
description: worktree 병렬 작업 마무리 후 develop 브랜치에 순서대로 병합할 때 사용. "worktree 마무리", "develop에 올리기", "브랜치 합치기" 요청에 활성화됨.
version: 1.0.0
---

# Worktree 마무리 & Develop 병합 스킬

병렬 worktree 작업을 develop에 스무스하게 병합하는 절차.

---

## 병합 전 체크리스트

각 worktree 브랜치에서 실행:

```bash
# 1. 현재 작업 완료 여부 확인
git status

# 2. develop 최신화
git fetch origin

# 3. 현재 브랜치를 develop 기준으로 rebase
git rebase origin/develop
```

충돌 발생 시:
```bash
# 충돌 파일 수동 해결 후
git add <파일>
git rebase --continue
```

---

## 병합 순서 결정

병합 순서는 **의존성 기준**으로 정함:

1. 다른 브랜치가 의존하는 기반 변경 먼저
2. 독립적인 기능끼리는 파일 겹침이 적은 순서로
3. 겹치는 파일이 있으면 한 브랜치를 먼저 merge → 나머지는 그 위에 rebase

---

## 순서대로 병합하기

```bash
# Step 1: 가장 독립적인 브랜치부터
git checkout branch-A
git rebase origin/develop
# PR 올리고 develop에 merge

# Step 2: develop 갱신 후 다음 브랜치
git checkout branch-B
git fetch origin
git rebase origin/develop   # 이제 A가 포함된 develop 기준
# PR 올리고 develop에 merge

# Step 3: 반복
```

---

## 주의사항

- **절대 동시에 여러 브랜치를 develop에 merge하지 말 것**
- merge 전 항상 `git fetch origin` → `git rebase origin/develop` 순서
- rebase 후 force push 필요: `git push origin <branch> --force-with-lease`
- 같은 파일을 건드린 브랜치가 있으면 merge 순서를 팀과 합의 후 진행
