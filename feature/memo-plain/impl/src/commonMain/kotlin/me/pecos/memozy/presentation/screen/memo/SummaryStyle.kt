package me.pecos.memozy.presentation.screen.memo

enum class SummaryStyle(
    val displayName: String,
    val description: String,
) {
    SIMPLE(
        displayName = "간단 요약",
        description = "키워드 + 핵심 포인트 3~5개",
    ),
    DETAILED(
        displayName = "상세 요약",
        description = "타임라인별 상세 정리",
    ),
    NOTE(
        displayName = "노트 정리",
        description = "학습/복습용 개조식",
    ),
    LANGUAGE(
        displayName = "언어 학습",
        description = "원문 표현 유지 + 모국어 해설",
    );

    val summaryMode: SummaryMode get() = when (this) {
        SIMPLE -> SummaryMode.SIMPLE
        DETAILED -> SummaryMode.DETAILED
        NOTE -> SummaryMode.DETAILED
        LANGUAGE -> SummaryMode.DETAILED
    }
}
