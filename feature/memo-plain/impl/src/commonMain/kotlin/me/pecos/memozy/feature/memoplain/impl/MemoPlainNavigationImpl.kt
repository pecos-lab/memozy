package me.pecos.memozy.feature.memoplain.impl

import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.foundation.layout.Box
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
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.confirm
import me.pecos.memozy.feature.core.resource.generated.resources.login_prompt_message
import me.pecos.memozy.feature.core.resource.generated.resources.login_prompt_title
import me.pecos.memozy.feature.core.resource.generated.resources.ai_consent_title
import me.pecos.memozy.feature.core.resource.generated.resources.ai_consent_required_toast
import me.pecos.memozy.feature.core.viewmodel.settings.AiConsentKeys
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
import androidx.savedstate.read
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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
import me.pecos.memozy.platform.intent.AppPermission
import me.pecos.memozy.platform.intent.PermissionService
import me.pecos.memozy.platform.intent.PermissionStatus
import me.pecos.memozy.platform.intent.SharedContentReader
import me.pecos.memozy.platform.intent.ToastPresenter
import me.pecos.memozy.platform.intent.percentDecodeUtf8
import me.pecos.memozy.platform.media.AudioFileStore
import me.pecos.memozy.platform.media.AudioRecorder
import me.pecos.memozy.platform.media.MediaService
import me.pecos.memozy.presentation.components.rememberPermissionLauncher
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
    private val webScrapeService: me.pecos.memozy.data.datasource.remote.ai.WebScrapeService,
    private val analyticsService: me.pecos.memozy.platform.analytics.AnalyticsService,
    private val liveTranscriptionService: me.pecos.memozy.platform.transcription.LiveTranscriptionService,
) : MemoPlainNavigation {

    // 화면 dispose 후에도 살아있어야 하는 저장 작업용 (싱글톤 라이프타임)
    private val saveScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    // onSave/onAutoSave 직렬화 — 동시 INSERT/UPDATE race 방지
    private val saveMutex = Mutex()

    companion object {
        private const val FEATURE_YOUTUBE_SUMMARY = "youtube_summary"
        private const val FEATURE_WEB_SUMMARY = "web_summary"
        private const val FEATURE_TRANSCRIPTION = "transcription"
        private const val FEATURE_IMAGE_OCR = "image_ocr"
        private const val FEATURE_REWARD_AD = "reward_ad"
        private const val FEATURE_AI_ASSIST = "ai_assist"
        private const val MAX_DAILY_AD_VIEWS = 3
        private const val MAX_MEMO_CONTEXT_CHARS = 3000
        private const val MEMOZY_AI_SYSTEM_ROLE = "너는 Memozy AI야. 메모지 앱의 AI 어시스턴트로, 사용자의 메모 작성을 돕는 게 너의 역할이야. 너의 이름은 'Memozy AI'이고, 다른 AI 서비스의 이름으로 자신을 소개하면 안 돼. 사용자가 '너 누구야?' 등 정체를 물어볼 때만 'Memozy AI입니다! 메모 작성을 도와드릴게요' 라고 답해. 그 외에는 자기소개 없이 바로 답변해. 도입부 인사('도와드릴게요', '물론이죠', '네!' 등)는 생략하고 본문부터 시작하되, 톤은 친근하고 다정하게(반말은 쓰지 마). 단답으로 끊지 말고 충분히 살을 붙여 꼼꼼하게 설명해 — 정의 → 배경/원리 → 예시 → 관련 맥락 순으로 자연스럽게 풀어줘. 사용자가 더 깊이 알고 싶을 만한 포인트가 있으면 슬쩍 짚어주는 것도 좋아."
        private const val NO_MARKDOWN_RULE = "마크다운 문법(**, ##, - 등)을 절대 사용하지 마. 순수 텍스트로만 답해."

        // 메모 자체 조작 요청 시 AI 가 출력해야 하는 액션 명령 사양.
        // 일반 질문 (정보/설명/의견) 은 평소처럼 텍스트로 답하고, 메모를 직접 수정하라는
        // 요청에만 액션 명령을 단독으로 출력한다.
        private val MEMOZY_AI_ACTION_RULES = """
사용자가 메모 자체를 수정/조작하라고 요청하면 일반 텍스트 답변 대신 아래 액션 명령 중 하나만 정확한 형식으로, 다른 부가 텍스트 없이 출력해.

지원 액션:
[ACTION:CLEAR]                    — 본문 비우기
[ACTION:BOLD_ALL]                 — 본문 전체 굵게
[ACTION:ITALIC_ALL]               — 본문 전체 이탤릭
[ACTION:UNDERLINE_ALL]            — 본문 전체 밑줄
[ACTION:REPLACE]
새 본문 내용 (여러 줄 가능)
[/ACTION]                         — 본문을 통째로 교체 (번역/다듬기/정리 등)
[ACTION:APPEND]
끝에 추가할 내용
[/ACTION]                         — 본문 끝에 새 내용 덧붙이기

요청 → 액션 매핑 예시:
"메모 지워줘" → [ACTION:CLEAR]
"전체 볼드로 만들어줘" → [ACTION:BOLD_ALL]
"이거 영어로 번역해서 본문 바꿔줘" → [ACTION:REPLACE] + (번역된 영어 본문) + [/ACTION]
"오타 다듬어줘" → [ACTION:REPLACE] + (다듬은 본문) + [/ACTION]
"맨 끝에 '내일 회의' 한 줄 추가" → [ACTION:APPEND] + 내일 회의 + [/ACTION]

단, 일반 질문/설명/의견 요청에는 액션 명령을 쓰지 말고 평소처럼 친근한 텍스트로 답해.
            """.trimIndent()

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
                    |[한줄 요약]
                    |영상의 핵심을 한 문장으로 요약
                    |
                    |[키워드]
                    |#키워드1 #키워드2 #키워드3
                    |
                    |[핵심 내용]
                    |- 첫 번째 핵심 내용
                    |
                    |- 두 번째 핵심 내용
                    |
                    |- 세 번째 핵심 내용
                    |
                    |(가장 중요한 내용 3~5개를 각 1줄로 정리. 첫 항목은 [핵심 내용] 바로 다음 줄에, 이후 항목들 사이에는 빈 줄 1개를 넣어줘)
                """.trimMargin()
                SummaryStyle.DETAILED -> """
                    |이 유튜브 영상을 ${l} 상세하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
                    |
                    |[한줄 요약]
                    |영상의 핵심을 한 문장으로 요약
                    |
                    |[핵심 키워드]
                    |#키워드1 #키워드2 #키워드3 #키워드4 #키워드5
                    |
                    |[타임라인별 상세 요약]
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
                    |[핵심 인사이트]
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
                    |【번역 원칙 — 매우 중요】
                    |- 자막은 음성을 전사한 것일 뿐이라 직역하면 어색하다. 자막의 단어를 그대로 옮기지 말고,
                    |  화자가 진짜 전달하려는 의미를 ${userLang} 사용자에게 자연스럽게 전달해라.
                    |- 관용 표현·구어·줄임말은 ${userLang}의 자연스러운 대응 표현으로 의역해라.
                    |- 문맥상 생략된 주어·목적어가 있으면 ${userLang} 번역에서 보완해 자연스럽게 만들어라.
                    |- 원어가 짧다고 번역도 짧을 필요 없다. 의미가 잘 전달되는 길이가 우선.
                    |- 직역 같은 부자연스러운 표현(예: "그것을 하다", "이것을 봐주세요" 같은 기계 번역체) 금지.
                    |
                    |[핵심 표현 (10~15개)]
                    |
                    |영상에서 나온 외국어 표현을 원문 그대로 적고, 바로 다음 줄에 ${userLang} 뜻을 자연스럽게 적어.
                    |단어 단위 직역이 아니라 ${userLang} 화자가 같은 상황에서 실제로 쓸 표현으로.
                    |표현과 표현 사이에는 빈 줄을 넣어.
                    |
                    |외국어 원문
                    |${userLang} 뜻 (자연스러운 의역)
                    |
                    |외국어 원문
                    |${userLang} 뜻 (자연스러운 의역)
                    |
                    |[주요 문장 (5~10개)]
                    |
                    |영상에서 나온 핵심 문장을 원문 그대로 적고, 다음 줄에 ${userLang} 해석을 적어.
                    |해석은 자막 직역이 아니라, 그 문장이 영상 흐름에서 어떤 의미인지 반영한 자연스러운 번역으로.
                    |각 문장 앞에 * 를 붙여. 문장과 문장 사이에는 빈 줄을 넣어.
                    |
                    |* 외국어 원문 문장
                    |${userLang} 해석 (자연스러운 번역)
                    |
                    |* 외국어 원문 문장
                    |${userLang} 해석 (자연스러운 번역)
                    |
                    |[학습 포인트]
                    |
                    |문법, 뉘앙스, 직역과 의역의 차이, 발음, 사용 상황 등 학습에 도움되는 내용을 ${userLang}로 2~4줄 정리.
                    |특히 위에서 의역한 표현 중 직역과 큰 차이가 있는 것이 있다면 왜 그렇게 번역했는지 설명해.
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
            val style = when (mode) {
                SummaryMode.SIMPLE -> SummaryStyle.SIMPLE
                SummaryMode.DETAILED -> SummaryStyle.DETAILED
            }
            return buildWebPrompt(style, lang)
        }

        private fun buildWebPrompt(style: SummaryStyle, lang: String): String {
            val l = langInstruction(lang)
            return when (style) {
                SummaryStyle.SIMPLE -> """
                    |이 웹페이지 내용을 ${l} 간결하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
                    |
                    |[한줄 요약]
                    |페이지의 핵심을 한 문장으로 요약
                    |
                    |[키워드]
                    |#키워드1 #키워드2 #키워드3
                    |
                    |[핵심 내용]
                    |- 가장 중요한 내용 3~5개를 각 1줄로 정리
                """.trimMargin()
                SummaryStyle.DETAILED -> """
                    |이 웹페이지 내용을 ${l} 상세하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
                    |
                    |[한줄 요약]
                    |페이지의 핵심을 한 문장으로 요약
                    |
                    |[핵심 키워드]
                    |#키워드1 #키워드2 #키워드3 #키워드4 #키워드5
                    |
                    |[섹션별 상세 요약]
                    |각 섹션/주제별로 나눠서 아래처럼 정리해줘:
                    |
                    |[섹션 제목]
                    |- 상세 설명 (2~3문장)
                    |- 중요한 내용이나 인사이트
                    |
                    |(모든 섹션을 빠짐없이 정리)
                    |
                    |[핵심 인사이트]
                    |- 이 글에서 얻을 수 있는 주요 인사이트를 정리
                """.trimMargin()
                SummaryStyle.NOTE -> """
                    |다음 웹페이지 내용을 ${l} 학습 노트 형태로 정리해줘. 인사말이나 부가 설명 없이 바로 시작해:
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
                    |다음 웹페이지 내용을 언어 학습용으로 정리해줘.
                    |사용자의 모국어는 ${userLang}이고, 페이지에 나오는 외국어를 학습하려는 목적이야.
                    |절대 #, ##, **, *** 같은 마크다운 서식을 사용하지 마. 이모지와 일반 텍스트만 써.
                    |인사말이나 부가 설명 없이 바로 아래 형식대로만 출력해:
                    |
                    |[핵심 표현 (10~15개)]
                    |
                    |페이지에서 나온 외국어 표현을 원문 그대로 적고, 바로 다음 줄에 ${userLang} 뜻을 자연스럽게 적어.
                    |단어 단위 직역이 아니라 ${userLang} 화자가 같은 상황에서 실제로 쓸 표현으로.
                    |표현과 표현 사이에는 빈 줄을 넣어.
                    |
                    |외국어 원문
                    |${userLang} 뜻 (자연스러운 의역)
                    |
                    |[주요 문장 (5~10개)]
                    |
                    |페이지에서 나온 핵심 문장을 원문 그대로 적고, 다음 줄에 ${userLang} 해석을 적어.
                    |각 문장 앞에 * 를 붙여. 문장과 문장 사이에는 빈 줄을 넣어.
                    |
                    |* 외국어 원문 문장
                    |${userLang} 해석 (자연스러운 번역)
                    |
                    |[학습 포인트]
                    |
                    |문법, 뉘앙스, 직역과 의역의 차이, 사용 상황 등 학습에 도움되는 내용을 ${userLang}로 2~4줄 정리.
                """.trimMargin()
                }
            }
        }
    }

    @OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onNavigateToHome: () -> Unit,
        onBack: () -> Unit,
        onNavigateToSubscription: () -> Unit,
    ) {
        navGraphBuilder.composable(
            MemoPlainRoute.MEMO,
            enterTransition = { fadeIn(tween(150)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { fadeOut(tween(150)) }
        ) { backStackEntry ->
            val memoIdStr = backStackEntry.arguments?.read { getStringOrNull("memoId") } ?: ""
            val isShared = memoIdStr.startsWith("shared_")
            val isSharedImage = memoIdStr.startsWith("shared_image_")
            val isSharedPdf = memoIdStr.startsWith("shared_pdf_")
            val memoId = memoIdStr.toIntOrNull() ?: -1

            // 공유 텍스트를 route parameter에서 디코딩
            val sharedText = remember {
                if (isShared && !isSharedImage && !isSharedPdf) {
                    try {
                        percentDecodeUtf8(memoIdStr.removePrefix("shared_"))
                    } catch (e: Exception) { "" }
                } else ""
            }

            // 이미지/PDF URI 디코딩 — 플랫폼 무관 문자열로 보관.
            val sharedFileUri: String? = remember {
                if (isSharedImage || isSharedPdf) {
                    try {
                        val prefix = if (isSharedImage) "shared_image_" else "shared_pdf_"
                        percentDecodeUtf8(memoIdStr.removePrefix(prefix))
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

            // AI 데이터 전송 동의 — 거부 상태면 AI 기능 자체를 차단하고 안내 다이얼로그.
            // 매번 prefs 를 읽어서 다른 화면에서 토글되더라도 다음 진입 시 반영되게 함.
            var consentGiven by remember {
                mutableStateOf(preferencesProvider.getBoolean(AiConsentKeys.GIVEN, false))
            }
            var showConsentRequired by remember { mutableStateOf(false) }

            // AI 사용량 체크 (티어별 일일 한도)
            val subscriptionTier = LocalSubscriptionTier.current
            val rewardAdProvider = LocalRewardAdProvider.current
            var dailyUsageCount by remember { mutableStateOf(0) }
            var dailyAdViewCount by remember { mutableStateOf(0) }
            var adBonusCount by remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                val total = aiUsageDao.getTotalCountSince(startOfToday())
                dailyAdViewCount = aiUsageDao.getCountSince(FEATURE_REWARD_AD, startOfToday())
                // ai_usage 에 광고 시청 기록도 들어가므로 실제 AI 사용량은 total - 광고시청 횟수.
                dailyUsageCount = total - dailyAdViewCount
                // adBonusCount 는 in-memory 라 화면 재진입 시 사라지지만 광고 시청은 DB 에 남으므로
                // 광고 본 횟수만큼 보너스를 복원해서 일관성 유지.
                adBonusCount = dailyAdViewCount
                // 화면 재진입 시 동의 상태도 다시 읽음 (AiConsentGate 가 새로 결정되었을 수 있음).
                consentGiven = preferencesProvider.getBoolean(AiConsentKeys.GIVEN, false)
            }
            val dailyLimit = subscriptionTier.dailyAiLimit + adBonusCount
            val canUseAiQuota = dailyUsageCount < dailyLimit
            val canUseAi = isLoggedIn && consentGiven && canUseAiQuota
            val canWatchAd = !subscriptionTier.isPro && dailyAdViewCount < MAX_DAILY_AD_VIEWS
            val remainingAdViews = MAX_DAILY_AD_VIEWS - dailyAdViewCount
            var showLimitBottomSheet by remember { mutableStateOf(false) }
            val notifyAiBlocked: () -> Unit = {
                when {
                    !isLoggedIn -> showLoginPrompt = true
                    !consentGiven -> showConsentRequired = true
                    else -> showLimitBottomSheet = true
                }
                analyticsService.logEvent(
                    me.pecos.memozy.platform.analytics.AnalyticsEvents.AI_LIMIT_REACHED,
                )
            }

            // AI 사용 1회 차감. 화면 재진입 전까지 카운터 sync 를 위해 in-memory 도 같이 +1.
            val consumeAiQuota: (String) -> Unit = { feature ->
                dailyUsageCount++
                scope.launch { aiUsageDao.insert(AiUsage(feature = feature)) }
            }

            // 이미지 OCR 처리
            var imageOcrState by remember { mutableStateOf<SummaryState>(SummaryState.Idle) }
            val sharedContentReader: SharedContentReader = koinInject()

            LaunchedEffect(sharedFileUri, isSharedImage) {
                if (isSharedImage && sharedFileUri != null && imageOcrState is SummaryState.Idle) {
                    if (!canUseAi) {
                        imageOcrState = SummaryState.Error("오늘 AI 사용 한도를 모두 사용했어요.")
                        return@LaunchedEffect
                    }
                    imageOcrState = SummaryState.Loading
                    try {
                        val bytes = sharedContentReader.readBytes(sharedFileUri)
                            ?: throw Exception("Cannot read image")
                        @OptIn(ExperimentalEncodingApi::class)
                        val base64 = Base64.Default.encode(bytes)
                        val mimeType = sharedContentReader.getMimeType(sharedFileUri) ?: "image/jpeg"
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
                            summaryContent = it.summaryContent,
                            isSummaryExpanded = it.isSummaryExpanded,
                            webUrl = it.webUrl,
                            recordingTranscript = it.recordingTranscript
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
            // 녹음 끝난 직후 사용자에게 "저장/닫기" 결정 받기 위한 pending state.
            // 파일은 이미 permanent 로 옮겨져 안전 — 닫기 시에만 삭제.
            var pendingAudioPath by remember { mutableStateOf<String?>(null) }
            var pendingAudioDurationSeconds by remember { mutableStateOf(0L) }
            // 녹음 정지 후에도 라이브 카드를 카드 형태로 유지 — 헤더가 제목, 본문이 녹음 텍스트.
            var recordedCardTitle by remember { mutableStateOf<String?>(null) }
            var recordedCardText by remember { mutableStateOf<String?>(null) }
            // 번역 녹음 — 소스/타겟 언어 선택. null 이면 일반 녹음.
            var showTranslationDialog by remember { mutableStateOf(false) }
            var translationSourceLang by remember { mutableStateOf<String?>(null) }
            var translationTargetLang by remember { mutableStateOf<String?>(null) }
            // 녹음 시작 시 사용할 언어 — 번역 녹음이면 소스 언어, 아니면 앱 언어.
            // beginRecording 안에서 매번 갱신해서 일반/번역 흐름 분기.
            var activeRecordingSourceLang by remember { mutableStateOf<String?>(null) }
            var activeRecordingTargetLang by remember { mutableStateOf<String?>(null) }
            // 메모 다시 열 때 entity 의 recordingTranscript 가 있으면 카드 복원.
            // 제목은 메모의 createdAt 으로 stamp 생성 (별도 필드 없이 자연스럽게).
            LaunchedEffect(finalMemo.id, finalMemo.recordingTranscript) {
                try {
                    if (!finalMemo.recordingTranscript.isNullOrBlank() && recordedCardText.isNullOrBlank()) {
                        recordedCardText = finalMemo.recordingTranscript
                        val createdAt = if (finalMemo.createdAt > 0) finalMemo.createdAt else Clock.System.now().toEpochMilliseconds()
                        val nowLocal = kotlin.time.Instant.fromEpochMilliseconds(createdAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        fun Int.pad2(): String = toString().padStart(2, '0')
                        val yy = (nowLocal.year % 100).pad2()
                        val stamp = "$yy.${nowLocal.monthNumber.pad2()}.${nowLocal.dayOfMonth.pad2()} ${nowLocal.hour.pad2()}:${nowLocal.minute.pad2()}"
                        recordedCardTitle = "$stamp 녹음"
                    }
                } catch (e: Throwable) {
                    // 복원 실패해도 화면은 죽으면 안 됨 — 단순 폴백
                    recordedCardText = finalMemo.recordingTranscript
                    recordedCardTitle = "녹음"
                }
            }
            // Web summary busy — 가드 검사용으로 다른 busy state 와 같은 위치에 선언
            var isWebSummarizing by remember { mutableStateOf(false) }

            val toastPresenter: ToastPresenter = koinInject()
            // AI 기능 중복 실행 가드 — 어느 하나라도 진행 중이면 다른 entry point 차단
            val isAnyAiBusy = isRecording || isTranscribing || isWebSummarizing ||
                inlineSummaryState is SummaryState.Loading ||
                inlineSummaryState is SummaryState.Streaming ||
                isAiAssistLoading
            val notifyAiBusy: () -> Unit = {
                toastPresenter.show("다른 AI 작업이 진행 중이에요. 잠시 후 다시 시도해주세요.")
            }
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
            val permissionService: PermissionService = koinInject()
            var audioRecorder by remember { mutableStateOf<AudioRecorder?>(null) }
            var recordingStartTime by remember { mutableStateOf(0L) }
            val audioCachePath = remember { audioFileStore.cachePath("recording.m4a") }

            // Live STT 실시간 텍스트 — 녹음 중 화면에 즉시 반영
            val livePartialText by liveTranscriptionService.partialText.collectAsState()
            val liveConfirmedText by liveTranscriptionService.confirmedText.collectAsState()

            fun beginRecording() {
                try {
                    // LiveTranscriptionService 가 마이크 단독 소유 + 파일 저장 + 실시간 STT 통합.
                    // iOS AVAudioEngine, Android AudioRecord+SpeechRecognizer(pipe) 패턴 동일.
                    // AudioRecorder.start() 는 양 플랫폼 모두 Noop — mic 충돌 회피.
                    val recorder = mediaService.createAudioRecorder()
                    recorder.start(audioCachePath)
                    audioRecorder = recorder
                    recordingStartTime = Clock.System.now().toEpochMilliseconds()
                    isRecording = true
                    transcriptionError = null
                    // 새 녹음 시작 시 이전 녹음 카드 초기화
                    recordedCardTitle = null
                    recordedCardText = null
                    val effectiveLang = activeRecordingSourceLang ?: languageCode
                    scope.launch {
                        liveTranscriptionService.start(effectiveLang, audioCachePath)
                    }
                } catch (e: Exception) {
                    transcriptionError = "녹음을 시작할 수 없어요."
                }
            }

            val launchRecordPermission = rememberPermissionLauncher(
                permission = AppPermission.RECORD_AUDIO,
            ) { granted ->
                if (granted) beginRecording()
                else transcriptionError = "마이크 권한이 필요해요."
            }

            fun startRecording() {
                if (isAnyAiBusy) {
                    notifyAiBusy()
                    return
                }
                if (!canUseAi) {
                    notifyAiBlocked()
                    return
                }
                transcriptionResult = null
                if (permissionService.status(AppPermission.RECORD_AUDIO) == PermissionStatus.GRANTED) {
                    beginRecording()
                } else {
                    launchRecordPermission()
                }
            }

            fun stopRecordingAndTranscribe() {
                // Live STT 정지 — 마지막 partial 이 confirmed 로 flush 됨.
                liveTranscriptionService.stop()
                try {
                    audioRecorder?.apply { stop(); release() }
                } catch (_: Exception) { }
                val durationSeconds = (Clock.System.now().toEpochMilliseconds() - recordingStartTime) / 1000
                audioRecorder = null
                isRecording = false

                // 녹음 도중 다른 AI 사용으로 한도 초과되면 transcription 차단.
                if (!canUseAi) {
                    audioFileStore.delete(audioCachePath)
                    notifyAiBlocked()
                    return
                }

                isTranscribing = true
                scope.launch {
                    try {
                        // 1) 서비스가 인코더 EOS 처리하고 파일을 닫을 시간 + 마지막 STT 결과가 도착할 시간을 대기.
                        // 길게 잡되, confirmed text 가 도착하면 일찍 끝낼 수 있음.
                        var waited = 0
                        while (waited < 2000 && liveTranscriptionService.confirmedText.value.isBlank()) {
                            kotlinx.coroutines.delay(100)
                            waited += 100
                        }
                        // 마지막 partial → confirmed flush 시간 추가 100ms
                        kotlinx.coroutines.delay(150)

                        // 카드에 보였던 모든 텍스트가 본문에 들어가도록 confirmed + partial 둘 다 머지.
                        // SR 이 cancel 되는 시점 race 로 마지막 partial 이 confirmed 로 flush 안 된 케이스 대비.
                        val confirmed = liveTranscriptionService.confirmedText.value.trim()
                        val partial = liveTranscriptionService.partialText.value.trim()
                        val liveText = when {
                            confirmed.isEmpty() -> partial
                            partial.isEmpty() -> confirmed
                            partial.startsWith(confirmed) -> partial  // partial 이 confirmed 의 연속이면 partial 만
                            else -> "$confirmed $partial"
                        }.trim()
                        val fileExists = audioFileStore.exists(audioCachePath) && audioFileStore.length(audioCachePath) >= 1024

                        if (liveText.isNotBlank()) {
                            // iOS / API 33+ Android — Live STT 결과 그대로 사용. Gemini fabrication 회피.
                            // 번역 모드면 STT 결과를 Gemini 로 번역해서 [원문 \n\n 번역] 합쳐서 표시.
                            val sourceLangSnap = activeRecordingSourceLang
                            val targetLangSnap = activeRecordingTargetLang
                            val isTranslation = sourceLangSnap != null && targetLangSnap != null && sourceLangSnap != targetLangSnap
                            val result = if (isTranslation) {
                                try {
                                    val targetName = languageDisplayName(targetLangSnap!!)
                                    val translation = retryOn503 {
                                        aiApiService.generateContent(
                                            "Translate the following text to $targetName. " +
                                                "Output ONLY the translation as plain text — no quotes, no explanations, no language tags.\n\n" +
                                                "Text:\n$liveText"
                                        )
                                    }.trim().trim('"', '\'', '`').trim()
                                    if (translation.isNotBlank()) {
                                        "$liveText\n\n[${languageDisplayName(targetLangSnap)}]\n$translation"
                                    } else liveText
                                } catch (e: Throwable) {
                                    transcriptionError = "번역 실패: ${e.message ?: e::class.simpleName}"
                                    liveText
                                }
                            } else liveText

                            val nowLocal = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            fun Int.pad2(): String = toString().padStart(2, '0')
                            val yy = (nowLocal.year % 100).pad2()
                            val stamp = "$yy.${nowLocal.monthNumber.pad2()}.${nowLocal.dayOfMonth.pad2()} ${nowLocal.hour.pad2()}:${nowLocal.minute.pad2()}"

                            if (fileExists) {
                                val safeFileName = "$stamp 녹음".replace(":", "-").replace("/", "-")
                                val permanentPath = audioFileStore.permanentPath(safeFileName)
                                audioFileStore.copy(audioCachePath, permanentPath)
                                audioFileStore.delete(audioCachePath)
                                savedAudioPath = permanentPath
                                pendingAudioPath = permanentPath
                                pendingAudioDurationSeconds = durationSeconds
                            }

                            // 카드 영구 표시: 헤더는 번역 모드면 "번역", 일반이면 "녹음".
                            val typeLabel = if (isTranslation) "번역" else "녹음"
                            recordedCardTitle = "$stamp $typeLabel"
                            recordedCardText = result

                            transcriptionResult = result
                            transcriptionError = null
                            consumeAiQuota(FEATURE_TRANSCRIPTION)
                        } else if (fileExists) {
                            // Live STT 가 비어있을 때만 (API < 33 / SpeechRecognizer 미가용) Gemini fallback.
                            val audioBytes = audioFileStore.readBytes(audioCachePath)
                            @OptIn(ExperimentalEncodingApi::class)
                            val base64 = Base64.Default.encode(audioBytes)
                            val resultRaw = aiApiService.transcribeAudio(base64, "audio/mp4", durationSeconds)
                            val result = resultRaw.trim().trim('"', '\'', '`').trim()
                            // Gemini 가 빈/짧은 오디오 받으면 transcribe prompt 를 그대로 echo 하는 케이스가 있어
                            // 카드에 prompt 문구가 노출되던 버그 차단. 한국어 prompt 의 흔한 토큰을 폭넓게 검사.
                            val looksLikePromptEcho = result.contains("받아쓰기")
                                || result.contains("텍스트만 출력")
                                || result.contains("다른 설명은 하지")
                                || result.contains("다른설명은 하지")
                                || result.contains("오디오를")
                            if (looksLikePromptEcho || result.isBlank()) {
                                transcriptionError = "음성이 감지되지 않았어요. 다시 시도해주세요."
                                transcriptionResult = null
                                audioFileStore.delete(audioCachePath)
                            } else {
                                val nowLocal = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                fun Int.pad2(): String = toString().padStart(2, '0')
                                val yy = (nowLocal.year % 100).pad2()
                                val stamp = "$yy.${nowLocal.monthNumber.pad2()}.${nowLocal.dayOfMonth.pad2()} ${nowLocal.hour.pad2()}:${nowLocal.minute.pad2()}"
                                val safeFileName = "$stamp 녹음".replace(":", "-").replace("/", "-")
                                val permanentPath = audioFileStore.permanentPath(safeFileName)
                                audioFileStore.copy(audioCachePath, permanentPath)
                                audioFileStore.delete(audioCachePath)
                                savedAudioPath = permanentPath
                                pendingAudioPath = permanentPath
                                pendingAudioDurationSeconds = durationSeconds

                                // 카드 영구 표시 (Gemini fallback 경로도 동일 처리)
                                recordedCardTitle = "$stamp 녹음"
                                recordedCardText = result

                                transcriptionResult = result
                                transcriptionError = null
                                consumeAiQuota(FEATURE_TRANSCRIPTION)
                            }
                        } else {
                            transcriptionError = "녹음이 너무 짧아요. 다시 시도해주세요."
                            audioFileStore.delete(audioCachePath)
                        }
                    } catch (e: Exception) {
                        transcriptionError = "음성 변환에 실패했어요. (${e::class.simpleName}: ${e.message})"
                        audioFileStore.delete(audioCachePath)
                    } finally {
                        isTranscribing = false
                    }
                }
            }

            // 웹 요약 상태 (isWebSummarizing 은 위쪽에 선언됨 — 가드용)
            var webSummaryResult by remember { mutableStateOf<String?>(null) }
            var webSummaryError by remember { mutableStateOf<String?>(null) }
            var webPageTitle by remember { mutableStateOf<String?>(null) }

            // 양식 선택 상태
            var activeSummaryStyle by remember { mutableStateOf(SummaryStyle.SIMPLE) }

            // 현재 진행 중인 요약 Job (취소용)
            var currentSummarizeJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

            // 자동저장용 memoId 추적 (새 메모 insert 후 update로 전환)
            var savedMemoId by remember { mutableStateOf(memoId) }

            // 시스템 뒤로가기 / 제스처 백 → 좌상단 화살표와 동일하게 onBack(=홈으로 pop) 으로 위임.
            // 기본 NavController pop 은 직전 라우트(설정 등)로 가버려서 메모 → 홈 보장이 안 됨.
            BackHandler { onBack() }

            MemoScreen(
                existingMemo = finalMemo,
                onBack = onBack,
                onAutoSave = { memo ->
                    saveScope.launch {
                        saveMutex.withLock {
                            val memoWithAudio = memo.copy(audioPath = savedAudioPath ?: memo.audioPath)
                            if (savedMemoId > 0) {
                                repository.updateMemo(memoWithAudio.copy(id = savedMemoId).toEntity())
                            } else if (memoWithAudio.name.isNotBlank()
                                || memoWithAudio.content.isNotBlank()
                                || memoWithAudio.summaryContent != null
                                || memoWithAudio.youtubeUrl != null
                                || memoWithAudio.webUrl != null
                                || !memoWithAudio.recordingTranscript.isNullOrBlank()
                                || memoWithAudio.audioPath != null
                            ) {
                                val newId = repository.addMemo(memoWithAudio.toEntity())
                                savedMemoId = newId.toInt()
                            }
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
                        // 일반 녹음: 번역 모드 해제.
                        activeRecordingSourceLang = null
                        activeRecordingTargetLang = null
                        startRecording()
                    }
                },
                onStopRecording = { stopRecordingAndTranscribe() },
                // onStartRecording 진입(마이크 버튼) — 일반 녹음이므로 번역 lang 초기화.
                // 번역 녹음은 별도로 onOpenTranslationDialog → 확인 시 startRecording 직접 호출.
                isRecording = isRecording,
                isTranscribing = isTranscribing,
                transcriptionResult = transcriptionResult,
                transcriptionError = transcriptionError,
                livePartialText = livePartialText,
                liveConfirmedText = liveConfirmedText,
                recordedCardTitle = recordedCardTitle,
                recordedCardText = recordedCardText,
                onOpenTranslationDialog = {
                    // 기본값: "내 언어" = 앱 설정 언어 (자동), "번역할 언어" = 비어있음 (사용자 선택)
                    translationSourceLang = languageCode
                    translationTargetLang = null
                    showTranslationDialog = true
                },
                onDismissRecordedCard = {
                    recordedCardTitle = null
                    recordedCardText = null
                },
                onUpdateRecordedCardText = { newText ->
                    recordedCardText = newText
                },
                audioPath = savedAudioPath,
                pendingAudioPath = pendingAudioPath,
                pendingAudioDurationSeconds = pendingAudioDurationSeconds,
                onSaveRecording = {
                    pendingAudioPath = null
                },
                onDiscardRecording = {
                    val path = pendingAudioPath
                    if (path != null) {
                        scope.launch { audioFileStore.delete(path) }
                    }
                    savedAudioPath = null
                    pendingAudioPath = null
                },
                onCancelSummarize = {
                    currentSummarizeJob?.cancel()
                    currentSummarizeJob = null
                    inlineSummaryState = SummaryState.Idle
                    isWebSummarizing = false
                    isTranscribing = false
                },
                onWebSummarize = { url, mode ->
                    if (isAnyAiBusy) {
                        notifyAiBusy()
                        return@MemoScreen
                    }
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
                                consumeAiQuota(FEATURE_WEB_SUMMARY)
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
                onWebSummaryStyleSelected = { style, url ->
                    if (isAnyAiBusy) {
                        notifyAiBusy()
                        return@MemoScreen
                    }
                    if (!canUseAi) {
                        notifyAiBlocked()
                        return@MemoScreen
                    }
                    analyticsService.logEvent(
                        me.pecos.memozy.platform.analytics.AnalyticsEvents.WEB_SUMMARY_REQUESTED,
                        mapOf(me.pecos.memozy.platform.analytics.AnalyticsParams.SUMMARY_STYLE to style.name),
                    )
                    currentSummarizeJob?.cancel()
                    currentSummarizeJob = scope.launch {
                        isWebSummarizing = true
                        webSummaryError = null
                        try {
                            val content = webScrapeService.scrapeWebPage(url)
                            if (content == null) {
                                webSummaryError = "웹페이지를 불러올 수 없어요."
                            } else {
                                webPageTitle = content.title
                                val webPrompt = buildWebPrompt(style, languageCode)
                                val summary = retryOn503 {
                                    aiApiService.generateContent(
                                        "$webPrompt\n\n아래는 웹페이지 내용입니다:\n\n${content.text}"
                                    )
                                }
                                webSummaryResult = summary
                                consumeAiQuota(FEATURE_WEB_SUMMARY)
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
                onSummaryStyleSelected = { style, url ->
                    activeSummaryStyle = style
                    analyticsService.logEvent(
                        me.pecos.memozy.platform.analytics.AnalyticsEvents.YOUTUBE_SUMMARY_REQUESTED,
                        mapOf(me.pecos.memozy.platform.analytics.AnalyticsParams.SUMMARY_STYLE to style.name),
                    )
                    // 바텀시트에서 양식 선택 시 즉시 요약 실행
                    currentSummarizeJob?.cancel()
                    currentSummarizeJob = scope.launch {
                        if (isAnyAiBusy) {
                            notifyAiBusy()
                            return@launch
                        }
                        if (!canUseAi) {
                            notifyAiBlocked()
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
                            consumeAiQuota(FEATURE_YOUTUBE_SUMMARY)
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
                        if (isAnyAiBusy) {
                            notifyAiBusy()
                            return@launch
                        }
                        if (!canUseAi) {
                            notifyAiBlocked()
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
                            consumeAiQuota(FEATURE_YOUTUBE_SUMMARY)
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
                    saveScope.launch {
                        saveMutex.withLock {
                            val memoWithAudio = memo.copy(audioPath = savedAudioPath ?: memo.audioPath)
                            if (savedMemoId > 0) {
                                repository.updateMemo(memoWithAudio.copy(id = savedMemoId).toEntity())
                            } else if (memoId > 0 && existingMemo != null) {
                                repository.updateMemo(memoWithAudio.toEntity())
                            } else {
                                val newId = repository.addMemo(memoWithAudio.toEntity()).toInt()
                                savedMemoId = newId
                            }
                        }
                        withContext(Dispatchers.Main) {
                            onNavigateToHome()
                        }
                    }
                },
                onDelete = if (memoId > 0) { id ->
                    scope.launch {
                        repository.deleteMemo(id)
                        analyticsService.logEvent(
                            me.pecos.memozy.platform.analytics.AnalyticsEvents.MEMO_DELETED,
                        )
                        onNavigateToHome()
                    }
                } else null,
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
                    if (isAnyAiBusy) {
                        notifyAiBusy()
                        return@MemoScreen
                    }
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
                            val prompt = buildString {
                                appendLine(MEMOZY_AI_SYSTEM_ROLE)
                                appendLine("사용자가 현재 메모를 작성 중이니, 메모 내용이 있으면 자연스럽게 참고해서 답해줘.")
                                appendLine("메모와 관련 없는 질문이어도 자유롭게 답변하고, 살을 충분히 붙여 친근하게 풀어줘. $NO_MARKDOWN_RULE")
                                appendLine()
                                appendLine(MEMOZY_AI_ACTION_RULES)
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
                            kotlinx.coroutines.yield()
                            kotlinx.coroutines.delay(50)
                            aiAssistStreamingText = null
                            consumeAiQuota(FEATURE_AI_ASSIST)
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

            if (showConsentRequired) {
                AlertDialog(
                    onDismissRequest = { showConsentRequired = false },
                    title = { Text(stringResource(Res.string.ai_consent_title)) },
                    text = { Text(stringResource(Res.string.ai_consent_required_toast)) },
                    confirmButton = {
                        TextButton(onClick = { showConsentRequired = false }) {
                            Text(stringResource(Res.string.confirm))
                        }
                    }
                )
            }

            if (showTranslationDialog) {
                me.pecos.memozy.presentation.screen.memo.components.TranslationLanguageDialog(
                    sourceLang = translationSourceLang,
                    targetLang = translationTargetLang,
                    onSourceLangSelected = { translationSourceLang = it },
                    onTargetLangSelected = { translationTargetLang = it },
                    onDismiss = { showTranslationDialog = false },
                    onConfirm = {
                        activeRecordingSourceLang = translationSourceLang
                        activeRecordingTargetLang = translationTargetLang
                        showTranslationDialog = false
                        if (!canUseAi) notifyAiBlocked() else startRecording()
                    },
                )
            }

            if (showLimitBottomSheet) {
                val isAdPlatformSupported = rewardAdProvider?.isPlatformSupported != false
                AiLimitBottomSheet(
                    subscriptionTier = subscriptionTier,
                    canWatchAd = canWatchAd && isAdPlatformSupported,
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
                    onUpgrade = {
                        showLimitBottomSheet = false
                        onNavigateToSubscription()
                    },
                    onDismiss = { showLimitBottomSheet = false },
                    isPlatformSupported = isAdPlatformSupported,
                )
            }
        }
    }

    private fun languageDisplayName(code: String): String = when (code) {
        "ko" -> "한국어"
        "en" -> "영어"
        "ja" -> "일본어"
        "zh" -> "중국어"
        else -> code
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
        isSummaryExpanded = isSummaryExpanded,
        webUrl = webUrl,
        recordingTranscript = recordingTranscript
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
                val now = Clock.System.now().toEpochMilliseconds()
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
