#!/usr/bin/env bash
# GitHub 통계 수집 스크립트
# 사용법: bash .claude/skills/github-stats/collect.sh

REPO="pecos-lab/memozy"

echo "================================================================"
echo "📊 GitHub 통계 — $(date '+%Y-%m-%d %H:%M')"
echo "================================================================"

# ── PR ───────────────────────────────────────────────────────────────
echo ""
echo "### PR"
MERGED=$(gh pr list --repo $REPO --state merged --limit 200 --json number --jq 'length')
CLOSED=$(gh pr list  --repo $REPO --state closed --limit 200 --json number --jq 'length')
OPEN=$(gh pr list    --repo $REPO --state open   --limit 200 --json number --jq 'length')
TOTAL=$((MERGED + CLOSED + OPEN))
echo "  Merged : $MERGED"
echo "  Closed : $CLOSED"
echo "  Open   : $OPEN"
echo "  합계   : $TOTAL"

echo ""
echo "  [최근 머지 PR 5개]"
gh pr list --repo $REPO --state merged --limit 5 \
  --json number,title,mergedAt \
  --jq '.[] | "  #\(.number) \(.title[:50]) [\(.mergedAt[:10])]"'

# ── ISSUE ────────────────────────────────────────────────────────────
echo ""
echo "### 이슈"
ISS_OPEN=$(gh issue list   --repo $REPO --state open   --limit 300 --json number --jq 'length')
ISS_CLOSED=$(gh issue list --repo $REPO --state closed --limit 300 --json number --jq 'length')
ISS_BUG=$(gh issue list    --repo $REPO --state open --label bug      --limit 300 --json number --jq 'length')
ISS_V100=$(gh issue list   --repo $REPO --state open --label "v1.0.0" --limit 300 --json number --jq 'length')
ISS_TOTAL=$((ISS_OPEN + ISS_CLOSED))
echo "  Open        : $ISS_OPEN"
echo "  Closed      : $ISS_CLOSED"
echo "  합계        : $ISS_TOTAL"
echo "  Open (bug)  : $ISS_BUG"
echo "  Open (v1.0.0): $ISS_V100"

# ── COMMIT ───────────────────────────────────────────────────────────
echo ""
echo "### 커밋 (develop)"
git fetch origin --quiet
TODAY=$(git log origin/develop --oneline --since="$(date '+%Y-%m-%d') 00:00" | wc -l | tr -d ' ')
WEEK=$(git log origin/develop --oneline --since="7 days ago" | wc -l | tr -d ' ')
TOTAL_COMMIT=$(git log origin/develop --oneline | wc -l | tr -d ' ')
echo "  총 커밋  : $TOTAL_COMMIT"
echo "  오늘     : $TODAY"
echo "  이번 주  : $WEEK"

echo ""
echo "  [일별 커밋 (최근 14일)]"
git log origin/develop --format="%ad" --date=format:"%m-%d" --since="14 days ago" \
  | sort | uniq -c | awk '{printf "  %s  %s건\n", $2, $1}'

# ── WORKFLOW ─────────────────────────────────────────────────────────
echo ""
echo "### 워크플로우 (최근 5회)"
gh run list --repo $REPO --limit 5 \
  --json name,conclusion,headBranch,createdAt \
  --jq '.[] | "  \(if .conclusion=="success" then "✅" else "❌" end) \(.name) | \(.headBranch) | \(.createdAt[:10])"'

# ── PROJECT BOARD ────────────────────────────────────────────────────
echo ""
echo "### Project Board"
gh api graphql -f query='{
  user(login:"pecos-lab"){
    projectV2(number:1){
      items(first:100){
        nodes{
          fieldValues(first:10){
            nodes{
              ...on ProjectV2ItemFieldSingleSelectValue{
                name
                field{...on ProjectV2SingleSelectField{name}}
              }
            }
          }
        }
      }
    }
  }
}' --jq '[.data.user.projectV2.items.nodes[].fieldValues.nodes[] | select(.field.name=="Status")] | group_by(.name) | map("  \(.[0].name | if . == "Todo" then "Todo       " elif . == "In progress" then "In progress" else "Done       " end) : \(length)") | .[]' 2>/dev/null \
  || echo "  Project Board 조회 실패"

echo ""
echo "================================================================"
