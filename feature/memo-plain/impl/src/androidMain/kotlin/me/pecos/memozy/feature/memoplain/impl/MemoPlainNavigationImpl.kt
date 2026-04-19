package me.pecos.memozy.feature.memoplain.impl

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.confirm
import me.pecos.memozy.feature.core.resource.generated.resources.login_prompt_message
import me.pecos.memozy.feature.core.resource.generated.resources.login_prompt_title
import org.jetbrains.compose.resources.stringResource
import me.pecos.memozy.presentation.screen.memo.SummaryMode
import me.pecos.memozy.presentation.screen.memo.SummaryStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.pecos.memozy.data.datasource.local.AiUsageDao
import me.pecos.memozy.data.datasource.local.YoutubeSummaryDao
import me.pecos.memozy.data.datasource.local.entity.AiUsage
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.entity.YoutubeSummary
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.pecos.memozy.data.datasource.remote.ai.AIApiService
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionService
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.model.MemoFormat
import me.pecos.memozy.feature.memoplain.api.MemoPlainNavigation
import me.pecos.memozy.feature.memoplain.api.MemoPlainRoute
import me.pecos.memozy.platform.media.AudioFileStore
import me.pecos.memozy.platform.media.AudioRecorder
import me.pecos.memozy.platform.media.MediaService
import org.koin.compose.koinInject
import me.pecos.memozy.feature.core.viewmodel.model.MemoFormatUi
import me.pecos.memozy.feature.core.viewmodel.model.MemoUiState
import me.pecos.memozy.feature.core.viewmodel.settings.PreferencesProvider
import me.pecos.memozy.presentation.screen.memo.MemoScreen
import me.pecos.memozy.presentation.screen.memo.components.AiLimitBottomSheet
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalIsLoggedIn
import me.pecos.memozy.presentation.theme.LocalRewardAdProvider
import me.pecos.memozy.presentation.theme.LocalSubscriptionTier

class MemoPlainNavigationImpl(
    private val repository: MemoRepository,
    private val aiApiService: AIApiService,
    private val youtubeSummaryDao: YoutubeSummaryDao,
    private val captionService: YouTubeCaptionService,
    private val aiUsageDao: AiUsageDao,
    private val webScrapeService: me.pecos.memozy.data.datasource.remote.ai.WebScrapeService
) : MemoPlainNavigation {

    companion object {
        private const val FEATURE_YOUTUBE_SUMMARY = "youtube_summary"
        private const val FEATURE_WEB_SUMMARY = "web_summary"
        private const val FEATURE_TRANSCRIPTION = "transcription"
        private const val FEATURE_IMAGE_OCR = "image_ocr"
        private const val FEATURE_REWARD_AD = "reward_ad"
        private const val FEATURE_AI_ASSIST = "ai_assist"
        private const val MAX_DAILY_AD_VIEWS = 3
        private const val MAX_MEMO_CONTEXT_CHARS = 3000
        private const val MEMOZY_AI_SYSTEM_ROLE = "너는 Memozy AI야. 메모지 앱의 AI 어시스턴트로, 사용자의 메모 작성을 돕는 게 너의 역할이야. 너의 이름은 'Memozy AI'이고, 다른 AI 서비스의 이름으로 자신을 소개하면 안 돼. 사용자가 '너 누구야?' 등 정체를 물어볼 때만 'Memozy AI입니다! 메모 작성을 도와드릴게요' 라고 답해. 그 외에는 자기소개 없이 바로 답변해. 인사말, 도입부('도와드릴게요', '해결책을 찾아볼게요', '물론이죠', '네!' 등) 없이 핵심 내용부터 바로 시작해."
        private const val NO_MARKDOWN_RULE = "마크다운 문법(**, ##, - 등)을 절대 사용하지 마. 순수 텍스트로만 답해."

        private fun stripMarkdown(text: String): String = text
            .replace(Regex("""^\s*#{1,6}\s+""", RegexOption.MULTILINE), "")  // ### 제목
            .replace(Regex("""\*\*(.+?)\*\*"""), "$1")                       // **볼드**
            .replace(Regex("""\*(.+?)\*"""), "$1")                           // *이탤릭*
            .replace(Regex("""~~(.+?)~~"""), "$1")                           // ~~취소선~~
            .replace(Regex("""^[-*+]\s+""", RegexOption.MULTILINE), "• ")   // - 리스트 → •
            .replace(Regex("""^\d+\.\s+""", RegexOption.MULTILINE), "")     // 1. 번호 리스트
            .replace(Regex("""`(.+?)`"""), "$1")                             // `코드`

        @OptIn(ExperimentalTime::class)
        private fun startOfToday(): Long {
            val zone = TimeZone.currentSystemDefault()
            val now = Clock.System.now().toLocalDateTime(zone)
            val midnight = LocalDateTime(now.year, now.monthNumber, now.dayOfMonth, 0, 0, 0, 0)
            return midnight.toInstant(zone).toEpochMilliseconds()
        }

        private val YOUTUBE_REGEX = Regex(
            """(?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)[\w-]+"""
        )

        private val VIDEO_ID_REGEX = Regex(
            """(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)([\w-]+)"""
        )

        fun extractVideoId(url: String): String? =
            VIDEO_ID_REGEX.find(url)?.groupValues?.get(1)

        private fun langInstruction(lang: String) = when (lang) {
            "en" -> "in English"
            "ja" -> "日本語で"
            else -> "한국어로"
        }

        private fun buildYoutubePrompt(style: SummaryStyle, lang: String): String {
            val l = langInstruction(lang)
            return when (style) {
                SummaryStyle.SIMPLE -> """
                    |이 유튜브 영상을 ${l} 간결하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
                    |
                    |📋 한줄 요약
                    |영상의 핵심을 한 문장으로 요약
                    |
                    |📌 키워드
                    |#키워드1 #키워드2 #키워드3
                    |
                    |📖 핵심 내용
                    |- 첫 번째 핵심 내용
                    |
                    |- 두 번째 핵심 내용
                    |
                    |- 세 번째 핵심 내용
                    |
                    |(가장 중요한 내용 3~5개를 각 1줄로 정리. 첫 항목은 📖 핵심 내용 바로 다음 줄에, 이후 항목들 사이에는 빈 줄 1개를 넣어줘)
                """.trimMargin()
                SummaryStyle.DETAILED -> """
                    |이 유튜브 영상을 ${l} 상세하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
                    |
                    |📋 한줄 요약
                    |영상의 핵심을 한 문장으로 요약
                    |
                    |📌 핵심 키워드
                    |#키워드1 #키워드2 #키워드3 #키워드4 #키워드5
                    |
                    |⏰ 타임라인별 상세 요약
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
                SummaryStyle.NOTE -> """
                    |다음 유튜브 영상 내용을 ${l} 학습 노트 형태로 정리해줘. 인사말이나 부가 설명 없이 바로 시작해:
                    |
                    |주제별로 묶어서 개조식(- 기호)으로 간결하게 작성해.
                    |굵게(**) 표시는 꼭 필요한 핵심 용어에만 최소한으로 사용해.
                    |한 항목당 1줄, 복습할 때 빠르게 훑을 수 있는 형태로.
                """.trimMargin()
                SummaryStyle.LANGUAGE -> {
                    val userLang = when (lang) {
                        "en" -> "English"
                        "ja" -> "日本語"
                        else -> "한국어"
                    }
                    """
                    |다음 유튜브 영상 자막을 언어 학습용으로 정리해줘.
                    |사용자의 모국어는 ${userLang}이고, 영상에 나오는 외국어를 학습하려는 목적이야.
                    |절대 #, ##, **, *** 같은 마크다운 서식을 사용하지 마. 이모지와 일반 텍스트만 써.
                    |인사말이나 부가 설명 없이 바로 아래 형식대로만 출력해:
                    |
                    |📋 핵심 표현 (10~15개)
                    |
                    |영상에서 나온 외국어 표현을 원문 그대로 적고, 바로 다음 줄에 ${userLang} 뜻을 적어.
                    |표현과 표현 사이에는 빈 줄을 넣어.
                    |
                    |외국어 원문
                    |${userLang} 뜻
                    |
                    |외국어 원문
                    |${userLang} 뜻
                    |
                    |📖 주요 문장 (5~10개)
                    |
                    |영상에서 나온 핵심 문장을 원문 그대로 적고, 다음 줄에 ${userLang} 해석을 적어.
                    |각 문장 앞에 * 를 붙여. 문장과 문장 사이에는 빈 줄을 넣어.
                    |
                    |* 외국어 원문 문장
                    |${userLang} 해석
                    |
                    |* 외국어 원문 문장
                    |${userLang} 해석
                    |
                    |💡 학습 포인트
                    |
                    |문법, 뉘앙스, 발음, 사용법 등 학습에 도움되는 내용을 ${userLang}로 2~4줄 정리.
                """.trimMargin()
                }
            }
        }

        /** 기존 SummaryMode 호환 — 레거시 호출용 */
        private fun buildYoutubePrompt(mode: SummaryMode, lang: String): String {
            val style = when (mode) {
                SummaryMode.SIMPLE -> SummaryStyle.SIMPLE
                SummaryMode.DETAILED -> SummaryStyle.DETAILED
            }
            return buildYoutubePrompt(style, lang)
        }

        private fun buildWebPrompt(mode: SummaryMode, lang: String): String {
            val l = langInstruction(lang)
            return when (mode) {
                SummaryMode.SIMPLE -> """
                    |이 웹페이지 내용을 ${l} 간결하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
                    |
                    |📋 한줄 요약
                    |페이지의 핵심을 한 문장으로 요약
                    |
                    |📌 키워드
                    |#키워드1 #키워드2 #키워드3
                    |
                    |📖 핵심 내용
                    |- 가장 중요한 내용 3~5개를 각 1줄로 정리
                """.trimMargin()
                SummaryMode.DETAILED -> """
                    |이 웹페이지 내용을 ${l} 상세하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
                    |
                    |📋 한줄 요약
                    |페이지의 핵심을 한 문장으로 요약
                    |
                    |📌 핵심 키워드
                    |#키워드1 #키워드2 #키워드3 #키워드4 #키워드5
                    |
                    |📖 섹션별 상세 요약
                    |각 섹션/주제별로 나눠서 아래처럼 정리해줘:
                    |
                    |[섹션 제목]
                    |- 상세 설명 (2~3문장)
                    |- 중요한 내용이나 인사이트
                    |
                    |(모든 섹션을 빠짐없이 정리)
                    |
                    |💡 핵심 인사이트
                    |- 이 글에서 얻을 수 있는 주요 인사이트를 정리
                """.trimMargin()
            }
        }
    }

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onNavigateToHome: () -> Unit,
        onBack: () -> Unit
    ) {
        navGraphBuilder.composable(
            MemoPlainRoute.MEMO,
            enterTransition = { fadeIn(tween(150)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { fadeOut(tween(150)) }
        ) { backStackEntry ->
            val memoIdStr = backStackEntry.arguments?.getString("memoId") ?: ""
            val isShared = memoIdStr.startsWith("shared_")
            val isSharedImage = memoIdStr.startsWith("shared_image_")
            val isSharedPdf = memoIdStr.startsWith("shared_pdf_")
            val memoId = memoIdStr.toIntOrNull() ?: -1

            // 공유 텍스트를 route parameter에서 디코딩
            val sharedText = remember {
                if (isShared && !isSharedImage && !isSharedPdf) {
                    try {
                        java.net.URLDecoder.decode(memoIdStr.removePrefix("shared_"), "UTF-8")
                    } catch (e: Exception) { "" }
                } else ""
            }

            // 이미지/PDF URI 디코딩
            val sharedFileUri = remember {
                if (isSharedImage || isSharedPdf) {
                    try {
                        val prefix = if (isSharedImage) "shared_image_" else "shared_pdf_"
                        android.net.Uri.parse(java.net.URLDecoder.decode(memoIdStr.removePrefix(prefix), "UTF-8"))
                    } catch (e: Exception) { null }
                } else null
            }

            val youtubeUrl = remember { YOUTUBE_REGEX.find(sharedText)?.value }

            // YouTube 요약 상태
            var summaryState by remember { mutableStateOf<SummaryState>(SummaryState.Idle) }
            var youtubeTitle by remember { mutableStateOf<String?>(null) }

            val preferencesProvider: PreferencesProvider = koinInject()
            val languageCode = remember {
                preferencesProvider.getString("language_code", "ko")
            }

            val scope = rememberCoroutineScope()

            // YouTube URL 공유 시 타이틀만 미리 조회 (자동 요약 안 함 — 인라인 카드로 표시)
            LaunchedEffect(youtubeUrl) {
                if (youtubeUrl != null) {
                    val videoId = extractVideoId(youtubeUrl)
                    if (videoId != null) {
                        val title = captionService.fetchTitle(videoId)
                        if (title != null) youtubeTitle = title
                    }
                }
            }

            // 로그인 여부 체크
            val isLoggedIn = LocalIsLoggedIn.current
            var showLoginPrompt by remember { mutableStateOf(false) }

            // AI 사용량 체크 (티어별 일일 한도)
            val subscriptionTier = LocalSubscriptionTier.current
            val rewardAdProvider = LocalRewardAdProvider.current
            var dailyUsageCount by remember { mutableStateOf(0) }
            var dailyAdViewCount by remember { mutableStateOf(0) }
            var adBonusCount by remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                dailyUsageCount = aiUsageDao.getTotalCountSince(startOfToday())
                dailyAdViewCount = aiUsageDao.getCountSince(FEATURE_REWARD_AD, startOfToday())
            }
            val dailyLimit = subscriptionTier.dailyAiLimit + adBonusCount
            val canUseAiQuota = dailyUsageCount < dailyLimit
            val canUseAi = isLoggedIn && canUseAiQuota
            val canWatchAd = !subscriptionTier.isPro && dailyAdViewCount < MAX_DAILY_AD_VIEWS
            val remainingAdViews = MAX_DAILY_AD_VIEWS - dailyAdViewCount
            var showLimitBottomSheet by remember { mutableStateOf(false) }
            val notifyAiBlocked: () -> Unit = {
                if (!isLoggedIn) showLoginPrompt = true else showLimitBottomSheet = true
            }

            // 이미지 OCR 처리
            var imageOcrState by remember { mutableStateOf<SummaryState>(SummaryState.Idle) }
            val imageContext = LocalContext.current

            LaunchedEffect(sharedFileUri, isSharedImage) {
                if (isSharedImage && sharedFileUri != null && imageOcrState is SummaryState.Idle) {
                    if (!canUseAi) {
                        imageOcrState = SummaryState.Error("오늘 AI 사용 한도를 모두 사용했어요.")
                        return@LaunchedEffect
                    }
                    imageOcrState = SummaryState.Loading
                    try {
                        val bytes = imageContext.contentResolver.openInputStream(sharedFileUri)?.use { it.readBytes() }
                            ?: throw Exception("Cannot read image")
                        @OptIn(ExperimentalEncodingApi::class)
                        val base64 = Base64.Default.encode(bytes)
                        val mimeType = imageContext.contentResolver.getType(sharedFileUri) ?: "image/jpeg"
                        val result = aiApiService.describeImage(base64, mimeType)
                        imageOcrState = SummaryState.Success(result)
                        aiUsageDao.insert(AiUsage(feature = FEATURE_IMAGE_OCR))
                    } catch (e: Exception) {
                        imageOcrState = SummaryState.Error(e.message ?: "이미지 처리 실패")
                    }
                }
            }

            // 일반 공유 텍스트 프리필
            val sharedMemo = remember(youtubeTitle, imageOcrState) {
                when {
                    youtubeUrl != null -> {
                        // 본문 비우고, youtubeUrl 필드로 인라인 카드 표시
                        MemoUiState(0, youtubeTitle ?: "", 1, "", youtubeUrl = youtubeUrl)
                    }
                    isSharedImage -> when (val state = imageOcrState) {
                        is SummaryState.Success -> {
                            val lines = state.text.split("\n", limit = 2)
                            val title = lines.firstOrNull()?.take(50) ?: "이미지 메모"
                            val content = state.text
                            MemoUiState(0, title, 1, content)
                        }
                        is SummaryState.Error -> MemoUiState(0, "이미지 메모", 1, "[이미지 인식 실패: ${state.message}]")
                        else -> null
                    }
                    isSharedPdf -> MemoUiState(0, "PDF 메모", 1, "[PDF 파일이 첨부되었습니다]")
                    isShared && sharedText.isNotEmpty() -> {
                        val lines = sharedText.split("\n", limit = 2)
                        val title = lines.firstOrNull()?.take(50) ?: ""
                        val content = if (lines.size > 1) lines[1].trimStart() else sharedText
                        MemoUiState(0, title, 1, content)
                    }
                    else -> null
                }
            }

            // 기존 메모 직접 조회 (suspend, 빠름)
            var existingMemo by remember { mutableStateOf<MemoUiState?>(null) }
            var memoLoaded by remember { mutableStateOf(memoId <= 0) }
            LaunchedEffect(memoId) {
                if (memoId > 0) {
                    val memo = repository.getMemoById(memoId)
                    existingMemo = memo?.let {
                        MemoUiState(
                            id = it.id,
                            name = it.name,
                            categoryId = it.categoryId,
                            content = it.content,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                            format = when (it.format) {
                                MemoFormat.MARKDOWN -> MemoFormatUi.MARKDOWN
                                MemoFormat.PLAIN -> MemoFormatUi.PLAIN
                            },
                            isPinned = it.isPinned,
                            audioPath = it.audioPath,
                            styles = it.styles,
                            youtubeUrl = it.youtubeUrl,
                            summaryContent = it.summaryContent
                        )
                    }
                    memoLoaded = true
                }
            }

            if (!memoLoaded) return@composable

            // 요약 에러 시 원본 URL만 프리필
            val finalMemo = sharedMemo
                ?: if (youtubeUrl != null && summaryState is SummaryState.Error) {
                    MemoUiState(0, "유튜브 메모", 1, "$youtubeUrl\n\n(요약 실패: ${(summaryState as SummaryState.Error).message})")
                } else {
                    existingMemo ?: MemoUiState(0, "", 1, "")
                }

            val context = LocalContext.current

            // 요약 상태 통합 (ACTION_SEND + 메모 내 링크 감지)
            var inlineSummaryState by remember { mutableStateOf<SummaryState>(SummaryState.Idle) }
            // ACTION_SEND 요약 결과를 inlineSummaryState에 반영
            LaunchedEffect(summaryState) {
                if (summaryState !is SummaryState.Idle) {
                    inlineSummaryState = summaryState
                }
            }

            // Memozy AI 상태
            var aiAssistStreamingText by remember { mutableStateOf<String?>(null) }
            var isAiAssistLoading by remember { mutableStateOf(false) }
            var isAiCancelled by remember { mutableStateOf(false) }
            var aiAssistJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

            // 녹음/STT 상태
            var isRecording by remember { mutableStateOf(false) }
            var isTranscribing by remember { mutableStateOf(false) }
            var transcriptionResult by remember { mutableStateOf<String?>(null) }
            var transcriptionError by remember { mutableStateOf<String?>(null) }
            var savedAudioPath by remember { mutableStateOf<String?>(null) }
            // 에러/결과 메시지 3초 후 자동 해제
            LaunchedEffect(transcriptionError) {
                if (transcriptionError != null) {
                    kotlinx.coroutines.delay(3000)
                    transcriptionError = null
                }
            }
            LaunchedEffect(transcriptionResult) {
                if (transcriptionResult != null) {
                    // 본문에 삽입된 후 상태 초기화 (MemoScreen에서 LaunchedEffect로 삽입)
                    kotlinx.coroutines.delay(500)
                    transcriptionResult = null
                }
            }

            val mediaService: MediaService = koinInject()
            val audioFileStore: AudioFileStore = koinInject()
            var audioRecorder by remember { mutableStateOf<AudioRecorder?>(null) }
            var recordingStartTime by remember { mutableStateOf(0L) }
            val audioCachePath = remember { audioFileStore.cachePath("recording.m4a") }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    // 권한 획득 → 녹음 시작
                    try {
                        val recorder = mediaService.createAudioRecorder()
                        recorder.start(audioCachePath)
                        audioRecorder = recorder
                        recordingStartTime = Clock.System.now().toEpochMilliseconds()
                        isRecording = true
                        transcriptionError = null
                    } catch (e: Exception) {
                        transcriptionError = "녹음을 시작할 수 없어요."
                    }
                } else {
                    transcriptionError = "마이크 권한이 필요해요."
                }
            }

            fun startRecording() {
                transcriptionResult = null
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    try {
                        val recorder = mediaService.createAudioRecorder()
                        recorder.start(audioCachePath)
                        audioRecorder = recorder
                        recordingStartTime = Clock.System.now().toEpochMilliseconds()
                        isRecording = true
                        transcriptionError = null
                    } catch (e: Exception) {
                        transcriptionError = "녹음을 시작할 수 없어요."
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            fun stopRecordingAndTranscribe() {
                try {
                    audioRecorder?.apply { stop(); release() }
                } catch (_: Exception) { }
                val durationSeconds = (Clock.System.now().toEpochMilliseconds() - recordingStartTime) / 1000
                audioRecorder = null
                isRecording = false

                if (!audioFileStore.exists(audioCachePath) || audioFileStore.length(audioCachePath) < 1024) {
                    transcriptionError = "녹음이 너무 짧아요. 다시 시도해주세요."
                    audioFileStore.delete(audioCachePath)
                    return
                }

                isTranscribing = true
                scope.launch {
                    try {
                        val audioBytes = audioFileStore.readBytes(audioCachePath)
                        @OptIn(ExperimentalEncodingApi::class)
                        val base64 = Base64.Default.encode(audioBytes)
                        val result = aiApiService.transcribeAudio(base64, "audio/mp4", durationSeconds)
                        // Gemini가 프롬프트를 그대로 반환하는 경우 필터링
                        if (result.contains("받아쓰기") || result.contains("텍스트만 출력") || result.isBlank()) {
                            transcriptionError = "음성이 감지되지 않았어요. 다시 시도해주세요."
                            transcriptionResult = null
                            audioFileStore.delete(audioCachePath)
                        } else {
                            // 오디오 파일을 영구 저장소로 이동 (제목과 동일한 파일명)
                            val nowLocal = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            fun Int.pad2(): String = toString().padStart(2, '0')
                            val yy = (nowLocal.year % 100).pad2()
                            val stamp = "$yy.${nowLocal.monthNumber.pad2()}.${nowLocal.dayOfMonth.pad2()} ${nowLocal.hour.pad2()}:${nowLocal.minute.pad2()}"
                            val safeFileName = "$stamp 녹음".replace(":", "-").replace("/", "-")
                            val permanentPath = audioFileStore.permanentPath(safeFileName)
                            audioFileStore.copy(audioCachePath, permanentPath)
                            audioFileStore.delete(audioCachePath)
                            savedAudioPath = permanentPath

                            transcriptionResult = result
                            transcriptionError = null
                            aiUsageDao.insert(AiUsage(feature = FEATURE_TRANSCRIPTION))
                        }
                    } catch (e: Exception) {
                        transcriptionError = "음성 변환에 실패했어요."
                        audioFileStore.delete(audioCachePath)
                    } finally {
                        isTranscribing = false
                    }
                }
            }

            // 웹 요약 상태
            var isWebSummarizing by remember { mutableStateOf(false) }
            var webSummaryResult by remember { mutableStateOf<String?>(null) }
            var webSummaryError by remember { mutableStateOf<String?>(null) }
            var webPageTitle by remember { mutableStateOf<String?>(null) }

            // 양식 선택 상태
            var activeSummaryStyle by remember { mutableStateOf(SummaryStyle.SIMPLE) }

            // 현재 진행 중인 요약 Job (취소용)
            var currentSummarizeJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

            // 자동저장용 memoId 추적 (새 메모 insert 후 update로 전환)
            var savedMemoId by remember { mutableStateOf(memoId) }

            MemoScreen(
                existingMemo = finalMemo,
                onBack = onBack,
                onAutoSave = { memo ->
                    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                    GlobalScope.launch {
                        val memoWithAudio = memo.copy(audioPath = savedAudioPath ?: memo.audioPath)
                        if (savedMemoId > 0) {
                            repository.updateMemo(memoWithAudio.copy(id = savedMemoId).toEntity())
                        } else if (memoWithAudio.name.isNotBlank() || memoWithAudio.content.isNotBlank()) {
                            // 즉시 임시 ID 설정하여 race condition 방지
                            savedMemoId = -999
                            val newId = repository.addMemo(memoWithAudio.toEntity())
                            savedMemoId = newId.toInt()
                        }
                    }
                },
                isSummarizing = inlineSummaryState is SummaryState.Loading || inlineSummaryState is SummaryState.Streaming,
                summaryResult = when (inlineSummaryState) {
                    is SummaryState.Streaming -> (inlineSummaryState as SummaryState.Streaming).text
                    is SummaryState.Success -> (inlineSummaryState as SummaryState.Success).text
                    else -> null
                },
                summaryError = (inlineSummaryState as? SummaryState.Error)?.message,
                youtubeTitle = youtubeTitle,
                onStartRecording = {
                    if (!canUseAi) {
                        notifyAiBlocked()
                    } else {
                        startRecording()
                    }
                },
                onStopRecording = { stopRecordingAndTranscribe() },
                isRecording = isRecording,
                isTranscribing = isTranscribing,
                transcriptionResult = transcriptionResult,
                transcriptionError = transcriptionError,
                audioPath = savedAudioPath,
                onCancelSummarize = {
                    currentSummarizeJob?.cancel()
                    currentSummarizeJob = null
                    inlineSummaryState = SummaryState.Idle
                    isWebSummarizing = false
                    isTranscribing = false
                },
                onWebSummarize = { url, mode ->
                    if (!canUseAi) {
                        notifyAiBlocked()
                        return@MemoScreen
                    }
                    currentSummarizeJob = scope.launch {
                        isWebSummarizing = true
                        webSummaryError = null
                        try {
                            val content = webScrapeService.scrapeWebPage(url)
                            if (content == null) {
                                webSummaryError = "웹페이지를 불러올 수 없어요."
                            } else {
                                webPageTitle = content.title
                                val webPrompt = buildWebPrompt(mode, languageCode)
                                val summary = retryOn503 {
                                    aiApiService.generateContent(
                                        "$webPrompt\n\n아래는 웹페이지 내용입니다:\n\n${content.text}"
                                    )
                                }
                                webSummaryResult = summary
                                aiUsageDao.insert(AiUsage(feature = FEATURE_WEB_SUMMARY))
                            }
                        } catch (e: Exception) {
                            webSummaryError = when {
                                e.message?.contains("503") == true || e.message?.contains("UNAVAILABLE") == true ->
                                    "AI 서버가 일시적으로 바빠요. 잠시 후 다시 시도해주세요."
                                e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true ->
                                    "응답 시간이 초과됐어요. 더 짧은 페이지를 시도해주세요."
                                else -> "DEBUG: ${e::class.simpleName}: ${e.message}"
                            }
                        } finally {
                            isWebSummarizing = false
                        }
                    }
                },
                isWebSummarizing = isWebSummarizing,
                webSummaryResult = webSummaryResult,
                webSummaryError = webSummaryError,
                webPageTitle = webPageTitle,
                onSummaryStyleSelected = { style, url ->
                    activeSummaryStyle = style
                    // 바텀시트에서 양식 선택 시 즉시 요약 실행
                    currentSummarizeJob?.cancel()
                    currentSummarizeJob = scope.launch {
                        if (!canUseAi) {
                            showLimitBottomSheet = true
                            return@launch
                        }
                        val videoId = extractVideoId(url)
                        val cached = videoId?.let { youtubeSummaryDao.getByKey(it, style.name, languageCode) }
                        if (cached != null) {
                            inlineSummaryState = SummaryState.Success(cached.summary)
                            return@launch
                        }
                        inlineSummaryState = SummaryState.Loading
                        try {
                            val summary = if (videoId != null) {
                                summarizeVideoStream(
                                    videoId, url,
                                    style = style,
                                    lang = languageCode,
                                    onTitleFound = { title -> youtubeTitle = title },
                                    onStreamUpdate = { partial ->
                                        inlineSummaryState = SummaryState.Streaming(partial)
                                    }
                                )
                            } else {
                                aiApiService.generateContentWithVideo(
                                    prompt = buildYoutubePrompt(style, languageCode),
                                    videoUrl = url,
                                )
                            }
                            if (videoId != null) {
                                youtubeSummaryDao.insert(YoutubeSummary(
                                    videoId = videoId,
                                    mode = style.name,
                                    language = languageCode,
                                    url = url,
                                    summary = summary
                                ))
                            }
                            inlineSummaryState = SummaryState.Success(summary)
                            aiUsageDao.insert(AiUsage(feature = FEATURE_YOUTUBE_SUMMARY))
                            dailyUsageCount++
                        } catch (e: Exception) {
                            inlineSummaryState = SummaryState.Error(e.message ?: "요약 실패")
                        }
                    }
                },
                onYoutubeDetected = { videoId ->
                    scope.launch {
                        val title = captionService.fetchTitle(videoId)
                        if (title != null) {
                            youtubeTitle = title
                        }
                    }
                },
                onYoutubeSummarize = { url, mode ->
                    // mode → activeSummaryStyle 동기화
                    val style = when (mode) {
                        SummaryMode.SIMPLE -> SummaryStyle.SIMPLE
                        SummaryMode.DETAILED -> SummaryStyle.DETAILED
                    }
                    activeSummaryStyle = style
                    currentSummarizeJob = scope.launch {
                        if (!canUseAi) {
                            showLimitBottomSheet = true
                            return@launch
                        }
                        val videoId = extractVideoId(url)
                        // 캐시 조회 (style + language 기반)
                        val cached = videoId?.let { youtubeSummaryDao.getByKey(it, style.name, languageCode) }
                        if (cached != null) {
                            inlineSummaryState = SummaryState.Success(cached.summary)
                            return@launch
                        }
                        inlineSummaryState = SummaryState.Loading
                        val ytPrompt = buildYoutubePrompt(activeSummaryStyle, languageCode)
                        try {
                            val summary = if (videoId != null) {
                                summarizeVideoStream(
                                    videoId, url,
                                    style = activeSummaryStyle,
                                    lang = languageCode,
                                    onTitleFound = { title -> youtubeTitle = title },
                                    onStreamUpdate = { partial ->
                                        inlineSummaryState = SummaryState.Streaming(partial)
                                    }
                                )
                            } else {
                                aiApiService.generateContentWithVideo(
                                    prompt = ytPrompt,
                                    videoUrl = url,
                                )
                            }
                            // 캐시 저장
                            if (videoId != null) {
                                youtubeSummaryDao.insert(YoutubeSummary(
                                    videoId = videoId,
                                    mode = activeSummaryStyle.name,
                                    language = languageCode,
                                    url = url,
                                    summary = summary
                                ))
                            }
                            inlineSummaryState = SummaryState.Success(summary)
                            // 성공 시 사용 횟수 증가
                            aiUsageDao.insert(AiUsage(feature = FEATURE_YOUTUBE_SUMMARY))
                            dailyUsageCount++
                        } catch (e: Exception) {
                            val errorMsg = when {
                                e.message?.contains("token count exceeds") == true ||
                                e.message?.contains("INVALID_ARGUMENT") == true ->
                                    "영상이 너무 길어 요약할 수 없어요."
                                e.message?.contains("Rate limit") == true ->
                                    "요청이 너무 많아요. 잠시 후 다시 시도해주세요."
                                e.message?.contains("503") == true || e.message?.contains("UNAVAILABLE") == true ||
                                e.message?.contains("high demand") == true ->
                                    "AI 서버가 일시적으로 바빠요. 잠시 후 다시 시도해주세요."
                                e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true ->
                                    "응답 시간이 초과됐어요. 더 짧은 영상을 시도해주세요."
                                else -> "DEBUG: ${e::class.simpleName}: ${e.message}"
                            }
                            inlineSummaryState = SummaryState.Error(errorMsg)
                        }
                    }
                },
                onSave = { memo ->
                    scope.launch {
                        val memoWithAudio = memo.copy(audioPath = savedAudioPath ?: memo.audioPath)
                        val finalMemoId: Int
                        // savedMemoId == -999 은 autoSave가 진행 중 → 완료 대기
                        if (savedMemoId == -999) {
                            // autoSave가 완료될 때까지 대기
                            while (savedMemoId == -999) { kotlinx.coroutines.delay(50) }
                        }
                        if (savedMemoId > 0) {
                            repository.updateMemo(memoWithAudio.copy(id = savedMemoId).toEntity())
                            finalMemoId = savedMemoId
                        } else if (memoId > 0 && existingMemo != null) {
                            repository.updateMemo(memoWithAudio.toEntity())
                            finalMemoId = memoId
                        } else {
                            finalMemoId = repository.addMemo(memoWithAudio.toEntity()).toInt()
                        }
                        onNavigateToHome()
                    }
                },
                onDelete = if (memoId > 0) { id ->
                    scope.launch {
                        repository.deleteMemo(id)
                        onNavigateToHome()
                    }
                } else null,
                // Memozy AI
                aiAssistStreamingText = aiAssistStreamingText,
                isAiAssistLoading = isAiAssistLoading,
                isAiCancelled = isAiCancelled,
                onAiCancel = {
                    aiAssistJob?.cancel()
                    isAiCancelled = true
                    aiAssistStreamingText = null
                    isAiAssistLoading = false
                },
                onAiCustomSend = { userMessage, currentTitle, currentBody ->
                    if (!canUseAi) {
                        notifyAiBlocked()
                        return@MemoScreen
                    }
                    aiAssistJob?.cancel()
                    aiAssistJob = scope.launch {
                        isAiAssistLoading = true
                        isAiCancelled = false
                        aiAssistStreamingText = ""
                        try {
                            val plainBody = currentBody.replace(Regex("<[^>]*>"), "").trim()
                            val memoBody = if (plainBody.length > MAX_MEMO_CONTEXT_CHARS) {
                                plainBody.take(2000) + "\n...(중략)...\n" + plainBody.takeLast(1000)
                            } else plainBody
                            val systemRole = MEMOZY_AI_SYSTEM_ROLE
                            val noMarkdownRule = NO_MARKDOWN_RULE
                            val prompt = buildString {
                                appendLine(systemRole)
                                appendLine("답변은 간결하고 핵심적으로.")
                                appendLine("사용자가 현재 메모를 작성 중이니, 메모 내용이 있으면 참고해서 답해줘.")
                                appendLine("메모와 관련 없는 질문이어도 자유롭게 답변해. 답변은 간결하게. $noMarkdownRule")
                                appendLine()
                                if (memoBody.isNotBlank()) {
                                    appendLine("=== 현재 메모 ===")
                                    appendLine("제목: $currentTitle")
                                    appendLine(memoBody)
                                    appendLine("=== 메모 끝 ===")
                                    appendLine()
                                }
                                appendLine("사용자: $userMessage")
                            }
                            val sb = StringBuilder()
                            aiApiService.generateContentStream(prompt).collect { delta ->
                                sb.append(delta)
                                aiAssistStreamingText = stripMarkdown(sb.toString())
                            }
                            // UI가 마지막 스트리밍 텍스트를 반영할 시간 확보
                            kotlinx.coroutines.yield()
                            kotlinx.coroutines.delay(50)
                            aiAssistStreamingText = null
                            aiUsageDao.insert(AiUsage(feature = FEATURE_AI_ASSIST))
                            dailyUsageCount++
                        } catch (e: Exception) {
                            aiAssistStreamingText = null
                            if (e is kotlinx.coroutines.CancellationException) return@launch
                        } finally {
                            isAiAssistLoading = false
                        }
                    }
                },
                onAiPresetAction = { actionName, currentTitle, currentBody ->
                    if (!canUseAi) {
                        notifyAiBlocked()
                        return@MemoScreen
                    }
                    aiAssistJob?.cancel()
                    aiAssistJob = scope.launch {
                        isAiAssistLoading = true
                        isAiCancelled = false
                        aiAssistStreamingText = ""
                        try {
                            val plainBody = currentBody.replace(Regex("<[^>]*>"), "").trim()
                            val memoBody = if (plainBody.length > MAX_MEMO_CONTEXT_CHARS) {
                                plainBody.take(2000) + "\n...(중략)...\n" + plainBody.takeLast(1000)
                            } else plainBody
                            if (memoBody.isBlank()) {
                                aiAssistStreamingText = null
                                return@launch
                            }
                            val systemRole = MEMOZY_AI_SYSTEM_ROLE
                            val noMarkdownRule = NO_MARKDOWN_RULE
                            val prompt = when (actionName) {
                                "EXPLAIN" -> buildString {
                                    appendLine(systemRole)
                                    appendLine("아래 메모 내용을 이해하기 쉽게 풀어서 설명해줘. 어려운 용어가 있으면 쉬운 말로 바꿔줘.")
                                    appendLine("설명문만 출력해. $noMarkdownRule")
                                    appendLine()
                                    appendLine("=== 메모 ===")
                                    appendLine("제목: $currentTitle")
                                    appendLine(memoBody)
                                }
                                "ORGANIZE" -> buildString {
                                    appendLine(systemRole)
                                    appendLine("아래 메모 내용을 깔끔하게 정리해줘. 구조화하고 가독성을 높여줘.")
                                    appendLine("정리된 텍스트만 출력해. $noMarkdownRule")
                                    appendLine()
                                    appendLine("=== 메모 ===")
                                    appendLine("제목: $currentTitle")
                                    appendLine(memoBody)
                                }
                                "SUMMARIZE" -> buildString {
                                    appendLine(systemRole)
                                    appendLine("아래 메모 내용의 핵심만 간결하게 요약해줘. 원문의 1/3 이하로 줄여줘.")
                                    appendLine("요약문만 출력해. $noMarkdownRule")
                                    appendLine()
                                    appendLine("=== 메모 ===")
                                    appendLine("제목: $currentTitle")
                                    appendLine(memoBody)
                                }
                                else -> return@launch
                            }
                            val sb = StringBuilder()
                            aiApiService.generateContentStream(prompt).collect { delta ->
                                sb.append(delta)
                                aiAssistStreamingText = stripMarkdown(sb.toString())
                            }
                            // UI가 마지막 스트리밍 텍스트를 반영할 시간 확보
                            kotlinx.coroutines.yield()
                            kotlinx.coroutines.delay(50)
                            aiAssistStreamingText = null
                            aiUsageDao.insert(AiUsage(feature = FEATURE_AI_ASSIST))
                            dailyUsageCount++
                        } catch (e: Exception) {
                            aiAssistStreamingText = null
                            if (e is kotlinx.coroutines.CancellationException) return@launch
                        } finally {
                            isAiAssistLoading = false
                        }
                    }
                },
            )

            if (showLoginPrompt) {
                AlertDialog(
                    onDismissRequest = { showLoginPrompt = false },
                    title = { Text(stringResource(Res.string.login_prompt_title)) },
                    text = { Text(stringResource(Res.string.login_prompt_message)) },
                    confirmButton = {
                        TextButton(onClick = { showLoginPrompt = false }) {
                            Text(stringResource(Res.string.confirm))
                        }
                    }
                )
            }

            if (showLimitBottomSheet) {
                AiLimitBottomSheet(
                    subscriptionTier = subscriptionTier,
                    canWatchAd = canWatchAd,
                    remainingAdViews = remainingAdViews,
                    isAdLoading = rewardAdProvider?.isAdLoading == true,
                    onWatchAd = {
                        rewardAdProvider?.showAd {
                            scope.launch {
                                aiUsageDao.insert(AiUsage(feature = FEATURE_REWARD_AD))
                                dailyAdViewCount++
                                adBonusCount++
                                showLimitBottomSheet = false
                            }
                        }
                    },
                    onUpgrade = { showLimitBottomSheet = false },
                    onDismiss = { showLimitBottomSheet = false }
                )
            }
        }
    }

    private fun MemoUiState.toEntity() = Memo(
        id = id,
        name = name,
        categoryId = categoryId,
        content = content,
        audioPath = audioPath,
        styles = styles,
        youtubeUrl = youtubeUrl,
        summaryContent = summaryContent,
        isSummaryExpanded = isSummaryExpanded
    )

    // 503 에러 시 최대 3회 재시도 (exponential backoff)
    private suspend fun <T> retryOn503(block: suspend () -> T): T {
        val delays = longArrayOf(3_000, 6_000, 12_000)
        var lastException: Exception? = null
        // 첫 시도
        try {
            return block()
        } catch (e: Exception) {
            if (e.message?.contains("503") != true && e.message?.contains("UNAVAILABLE") != true && e.message?.contains("high demand") != true) {
                throw e
            }
            lastException = e
        }
        // 재시도
        for (delay in delays) {
            kotlinx.coroutines.delay(delay)
            try {
                return block()
            } catch (e: Exception) {
                if (e.message?.contains("503") != true && e.message?.contains("UNAVAILABLE") != true && e.message?.contains("high demand") != true) {
                    throw e
                }
                lastException = e
            }
        }
        throw lastException!!
    }

    private suspend fun summarizeVideoStream(
        videoId: String,
        videoUrl: String,
        style: SummaryStyle = SummaryStyle.SIMPLE,
        lang: String = "ko",
        onTitleFound: ((String) -> Unit)? = null,
        onStreamUpdate: ((String) -> Unit)? = null
    ): String {
        val prompt = buildYoutubePrompt(style, lang)
        // 1. 자막 + 제목 추출 시도
        val videoInfo = captionService.extractVideoInfo(videoId)
        if (videoInfo != null) {
            onTitleFound?.invoke(videoInfo.title)
        }
        val captions = videoInfo?.captions
        if (captions != null) {
            // 자막 기반 요약 — 스트리밍 (150ms 배칭으로 recomposition 최소화)
            val fullPrompt = "$prompt\n\n아래는 영상의 자막입니다:\n\n$captions"
            var result = ""
            val sb = StringBuilder()
            var lastEmit = 0L
            aiApiService.generateContentStream(fullPrompt, longOutput = true).collect { delta ->
                sb.append(delta)
                val now = System.currentTimeMillis()
                if (now - lastEmit >= 150) {
                    result = stripMarkdown(sb.toString())
                    onStreamUpdate?.invoke(result)
                    lastEmit = now
                }
            }
            result = stripMarkdown(sb.toString())
            onStreamUpdate?.invoke(result)
            if (result.isBlank()) throw me.pecos.memozy.data.datasource.remote.ai.AIException.UnknownException("Empty streaming response")
            return result
        }
        // 2. 자막 없으면 fallback — 비스트리밍 (영상 직접 분석은 스트리밍 미지원)
        return retryOn503 {
            aiApiService.generateContentWithVideo(
                prompt = prompt,
                videoUrl = videoUrl,
            )
        }
    }
}

private sealed class SummaryState {
    data object Idle : SummaryState()
    data object Loading : SummaryState()
    data class Streaming(val text: String) : SummaryState()
    data class Success(val text: String) : SummaryState()
    data class Error(val message: String) : SummaryState()
}
