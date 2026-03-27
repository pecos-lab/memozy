---
name: github-stats
description: GitHub 프로젝트 통계를 일관된 형식으로 수집하고 출력. "통계 뽑아줘", "깃허브 현황", "PR/이슈 현황", "stats" 요청에 활성화됨.
version: 1.0.0
---

# GitHub 통계 수집 스킬

REPO 변수를 `pecos-lab/memozy`로 고정하고 아래 순서대로 수집 후 출력.

---

## 실행 방법

```bash
bash .claude/skills/github-stats/collect.sh
```

결과를 그대로 읽어서 출력 형식에 맞게 정리할 것.

---

## 수집 항목 및 명령어 (수동 실행 시 참고)

### 1. PR 현황

```bash
REPO=pecos-lab/memozy

# 전체 PR 수 (상태별)
gh pr list --repo $REPO --state open  --json number --jq 'length'
gh pr list --repo $REPO --state merged --json number --jq 'length'
gh pr list --repo $REPO --state closed --json number --jq 'length'

# 최근 머지된 PR 5개
gh pr list --repo $REPO --state merged --limit 5 \
  --json number,title,mergedAt \
  --jq '.[] | "#\(.number) \(.title) [\(.mergedAt[:10])]"'
```

### 2. 이슈 현황

```bash
# 상태별 이슈 수
gh issue list --repo $REPO --state open   --json number --jq 'length'
gh issue list --repo $REPO --state closed --json number --jq 'length'

# 라벨별 오픈 이슈 수 (bug / v1.0.0 등)
gh issue list --repo $REPO --state open --label "bug"   --json number --jq '"bug: \(length)"'
gh issue list --repo $REPO --state open --label "v1.0.0" --json number --jq '"v1.0.0: \(length)"'

# 오픈 이슈 목록 (최근 10개)
gh issue list --repo $REPO --state open --limit 10 \
  --json number,title,labels \
  --jq '.[] | "#\(.number) \(.title)"'
```

### 3. 커밋 현황 (develop 브랜치)

```bash
# 오늘 날짜 커밋 수
git log origin/develop --oneline --since="00:00" | wc -l

# 이번 주 커밋 수
git log origin/develop --oneline --since="7 days ago" | wc -l

# 최근 커밋 5개
git log origin/develop --oneline -5
```

### 4. 워크플로우 현황

```bash
# 최근 5회 워크플로우 실행 결과
gh run list --repo $REPO --limit 5 \
  --json databaseId,name,conclusion,headBranch,createdAt \
  --jq '.[] | "\(.conclusion) | \(.name) | \(.headBranch) | \(.createdAt[:10])"'
```

### 5. GitHub Project 현황

```bash
# 프로젝트 아이템 상태별 카운트 (GraphQL)
gh api graphql -f query='
{
  user(login: "pecos-lab") {
    projectV2(number: 1) {
      items(first: 200) {
        nodes {
          fieldValues(first: 10) {
            nodes {
              ... on ProjectV2ItemFieldSingleSelectValue {
                name
                field { ... on ProjectV2SingleSelectField { name } }
              }
            }
          }
        }
      }
    }
  }
}' | python3 -c "
import json,sys
from collections import Counter
d=json.load(sys.stdin)
items=d['data']['user']['projectV2']['items']['nodes']
c=Counter()
for item in items:
    for fv in item['fieldValues']['nodes']:
        if fv.get('field',{}).get('name')=='Status':
            c[fv['name']]+=1
for k,v in sorted(c.items()): print(f'{k}: {v}')
"
```

---

## 출력 형식

수집 후 아래 형식으로 마크다운 테이블 정리:

```
## 📊 GitHub 통계 — {오늘 날짜}

### PR
| 상태 | 수 |
|------|----|
| Open | N |
| Merged | N |
| Closed | N |

### 이슈
| 상태 | 수 |
|------|----|
| Open | N |
| Closed | N |
| Open (bug) | N |
| Open (v1.0.0) | N |

### 커밋 (develop)
- 오늘: N건
- 이번 주: N건

### 워크플로우 (최근 5회)
| 결과 | 워크플로우 | 브랜치 | 날짜 |
|------|-----------|--------|------|
| ... | ... | ... | ... |

### Project Board
| 상태 | 수 |
|------|----|
| Todo | N |
| In progress | N |
| Done | N |
```

---

## 주의사항

- python3 파이프 사용 시 `/tmp` 파일 경유 방식 사용 (파이프 오류 방지)
- `git log` 명령은 `git fetch origin` 선행 후 실행
- 날짜 필터는 로컬 타임존 기준
