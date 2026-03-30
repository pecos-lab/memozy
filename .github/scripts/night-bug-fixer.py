"""
Night Bug Fixer
- 매일 새벽 GitHub의 bug 라벨 이슈 1개를 골라 Claude가 수정 후 PR 생성
"""

import os
import json
import subprocess
import sys
import anthropic


def run(cmd: str, check: bool = False) -> str:
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if check and result.returncode != 0:
        print(f"[ERROR] {cmd}\n{result.stderr}", file=sys.stderr)
        sys.exit(1)
    return result.stdout.strip()


def find_relevant_files(issue_title: str, issue_body: str) -> list[str]:
    """이슈 제목/내용에서 키워드를 뽑아 관련 .kt 파일 검색"""
    # 괄호·특수문자 제거 후 단어 추출
    text = (issue_title + " " + issue_body).replace("[", " ").replace("]", " ")
    keywords = [w for w in text.split() if len(w) > 3][:5]

    found = set()
    for kw in keywords:
        result = subprocess.run(
            ["grep", "-rl", kw, "--include=*.kt", "."],
            capture_output=True, text=True
        )
        for line in result.stdout.splitlines()[:3]:
            found.add(line.strip())

    # 홈화면·메모카드는 자주 수정되므로 기본 포함
    defaults = [
        "feature/home/impl/src/main/java/me/pecos/memozy/presentation/screen/home/HomeScreen.kt",
        "feature/home/impl/src/main/java/me/pecos/memozy/presentation/screen/home/components/MemoCard.kt",
    ]
    for f in defaults:
        if os.path.exists(f):
            found.add(f)

    return list(found)[:5]


def build_file_context(file_paths: list[str]) -> str:
    context = ""
    for path in file_paths:
        try:
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()[:4000]
            context += f"\n\n### {path}\n```kotlin\n{content}\n```"
        except Exception:
            pass
    return context


def ask_claude(client: anthropic.Anthropic, issue_number: int, issue_title: str,
               issue_body: str, file_context: str) -> dict:
    prompt = f"""당신은 Android Kotlin 개발자입니다. 아래 버그 이슈를 분석하고 수정해주세요.

## 이슈 #{issue_number}: {issue_title}
{issue_body or "(내용 없음)"}

## 관련 소스 파일
{file_context or "(파일 없음)"}

## 지시
1. 버그 원인을 파악하세요.
2. 정확히 어떤 파일의 어떤 코드를 수정해야 하는지 결정하세요.
3. **반드시** 아래 JSON 형식만 출력하세요 (다른 텍스트 없이):

수정 가능한 경우:
{{
  "can_fix": true,
  "analysis": "버그 원인 한 줄 설명",
  "file_path": "수정할 파일의 상대 경로 (예: feature/home/impl/.../HomeScreen.kt)",
  "old_code": "수정 전 코드 (파일에 실제로 존재하는 코드 그대로)",
  "new_code": "수정 후 코드"
}}

수정 불가능한 경우 (정보 부족, 복잡한 구조 변경 필요 등):
{{
  "can_fix": false,
  "analysis": "수정할 수 없는 이유"
}}"""

    response = client.messages.create(
        model="claude-sonnet-4-6",
        max_tokens=4096,
        messages=[{"role": "user", "content": prompt}],
    )

    text = response.content[0].text.strip()

    # 코드 블록 제거
    if "```json" in text:
        text = text.split("```json")[1].split("```")[0].strip()
    elif "```" in text:
        text = text.split("```")[1].split("```")[0].strip()

    return json.loads(text)


def sanitize_branch_name(title: str) -> str:
    import re
    slug = re.sub(r"[^\w가-힣]", "-", title.lower())[:40].strip("-")
    return slug


def main():
    repo = os.environ["REPO"]
    client = anthropic.Anthropic(api_key=os.environ["ANTHROPIC_API_KEY"])

    # ── 1. 오픈 bug 이슈 1개 가져오기 ──────────────────────────────────────
    raw = run(
        f'gh issue list --repo {repo} --label bug --state open --limit 1 '
        f'--json number,title,body'
    )
    issues = json.loads(raw)
    if not issues:
        print("처리할 bug 이슈 없음")
        return

    issue = issues[0]
    issue_number: int = issue["number"]
    issue_title: str = issue["title"]
    issue_body: str = issue.get("body") or ""
    print(f"대상 이슈: #{issue_number} — {issue_title}")

    # ── 2. 이미 해당 이슈 PR이 있으면 스킵 ────────────────────────────────
    existing = run(
        f'gh pr list --repo {repo} --state open '
        f'--search "fix: #{issue_number}" --json number'
    )
    if json.loads(existing):
        print(f"이슈 #{issue_number}에 대한 PR이 이미 존재합니다. 스킵.")
        return

    # ── 3. 관련 파일 수집 ──────────────────────────────────────────────────
    relevant = find_relevant_files(issue_title, issue_body)
    file_context = build_file_context(relevant)
    print(f"관련 파일: {relevant}")

    # ── 4. Claude에게 수정 요청 ────────────────────────────────────────────
    result = ask_claude(client, issue_number, issue_title, issue_body, file_context)

    if not result.get("can_fix"):
        print(f"Claude 판단: 수정 불가 — {result.get('analysis')}")
        return

    file_path = result["file_path"]
    old_code  = result["old_code"]
    new_code  = result["new_code"]
    analysis  = result["analysis"]
    print(f"분석: {analysis}")
    print(f"수정 파일: {file_path}")

    # ── 5. 파일 패치 ───────────────────────────────────────────────────────
    # file_path가 레포 루트 밖을 가리키는지 검증
    repo_root = os.path.abspath(".")
    abs_path  = os.path.abspath(file_path)
    if not abs_path.startswith(repo_root + os.sep):
        print(f"유효하지 않은 경로: {file_path}")
        return

    if not os.path.exists(abs_path):
        print(f"파일 없음: {file_path}")
        return

    file_path = abs_path  # 이후 절대경로 사용

    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    if old_code not in content:
        print("교체할 코드를 파일에서 찾지 못했습니다.")
        return

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content.replace(old_code, new_code, 1))

    print(f"{file_path} 수정 완료")

    # ── 6. 브랜치 생성 + 커밋 + 푸시 ─────────────────────────────────────
    slug = sanitize_branch_name(issue_title)
    branch = f"fix/issue-{issue_number}-{slug}"

    run("git config user.email 'github-actions[bot]@users.noreply.github.com'")
    run("git config user.name 'github-actions[bot]'")
    run(f"git checkout -b {branch}", check=True)
    run(f"git add {file_path}", check=True)

    commit_msg = (
        f"fix: #{issue_number} {issue_title}\n\n"
        f"{analysis}\n\n"
        f"Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
    )
    with open("/tmp/commit_msg.txt", "w") as f:
        f.write(commit_msg)
    run("git commit -F /tmp/commit_msg.txt", check=True)
    run(f"git push origin {branch}", check=True)

    # ── 7. PR 생성 ─────────────────────────────────────────────────────────
    pr_body = (
        f"## 🤖 자동 버그 수정\n\n"
        f"**관련 이슈:** #{issue_number} — {issue_title}\n\n"
        f"**원인 분석:** {analysis}\n\n"
        f"**수정 파일:** `{file_path}`\n\n"
        f"---\n"
        f"> ⚠️ 이 PR은 AI가 자동 생성했습니다. 머지 전 반드시 코드를 검토해주세요.\n"
        f"> 🤖 Generated by Night Bug Fixer (Claude Sonnet 4.6)"
    )
    with open("/tmp/pr_body.md", "w") as f:
        f.write(pr_body)

    run(
        f'gh pr create --repo {repo} '
        f'--title "fix: #{issue_number} {issue_title[:60]}" '
        f'--body-file /tmp/pr_body.md '
        f'--base develop',
        check=True,
    )
    print(f"PR 생성 완료! 이슈 #{issue_number}")


if __name__ == "__main__":
    main()
