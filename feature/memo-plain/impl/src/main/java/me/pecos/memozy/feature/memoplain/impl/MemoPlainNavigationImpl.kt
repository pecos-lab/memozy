package me.pecos.memozy.feature.memoplain.impl

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.pecos.memozy.data.datasource.local.YoutubeSummaryDao
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.entity.YoutubeSummary
import me.pecos.memozy.data.datasource.remote.ai.AIApiService
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionService
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.model.MemoFormat
import me.pecos.memozy.feature.memoplain.api.MemoPlainNavigation
import me.pecos.memozy.feature.memoplain.api.MemoPlainRoute
import me.pecos.memozy.presentation.screen.home.model.MemoFormatUi
import me.pecos.memozy.presentation.screen.home.model.MemoUiState
import me.pecos.memozy.presentation.screen.memo.MemoScreen
import me.pecos.memozy.presentation.theme.LocalAppColors
import javax.inject.Inject

class MemoPlainNavigationImpl @Inject constructor(
    private val repository: MemoRepository,
    private val aiApiService: AIApiService,
    private val youtubeSummaryDao: YoutubeSummaryDao,
    private val captionService: YouTubeCaptionService
) : MemoPlainNavigation {

    companion object {
        private const val PREF_NAME = "memozy_ai_usage"
        private const val KEY_SUMMARY_COUNT = "youtube_summary_count"
        private const val FREE_SUMMARY_LIMIT = 100

        private val YOUTUBE_REGEX = Regex(
            """(?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)[\w-]+"""
        )

        private val VIDEO_ID_REGEX = Regex(
            """(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)([\w-]+)"""
        )

        fun extractVideoId(url: String): String? =
            VIDEO_ID_REGEX.find(url)?.groupValues?.get(1)

        private val SUMMARY_PROMPT = """
            |이 유튜브 영상을 한국어로 상세하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
            |
            |📋 한줄 요약
            |영상의 핵심을 한 문장으로 요약
            |
            |📌 핵심 키워드
            |#키워드1 #키워드2 #키워드3 #키워드4 #키워드5
            |
            |⏰ 타임라인별 상세 요약
            |각 주제/구간별로 나눠서 아래처럼 정리해줘:
            |
            |[00:00] 섹션 제목
            |- 상세 설명 (2~3문장)
            |- 중요한 내용이나 인사이트
            |
            |[다음 구간] 섹션 제목
            |- 상세 설명
            |- 중요한 내용이나 인사이트
            |
            |(영상 흐름에 따라 모든 구간을 빠짐없이 정리)
            |
            |💡 핵심 인사이트
            |- 영상에서 얻을 수 있는 주요 인사이트를 정리
        """.trimMargin()
    }

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onNavigateToHome: () -> Unit,
        onBack: () -> Unit
    ) {
        navGraphBuilder.composable(MemoPlainRoute.MEMO) { backStackEntry ->
            val memoIdStr = backStackEntry.arguments?.getString("memoId") ?: ""
            val isShared = memoIdStr.startsWith("shared_")
            val memoId = memoIdStr.toIntOrNull() ?: -1

            // 공유 텍스트를 route parameter에서 디코딩
            val sharedText = remember {
                if (isShared) {
                    try {
                        java.net.URLDecoder.decode(memoIdStr.removePrefix("shared_"), "UTF-8")
                    } catch (e: Exception) { "" }
                } else ""
            }
            val youtubeUrl = remember { YOUTUBE_REGEX.find(sharedText)?.value }

            // YouTube 요약 상태
            var summaryState by remember { mutableStateOf<SummaryState>(SummaryState.Idle) }
            var youtubeTitle by remember { mutableStateOf<String?>(null) }

            val scope = rememberCoroutineScope()

            // YouTube URL이면 자동 요약 시작 (캐시 우선 조회)
            LaunchedEffect(youtubeUrl) {
                if (youtubeUrl != null && summaryState is SummaryState.Idle) {
                    val videoId = extractVideoId(youtubeUrl)
                    // 캐��� 조회
                    val cached = videoId?.let { youtubeSummaryDao.getByVideoId(it) }
                    if (cached != null) {
                        summaryState = SummaryState.Success(cached.summary)
                        return@LaunchedEffect
                    }
                    summaryState = SummaryState.Loading
                    try {
                        val summary = summarizeVideo(
                            videoId!!, youtubeUrl,
                            onTitleFound = { title -> youtubeTitle = title }
                        )
                        // 캐시 저장
                        youtubeSummaryDao.insert(YoutubeSummary(
                            videoId = videoId,
                            url = youtubeUrl,
                            summary = summary
                        ))
                        summaryState = SummaryState.Success(summary)
                    } catch (e: Exception) {
                        val errorMsg = when {
                            e.message?.contains("token count exceeds") == true ||
                            e.message?.contains("INVALID_ARGUMENT") == true ->
                                "영상이 너무 길어 요약할 수 없어요. 약 30분 이하 영상을 시도해주세요."
                            e.message?.contains("Rate limit") == true ->
                                "요청이 너무 많아요. 잠시 후 다시 시도해주세요."
                            e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true ->
                                "응답 시간이 초과됐어요. 더 짧은 영상을 시도해주세요."
                            else -> e.message ?: "요약 실패"
                        }
                        summaryState = SummaryState.Error(errorMsg)
                    }
                }
            }

            // 일반 공유 텍스트 프리필
            val sharedMemo = remember(summaryState) {
                when {
                    youtubeUrl != null -> when (val state = summaryState) {
                        is SummaryState.Success -> {
                            val lines = state.text.split("\n", limit = 2)
                            val title = lines.firstOrNull()?.replace(Regex("^#+\\s*"), "")?.take(50) ?: "유튜브 요약"
                            val content = "${state.text}\n\n원본: $youtubeUrl"
                            MemoUiState(0, title, 1, content)
                        }
                        else -> null
                    }
                    isShared && sharedText.isNotEmpty() -> {
                        val lines = sharedText.split("\n", limit = 2)
                        val title = lines.firstOrNull()?.take(50) ?: ""
                        val content = if (lines.size > 1) lines[1].trimStart() else sharedText
                        MemoUiState(0, title, 1, content)
                    }
                    else -> null
                }
            }

            val memos by repository.getMemos()
                .map { list ->
                    list.map { memo ->
                        MemoUiState(
                            id = memo.id,
                            name = memo.name,
                            categoryId = memo.categoryId,
                            content = memo.content,
                            createdAt = memo.createdAt,
                            updatedAt = memo.updatedAt,
                            format = when (memo.format) {
                                MemoFormat.MARKDOWN -> MemoFormatUi.MARKDOWN
                                MemoFormat.PLAIN -> MemoFormatUi.PLAIN
                            }
                        )
                    }
                }
                .collectAsState(initial = emptyList())
            val existingMemo = memos.firstOrNull { it.id == memoId }

            // 요약 에러 시 원본 URL만 프리필
            val finalMemo = sharedMemo
                ?: if (youtubeUrl != null && summaryState is SummaryState.Error) {
                    MemoUiState(0, "유튜브 메모", 1, "$youtubeUrl\n\n(요약 실패: ${(summaryState as SummaryState.Error).message})")
                } else {
                    existingMemo ?: MemoUiState(0, "", 1, "")
                }

            // AI 사용량 체크
            val context = LocalContext.current
            val aiPrefs = remember { context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE) }
            val summaryCount = remember { mutableStateOf(aiPrefs.getInt(KEY_SUMMARY_COUNT, 0)) }
            val canSummarize = summaryCount.value < FREE_SUMMARY_LIMIT

            // 요약 상태 통합 (ACTION_SEND + 메모 내 링크 감지)
            var inlineSummaryState by remember { mutableStateOf<SummaryState>(SummaryState.Idle) }
            // ACTION_SEND 요약 결과를 inlineSummaryState에 반영
            LaunchedEffect(summaryState) {
                if (summaryState !is SummaryState.Idle) {
                    inlineSummaryState = summaryState
                }
            }

            // 자동저장용 memoId 추적 (새 메모 insert 후 update로 전환)
            var savedMemoId by remember { mutableStateOf(memoId) }

            MemoScreen(
                existingMemo = finalMemo,
                onBack = onBack,
                onAutoSave = { memo ->
                    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                    GlobalScope.launch {
                        if (savedMemoId > 0) {
                            repository.updateMemo(memo.copy(id = savedMemoId).toEntity())
                        } else if (memo.name.isNotBlank() || memo.content.isNotBlank()) {
                            val newId = repository.addMemo(memo.toEntity())
                            savedMemoId = newId.toInt()
                        }
                    }
                },
                isSummarizing = inlineSummaryState is SummaryState.Loading,
                summaryResult = (inlineSummaryState as? SummaryState.Success)?.text,
                youtubeTitle = youtubeTitle,
                onYoutubeDetected = { videoId ->
                    scope.launch {
                        val info = captionService.extractVideoInfo(videoId)
                        if (info != null) {
                            youtubeTitle = info.title
                        }
                    }
                },
                onYoutubeSummarize = if (canSummarize) { { url ->
                    scope.launch {
                        val videoId = extractVideoId(url)
                        // 캐시 조회
                        val cached = videoId?.let { youtubeSummaryDao.getByVideoId(it) }
                        if (cached != null) {
                            inlineSummaryState = SummaryState.Success(cached.summary)
                            return@launch
                        }
                        inlineSummaryState = SummaryState.Loading
                        try {
                            val summary = if (videoId != null) {
                                summarizeVideo(
                                    videoId, url,
                                    onTitleFound = { title -> youtubeTitle = title }
                                )
                            } else {
                                aiApiService.generateContentWithVideo(
                                    prompt = SUMMARY_PROMPT,
                                    videoUrl = url,
                                )
                            }
                            // 캐시 저장
                            if (videoId != null) {
                                youtubeSummaryDao.insert(YoutubeSummary(
                                    videoId = videoId,
                                    url = url,
                                    summary = summary
                                ))
                            }
                            inlineSummaryState = SummaryState.Success(summary)
                            // 성공 시 사용 횟수 증가
                            val newCount = summaryCount.value + 1
                            aiPrefs.edit().putInt(KEY_SUMMARY_COUNT, newCount).apply()
                            summaryCount.value = newCount
                        } catch (e: Exception) {
                            val errorMsg = when {
                                e.message?.contains("token count exceeds") == true ||
                                e.message?.contains("INVALID_ARGUMENT") == true ->
                                    "영상이 너무 길어 요약할 수 없어요. 약 30분 이하 영상을 시도해주세요."
                                e.message?.contains("Rate limit") == true ->
                                    "요청이 너무 많아요. 잠시 후 다시 시도해주세요."
                                e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true ->
                                    "응답 시간이 초과됐어요. 더 짧은 영상을 시도해주세요."
                                else -> e.message ?: "요약 실패"
                            }
                            inlineSummaryState = SummaryState.Error(errorMsg)
                        }
                    }
                } } else null,
                onSave = { memo ->
                    scope.launch {
                        if (memoId > 0 && existingMemo != null) {
                            repository.updateMemo(memo.toEntity())
                        } else {
                            repository.addMemo(memo.toEntity())
                        }
                        onNavigateToHome()
                    }
                },
                onDelete = if (memoId > 0) { id ->
                    scope.launch {
                        repository.deleteMemo(id)
                        onNavigateToHome()
                    }
                } else null
            )
        }
    }

    private fun MemoUiState.toEntity() = Memo(
        id = id,
        name = name,
        categoryId = categoryId,
        content = content
    )

    private suspend fun summarizeVideo(
        videoId: String,
        videoUrl: String,
        onTitleFound: ((String) -> Unit)? = null
    ): String {
        // 1. 자막 + 제목 추출 시도
        val videoInfo = captionService.extractVideoInfo(videoId)
        if (videoInfo != null) {
            onTitleFound?.invoke(videoInfo.title)
        }
        val captions = videoInfo?.captions
        if (captions != null) {
            // 자막 기반 요약 (텍스트만, 빠르고 저렴)
            return aiApiService.generateContent(
                "$SUMMARY_PROMPT\n\n아래는 영상의 자막입니다:\n\n$captions"
            )
        }
        // 2. 자막 없으면 fallback — 영상 직접 분석
        return aiApiService.generateContentWithVideo(
            prompt = SUMMARY_PROMPT,
            videoUrl = videoUrl,
        )
    }
}

private sealed class SummaryState {
    data object Idle : SummaryState()
    data object Loading : SummaryState()
    data class Success(val text: String) : SummaryState()
    data class Error(val message: String) : SummaryState()
}
