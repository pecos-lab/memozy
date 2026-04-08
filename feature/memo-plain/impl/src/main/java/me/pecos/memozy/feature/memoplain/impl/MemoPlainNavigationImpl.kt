package me.pecos.memozy.feature.memoplain.impl

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.core.content.ContextCompat
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
import me.pecos.memozy.data.datasource.local.AiUsageDao
import me.pecos.memozy.data.datasource.local.YoutubeSummaryDao
import me.pecos.memozy.data.datasource.local.entity.AiUsage
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.entity.YoutubeSummary
import java.util.Calendar
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
    private val captionService: YouTubeCaptionService,
    private val aiUsageDao: AiUsageDao,
    private val tagDao: me.pecos.memozy.data.datasource.local.TagDao,
    private val webScrapeService: me.pecos.memozy.data.datasource.remote.ai.WebScrapeService
) : MemoPlainNavigation {

    private suspend fun autoTag(memoId: Int, tagName: String) {
        if (memoId <= 0) return
        // 태그가 없으면 생성
        val allTags = tagDao.getAllTagsOnce()
        val tag = allTags.firstOrNull { it.name == tagName }
            ?: run {
                val newId = tagDao.insertTag(me.pecos.memozy.data.datasource.local.entity.Tag(name = tagName))
                me.pecos.memozy.data.datasource.local.entity.Tag(id = newId.toInt(), name = tagName)
            }
        tagDao.insertMemoTag(me.pecos.memozy.data.datasource.local.entity.MemoTag(memoId = memoId, tagId = tag.id))
    }

    companion object {
        private const val FEATURE_YOUTUBE_SUMMARY = "youtube_summary"
        private const val DAILY_FREE_LIMIT = 100

        private fun startOfToday(): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        private val YOUTUBE_REGEX = Regex(
            """(?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)[\w-]+"""
        )

        private val VIDEO_ID_REGEX = Regex(
            """(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)([\w-]+)"""
        )

        fun extractVideoId(url: String): String? =
            VIDEO_ID_REGEX.find(url)?.groupValues?.get(1)

        private val SUMMARY_PROMPT = """
            |이 유튜브 영상을 한국어로 간결하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
            |
            |📋 한줄 요약
            |영상의 핵심을 한 문장으로 요약
            |
            |📌 키워드
            |#키워드1 #키워드2 #키워드3
            |
            |📖 핵심 내용
            |- 가장 중요한 내용 3~5개를 각 1줄로 정리
        """.trimMargin()

        private val WEB_SUMMARY_PROMPT = """
            |이 웹페이지 내용을 한국어로 간결하게 요약해줘. 인사말이나 부가 설명 없이 아래 형식만 정확히 출력해:
            |
            |📋 한줄 요약
            |페이지의 핵심을 한 문장으로 요약
            |
            |📌 키워드
            |#키워드1 #키워드2 #키워드3
            |
            |📖 핵심 내용
            |- 가장 중요한 내용 3~5개를 각 1줄로 정리
            |💡 핵심 인사이트
            |- 이 글에서 얻을 수 있는 주요 인사이트를 정리
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
                            e.message?.contains("503") == true || e.message?.contains("UNAVAILABLE") == true ||
                            e.message?.contains("high demand") == true ->
                                "AI 서버가 일시적으로 바빠요. 잠시 후 다시 시도해주세요."
                            e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true ->
                                "응답 시간이 초과됐어요. 더 짧은 영상을 시도해주세요."
                            else -> "요약 중 오류가 발생했어요. 다시 시도해주세요."
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
                            youtubeUrl = it.youtubeUrl
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

            // AI 사용량 체크 (일일 5회 제한)
            var dailyUsageCount by remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                dailyUsageCount = aiUsageDao.getCountSince(FEATURE_YOUTUBE_SUMMARY, startOfToday())
            }
            val canSummarize = dailyUsageCount < DAILY_FREE_LIMIT

            // 요약 상태 통합 (ACTION_SEND + 메모 내 링크 감지)
            var inlineSummaryState by remember { mutableStateOf<SummaryState>(SummaryState.Idle) }
            // ACTION_SEND 요약 결과를 inlineSummaryState에 반영
            LaunchedEffect(summaryState) {
                if (summaryState !is SummaryState.Idle) {
                    inlineSummaryState = summaryState
                }
            }

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

            var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
            var recordingStartTime by remember { mutableStateOf(0L) }
            val audioFile = remember(context) { java.io.File(context.cacheDir, "recording.m4a") }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    // 권한 획득 → 녹음 시작
                    try {
                        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            MediaRecorder(context)
                        } else {
                            @Suppress("DEPRECATION")
                            MediaRecorder()
                        }
                        recorder.apply {
                            setAudioSource(MediaRecorder.AudioSource.MIC)
                            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                            setAudioSamplingRate(16000)
                            setAudioEncodingBitRate(64000)
                            setOutputFile(audioFile.absolutePath)
                            prepare()
                            start()
                        }
                        mediaRecorder = recorder
                        recordingStartTime = System.currentTimeMillis()
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
                        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            MediaRecorder(context)
                        } else {
                            @Suppress("DEPRECATION")
                            MediaRecorder()
                        }
                        recorder.apply {
                            setAudioSource(MediaRecorder.AudioSource.MIC)
                            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                            setAudioSamplingRate(16000)
                            setAudioEncodingBitRate(64000)
                            setOutputFile(audioFile.absolutePath)
                            prepare()
                            start()
                        }
                        mediaRecorder = recorder
                        recordingStartTime = System.currentTimeMillis()
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
                    mediaRecorder?.apply { stop(); release() }
                } catch (_: Exception) { }
                val durationSeconds = (System.currentTimeMillis() - recordingStartTime) / 1000
                mediaRecorder = null
                isRecording = false

                if (!audioFile.exists() || audioFile.length() < 1024) {
                    transcriptionError = "녹음이 너무 짧아요. 다시 시도해주세요."
                    audioFile.delete()
                    return
                }

                isTranscribing = true
                scope.launch {
                    try {
                        val audioBytes = audioFile.readBytes()
                        val base64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
                        val result = aiApiService.transcribeAudio(base64, "audio/mp4", durationSeconds)
                        // Gemini가 프롬프트를 그대로 반환하는 경우 필터링
                        if (result.contains("받아쓰기") || result.contains("텍스트만 출력") || result.isBlank()) {
                            transcriptionError = "음성이 감지되지 않았어요. 다시 시도해주세요."
                            transcriptionResult = null
                            audioFile.delete()
                        } else {
                            // 오디오 파일을 영구 저장소로 이동 (제목과 동일한 파일명)
                            val audioDir = java.io.File(context.filesDir, "audio")
                            if (!audioDir.exists()) audioDir.mkdirs()
                            val now = java.text.SimpleDateFormat("yy.MM.dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                            val safeFileName = "$now 녹음".replace(":", "-").replace("/", "-")
                            val permanentFile = java.io.File(audioDir, "$safeFileName.m4a")
                            audioFile.copyTo(permanentFile, overwrite = true)
                            audioFile.delete()
                            savedAudioPath = permanentFile.absolutePath

                            transcriptionResult = result
                            transcriptionError = null
                        }
                    } catch (e: Exception) {
                        transcriptionError = "음성 변환에 실패했어요."
                        audioFile.delete()
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
                            val newId = repository.addMemo(memoWithAudio.toEntity())
                            savedMemoId = newId.toInt()
                        }
                    }
                },
                isSummarizing = inlineSummaryState is SummaryState.Loading,
                summaryResult = (inlineSummaryState as? SummaryState.Success)?.text,
                summaryError = (inlineSummaryState as? SummaryState.Error)?.message,
                youtubeTitle = youtubeTitle,
                onStartRecording = { startRecording() },
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
                onWebSummarize = { url ->
                    currentSummarizeJob = scope.launch {
                        isWebSummarizing = true
                        webSummaryError = null
                        try {
                            val content = webScrapeService.scrapeWebPage(url)
                            if (content == null) {
                                webSummaryError = "웹페이지를 불러올 수 없어요."
                            } else {
                                webPageTitle = content.title
                                val summary = retryOn503 {
                                    aiApiService.generateContent(
                                        "$WEB_SUMMARY_PROMPT\n\n아래는 웹페이지 내용입니다:\n\n${content.text}"
                                    )
                                }
                                webSummaryResult = summary
                            }
                        } catch (e: Exception) {
                            webSummaryError = when {
                                e.message?.contains("503") == true || e.message?.contains("UNAVAILABLE") == true ->
                                    "AI 서버가 일시적으로 바빠요. 잠시 후 다시 시도해주세요."
                                e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true ->
                                    "응답 시간이 초과됐어요. 더 짧은 페이지를 시도해주세요."
                                else -> "요약 중 오류가 발생했어요. 다시 시도해주세요."
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
                onYoutubeDetected = { videoId ->
                    scope.launch {
                        val title = captionService.fetchTitle(videoId)
                        if (title != null) {
                            youtubeTitle = title
                        }
                    }
                },
                onYoutubeSummarize = { url ->
                    currentSummarizeJob = scope.launch {
                        if (!canSummarize) {
                            inlineSummaryState = SummaryState.Error("오늘 무료 요약 횟수를 모두 사용했어요.")
                            return@launch
                        }
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
                            aiUsageDao.insert(AiUsage(feature = FEATURE_YOUTUBE_SUMMARY))
                            dailyUsageCount++
                        } catch (e: Exception) {
                            val errorMsg = when {
                                e.message?.contains("token count exceeds") == true ||
                                e.message?.contains("INVALID_ARGUMENT") == true ->
                                    "영상이 너무 길어 요약할 수 없어요. 약 30분 이하 영상을 시도해주세요."
                                e.message?.contains("Rate limit") == true ->
                                    "요청이 너무 많아요. 잠시 후 다시 시도해주세요."
                                e.message?.contains("503") == true || e.message?.contains("UNAVAILABLE") == true ||
                                e.message?.contains("high demand") == true ->
                                    "AI 서버가 일시적으로 바빠요. 잠시 후 다시 시도해주세요."
                                e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true ->
                                    "응답 시간이 초과됐어요. 더 짧은 영상을 시도해주세요."
                                else -> "요약 중 오류가 발생했어요. 다시 시도해주세요."
                            }
                            inlineSummaryState = SummaryState.Error(errorMsg)
                        }
                    }
                },
                onSave = { memo ->
                    scope.launch {
                        val memoWithAudio = memo.copy(audioPath = savedAudioPath ?: memo.audioPath)
                        val finalMemoId: Int
                        if (savedMemoId > 0) {
                            repository.updateMemo(memoWithAudio.copy(id = savedMemoId).toEntity())
                            finalMemoId = savedMemoId
                        } else if (memoId > 0 && existingMemo != null) {
                            repository.updateMemo(memoWithAudio.toEntity())
                            finalMemoId = memoId
                        } else {
                            finalMemoId = repository.addMemo(memoWithAudio.toEntity()).toInt()
                        }
                        // 자동 태그 부여
                        if (memoWithAudio.audioPath != null) {
                            autoTag(finalMemoId, "녹음")
                        }
                        val youtubeRegex = Regex("""(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)""")
                        if (youtubeRegex.containsMatchIn(memoWithAudio.content) || inlineSummaryState is SummaryState.Success) {
                            autoTag(finalMemoId, "유튜브")
                        }
                        if (webSummaryResult != null) {
                            autoTag(finalMemoId, "웹")
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
        content = content,
        audioPath = audioPath,
        styles = styles,
        youtubeUrl = youtubeUrl
    )

    // 503 에러 시 1회만 재시도 (5초 후)
    private suspend fun <T> retryOn503(block: suspend () -> T): T {
        try {
            return block()
        } catch (e: Exception) {
            if (e.message?.contains("503") == true || e.message?.contains("UNAVAILABLE") == true || e.message?.contains("high demand") == true) {
                kotlinx.coroutines.delay(5000)
                return block() // 1회만 재시도
            }
            throw e
        }
    }

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
        val captions = videoInfo?.captions?.take(15000)
        if (captions != null) {
            // 자막 추출 후 잠시 대기 (Gemini RPM 제한 방지)
            kotlinx.coroutines.delay(1000)
            // 자막 기반 요약 (텍스트만, 빠르고 저렴)
            return retryOn503 {
                aiApiService.generateContent(
                    "$SUMMARY_PROMPT\n\n아래는 영상의 자막입니다:\n\n$captions"
                )
            }
        }
        // 2. 자막 없으면 fallback — 영상 직접 분석
        return retryOn503 {
            aiApiService.generateContentWithVideo(
                prompt = SUMMARY_PROMPT,
                videoUrl = videoUrl,
            )
        }
    }
}

private sealed class SummaryState {
    data object Idle : SummaryState()
    data object Loading : SummaryState()
    data class Success(val text: String) : SummaryState()
    data class Error(val message: String) : SummaryState()
}
