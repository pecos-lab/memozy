⚠️ 중요 지침 ⚠️

## 기본 원칙
- 기존 기능을 절대 제거하거나 덮어쓰지 말 것
- 요청한 부분만 최소한으로 수정할 것
- 수정 전 반드시 기존 코드 전체 구조 파악 후 작업할 것
- 확신이 없으면 수정하지 말고 먼저 질문할 것
- 작업 완료 후 기존 기능 목록과 유지 여부를 체크리스트로 보고할 것

## 코드 수정 규칙
- 파일 전체를 재작성하지 말 것
- 변경이 필요한 함수/컴포넌트만 수정할 것
- 기존 변수명, 함수명 유지할 것
- 기존 import 제거하지 말 것

## 작업 순서
1. 기존 코드 구조 파악
2. 변경 범위 최소화
3. 수정 후 기존 기능 유지 여부 확인

## Room/DB 작업 자동 검증 규칙
Room DB 또는 Migration 관련 작업 완료 후 반드시 아래 항목을 자동으로 검증할 것:
1. Migration SQL의 컬럼 목록이 Entity 클래스와 100% 일치하는지
2. `@TypeConverters` 어노테이션 누락 여부
3. `@Database` version과 실제 Migration 경로 커버리지 (모든 버전 → 최신 버전 경로 존재 여부)
4. `DatabaseModule`(Hilt)과 `getDatabase()`(직접 호출) 양쪽에 Migration 및 Callback이 동일하게 등록되어 있는지
5. schemas/ 폴더의 JSON과 실제 Migration SQL 비교

**적용 대상**: Entity 추가/수정, Migration 추가/수정, @Database version 변경, DatabaseModule 수정

## worktree 병렬 작업 규칙
- 병렬 worktree 작업 중 매일 각 브랜치에서 `git rebase origin/develop` 실행
- develop 병합은 반드시 하나씩 순서대로 진행 (동시 병합 금지)
- 병렬 작업 시작 전 브랜치 간 파일 겹침 여부 사전 확인
- 마무리 시 `/worktree-finish` 스킬 참고