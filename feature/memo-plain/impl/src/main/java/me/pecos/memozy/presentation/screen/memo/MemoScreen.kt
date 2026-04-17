package me.pecos.memozy.presentation.screen.memo

import me.pecos.memozy.presentation.util.decodeHtmlEntities
import me.pecos.memozy.presentation.util.htmlToPlainText
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import com.wanted.android.wanted.design.input.textinput.textfield.WantedTextField
import com.wanted.android.wanted.design.input.textinput.textarea.WantedTextArea
import androidx.compose.runtime.mutableStateListOf
import me.pecos.memozy.presentation.screen.home.model.MemoUiState
import me.pecos.memozy.presentation.screen.home.model.SummaryEntry
import me.pecos.memozy.presentation.screen.home.model.parseSummaryEntries
import me.pecos.memozy.presentation.screen.home.model.toJson
import me.pecos.memozy.feature.core.resource.CATEGORY_EMOJIS
import me.pecos.memozy.feature.core.resource.CATEGORY_RES_IDS
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.screen.memo.components.AudioPlayerBar
import me.pecos.memozy.presentation.screen.memo.components.FormattingToolbar
import me.pecos.memozy.presentation.screen.memo.components.WebLinkBottomSheet
import me.pecos.memozy.presentation.screen.memo.components.WebSummaryInlineCard
import me.pecos.memozy.presentation.screen.memo.components.WebUrlDialog
import me.pecos.memozy.presentation.screen.memo.components.SummaryStyleBottomSheet
import me.pecos.memozy.presentation.screen.memo.components.YouTubeSummaryInlineCard
import me.pecos.memozy.presentation.screen.memo.components.YouTubeLinkBottomSheet
import me.pecos.memozy.presentation.screen.memo.components.YouTubeUrlDialog
import me.pecos.memozy.presentation.screen.memo.components.AiActionMenu
import me.pecos.memozy.presentation.screen.memo.components.AiPresetAction
import me.pecos.memozy.presentation.screen.memo.components.MemoActionBar
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private val YOUTUBE_URL_REGEX = Regex(
    """(?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)[\w\-]+(?:[&?][\w\-=]*)*"""
)

private class StyleVisualTransformation(
    private val styles: List<TextSpanStyle>
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val result = buildAnnotatedString {
            append(text)
            // 사용자 서식 적용
            for (style in styles) {
                if (style.start >= text.length || style.end > text.length || style.start >= style.end) continue
                addStyle(
                    SpanStyle(
                        fontWeight = if (style.bold) FontWeight.Bold else null,
                        fontStyle = if (style.italic) FontStyle.Italic else null,
                        textDecoration = if (style.strikethrough) TextDecoration.LineThrough else null,
                        color = style.color?.let {
                            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Unspecified }
                        } ?: Color.Unspecified
                    ),
                    start = style.start,
                    end = style.end
                )
            }
            // YouTube URL 하이라이트
            YOUTUBE_URL_REGEX.findAll(text.text).forEach { match ->
                addStyle(
                    SpanStyle(color = Color(0xFF2196F3), textDecoration = TextDecoration.Underline),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
        return TransformedText(result, OffsetMapping.Identity)
    }
}

private class UrlHighlightTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = buildAnnotatedString {
            append(text)
            YOUTUBE_URL_REGEX.findAll(text.text).forEach { match ->
                addStyle(
                    SpanStyle(
                        color = Color(0xFF2196F3),
                        textDecoration = TextDecoration.Underline
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MemoScreen(
    onSave: (MemoUiState) -> Unit,
    onAutoSave: ((MemoUiState) -> Unit)? = null,
    onBack: () -> Unit = {},
    onDelete: ((Int) -> Unit)? = null,
    onYoutubeSummarize: ((url: String, mode: SummaryMode) -> Unit)? = null,
    onYoutubeDetected: ((videoId: String) -> Unit)? = null,
    isSummarizing: Boolean = false,
    summaryResult: String? = null,
    summaryError: String? = null,
    youtubeTitle: String? = null,
    onStartRecording: (() -> Unit)? = null,
    onStopRecording: (() -> Unit)? = null,
    isRecording: Boolean = false,
    isTranscribing: Boolean = false,
    transcriptionResult: String? = null,
    transcriptionError: String? = null,
    audioPath: String? = null,
    onWebSummarize: ((url: String, mode: SummaryMode) -> Unit)? = null,
    onCancelSummarize: (() -> Unit)? = null,
    isWebSummarizing: Boolean = false,
    webSummaryResult: String? = null,
    webSummaryError: String? = null,
    webPageTitle: String? = null,
    onSummaryStyleSelected: ((SummaryStyle, String) -> Unit)? = null,
    // Memozy AI — (actionName, currentTitle, currentBody)
    onAiPresetAction: ((String, String, String) -> Unit)? = null,
    onAiCustomSend: ((String, String, String) -> Unit)? = null,
    onAiCancel: (() -> Unit)? = null,
    isAiCancelled: Boolean = false,
    aiAssistStreamingText: String? = null,
    isAiAssistLoading: Boolean = false,
    existingMemo: MemoUiState = MemoUiState(0, "", 1, "")
) {
    val isNewMemo = existingMemo.id <= 0
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val categories = listOf(
        stringResource(R.string.category_general),
        stringResource(R.string.category_work),
        stringResource(R.string.category_idea),
        stringResource(R.string.category_todo),
        stringResource(R.string.category_study),
        stringResource(R.string.category_schedule),
        stringResource(R.string.category_budget),
        stringResource(R.string.category_exercise),
        stringResource(R.string.category_health),
        stringResource(R.string.category_travel),
        stringResource(R.string.category_shopping),
    )
    // 초기값만 한 번 설정, 이후 recomposition에서 덮어쓰지 않음
    var initialized by remember { mutableStateOf(false) }
    var nameText by remember { mutableStateOf(existingMemo.name) }
    var categoryIndex by remember {
        mutableStateOf((existingMemo.categoryId - 1).coerceIn(0, CATEGORY_RES_IDS.size - 1))
    }
    var bodyText by remember { mutableStateOf(existingMemo.content) }
    val richTextState = com.mohamedrejeb.richeditor.model.rememberRichTextState()
    // 초기 HTML 스냅샷 — setHtml 직후 캡처, 이후 toHtml()과 비교하여 변경 감지
    var initialHtml by remember { mutableStateOf("") }

    if (!initialized && existingMemo.id > 0 && existingMemo.name.isNotEmpty()) {
        nameText = existingMemo.name
        categoryIndex = (existingMemo.categoryId - 1).coerceIn(0, CATEGORY_RES_IDS.size - 1)
        bodyText = existingMemo.content
        initialized = true
    }
    // HTML-to-HTML 비교로 변경 감지 (setHtml 왕복에 의한 false positive 방지)
    val hasChanges = nameText != existingMemo.name
        || richTextState.toHtml() != initialHtml
        || categoryIndex != (existingMemo.categoryId - 1).coerceIn(0, CATEGORY_RES_IDS.size - 1)

    // 유튜브 URL 감지 (하단바에서도 사용)
    val detectedYoutubeUrl = remember(bodyText) {
        YOUTUBE_URL_REGEX.find(bodyText)?.value
    }

    // 유튜브 URL 입력 다이얼로그 표시
    var showYoutubeDialog by remember { mutableStateOf(false) }
    var showWebDialog by remember { mutableStateOf(false) }
    var youtubeChipDismissed by remember { mutableStateOf(false) }
    var webChipDismissed by remember { mutableStateOf(false) }
    var savedWebUrl by remember { mutableStateOf<String?>(null) }
    var selectedWebUrl by remember { mutableStateOf<String?>(null) }
    // 요약 전 원본 URL 보관 (버튼으로 추가하거나 DB에 저장된 경우만)
    var savedYoutubeUrl by remember { mutableStateOf(existingMemo.youtubeUrl) }

    // 요약 결과 표시 상태
    var showSummary by remember { mutableStateOf(false) }
    var showSummaryStyleSheet by remember { mutableStateOf(false) }
    var selectedSummaryStyle by remember { mutableStateOf(SummaryStyle.SIMPLE) }

    // 요약 콘텐츠 접기/펼치기 상태 (summaryContent 별도 컬럼에서 로드)
    val summaryEntries = remember { mutableStateListOf<SummaryEntry>() }
    LaunchedEffect(existingMemo.id) {
        summaryEntries.clear()
        summaryEntries.addAll(parseSummaryEntries(existingMemo.summaryContent))
    }
    var isSummaryExpanded by remember { mutableStateOf(if (existingMemo.summaryContent == null) true else existingMemo.isSummaryExpanded) }
    var webSummaryText by remember { mutableStateOf<String?>(null) }
    var isWebSummaryExpanded by remember { mutableStateOf(existingMemo.summaryContent == null) }

    // 요약 삭제 확인 다이얼로그
    var showSummaryDeleteDialog by remember { mutableStateOf<String?>(null) } // "youtube" or "web"

    // 요약 완료 시 별도 상태로 분리 (본문에 삽입하지 않음)
    var summaryApplied by remember { mutableStateOf(false) }
    var currentSummaryMode by remember { mutableStateOf<SummaryMode?>(null) }
    LaunchedEffect(summaryResult, isSummarizing) {
        if (summaryResult != null && !isSummarizing && !summaryApplied) {
            if (savedYoutubeUrl == null) savedYoutubeUrl = detectedYoutubeUrl
            // URL을 본문에서 제거
            val newText = YOUTUBE_URL_REGEX.replace(bodyText, "").trim()
            bodyText = newText
            richTextState.setHtml(newText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>"))
            // 요약 텍스트를 리스트에 추가 (덮어쓰지 않음)
            val mode = currentSummaryMode?.name ?: "SIMPLE"
            summaryEntries.add(0, SummaryEntry(content = summaryResult, mode = mode))
            isSummaryExpanded = true  // 새 요약 완료 시 펼침
            if (youtubeTitle != null && (nameText.isBlank() || nameText == existingMemo.name)) {
                nameText = youtubeTitle
            }
            summaryApplied = true
        }
    }

    // 웹 요약 완료 시 별도 상태로 분리
    var webSummaryApplied by remember { mutableStateOf(false) }
    LaunchedEffect(webSummaryResult, isWebSummarizing) {
        if (webSummaryResult != null && !isWebSummarizing && !webSummaryApplied) {
            webSummaryText = webSummaryResult
            isWebSummaryExpanded = true  // 새 요약 완료 시 펼침
            if (webPageTitle != null && nameText.isBlank()) {
                nameText = webPageTitle
            }
            webSummaryApplied = true
        }
    }

    // Memozy AI 스트리밍 → 본문 끝에 실시간 반영
    var aiInsertAnchorHtml by remember { mutableStateOf("") }
    var aiInsertAnchorPlain by remember { mutableStateOf("") }
    var lastStreamedText by remember { mutableStateOf("") }

    // aiAssistStreamingText 변경될 때마다 직접 반응
    LaunchedEffect(aiAssistStreamingText) {
        val streaming = aiAssistStreamingText
        if (streaming != null) {
            if (aiInsertAnchorHtml.isEmpty()) {
                // 스트리밍 시작 — 앵커 저장 (1회)
                aiInsertAnchorHtml = richTextState.toHtml()
                aiInsertAnchorPlain = richTextState.annotatedString.text
                lastStreamedText = ""
            }
            // 스트리밍 중 — bodyText 업데이트
            if (streaming.isNotBlank()) {
                lastStreamedText = streaming
                val separator = if (aiInsertAnchorPlain.isBlank()) "" else "\n\n"
                bodyText = aiInsertAnchorPlain + separator + streaming
            }
        } else if (aiInsertAnchorHtml.isNotEmpty()) {
            // 스트리밍 완료 또는 취소
            if (isAiCancelled) {
                // 취소 — 본문 원복
                richTextState.setHtml(aiInsertAnchorHtml)
                bodyText = richTextState.annotatedString.text
            } else if (lastStreamedText.isNotBlank()) {
                // 완료 — richTextState에 최종 결과 반영
                val escapedAi = lastStreamedText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
                val anchorHtml = aiInsertAnchorHtml.trimEnd()
                val finalHtml = if (anchorHtml.isBlank() || anchorHtml == "<p><br></p>") {
                    "<p>$escapedAi</p>"
                } else {
                    "$anchorHtml<p><br></p><p>$escapedAi</p>"
                }
                richTextState.setHtml(finalHtml)
                bodyText = richTextState.annotatedString.text
            }
            aiInsertAnchorHtml = ""
            aiInsertAnchorPlain = ""
            lastStreamedText = ""
        }
    }

    fun safeContent(): String {
        val editorHtml = richTextState.toHtml().trim()
        return if (editorHtml.isNotBlank() && editorHtml != "<p><br></p>") editorHtml else ""
    }
    fun safeSummaryContent(): String? {
        if (summaryEntries.isNotEmpty()) return summaryEntries.toJson()
        return webSummaryText
    }
    fun safeStyles(): String? = richTextState.toHtml().takeIf { it.isNotBlank() }

    val canAutoSave = nameText.isNotBlank() || bodyText.isNotBlank() || summaryEntries.isNotEmpty() || webSummaryText != null

    // ON_PAUSE 라이프사이클 저장 — 화면 이탈 시에만 저장 (snapshotFlow 제거)
    if (onAutoSave != null) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE && hasChanges && canAutoSave) {
                    val newContent = safeContent()
                    // content가 기존과 동일하면 불필요한 저장 스킵
                    if (newContent == existingMemo.content && nameText == existingMemo.name) return@LifecycleEventObserver
                    onAutoSave(MemoUiState(
                        id = existingMemo.id,
                        name = nameText,
                        categoryId = categoryIndex + 1,
                        content = newContent,
                        styles = safeStyles(),
                        youtubeUrl = savedYoutubeUrl,
                        summaryContent = safeSummaryContent(),
                        isSummaryExpanded = isSummaryExpanded
                    ))
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    val enabled = nameText.isNotBlank() && bodyText.isNotBlank()
    val colors = LocalAppColors.current  // ← CompositionLocal에서 현재 테마 색상 가져옴
    val fontSettings = LocalFontSettings.current

    var showAiActionMenu by remember { mutableStateOf(false) }
    var showAiCustomInput by remember { mutableStateOf(false) }

    // containerColor 명시 → MaterialTheme.colorScheme.surface 무시
    Scaffold(
        containerColor = colors.screenBackground,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        val isKeyboardVisible = WindowInsets.isImeVisible
        val hazeState = rememberHazeState()
        val isSystemDark = colors.screenBackground == Color(0xFF1C1C1E)
        val glassStyle = remember(isSystemDark) {
            HazeStyle(
                blurRadius = 24.dp,
                backgroundColor = if (isSystemDark) Color(0xFF1C1C1E).copy(alpha = 0.01f)
                    else Color.White.copy(alpha = 0.01f),
                tints = listOf(
                    HazeTint(
                        color = if (isSystemDark) Color(0xFF2C2C2E).copy(alpha = 0.35f)
                        else Color.White.copy(alpha = 0.45f)
                    )
                )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // 상단 바
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 32.dp)
                    .padding(top = 24.dp, bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.topbarTitle,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            onBack()
                        }
                )
                Spacer(modifier = Modifier.weight(1f))

                // 공유 버튼 (기존 메모 또는 요약이 있을 때)
                val hasSharableContent = !isNewMemo || summaryEntries.isNotEmpty() || webSummaryText != null
                if (hasSharableContent) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.memo_share),
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                val shareText = buildString {
                                    val ytUrl = savedYoutubeUrl ?: detectedYoutubeUrl
                                    if (ytUrl != null) {
                                        appendLine(ytUrl)
                                        appendLine()
                                    }
                                    val summary = summaryEntries.firstOrNull()?.content ?: webSummaryText
                                    if (summary != null) {
                                        append(summary)
                                    } else {
                                        if (nameText.isNotBlank()) {
                                            appendLine(nameText)
                                            appendLine()
                                        }
                                        val plainContent = safeContent().htmlToPlainText()
                                        append(plainContent)
                                    }
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, null))
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // 완료 버튼 (새 메모 + 기존 메모 모두 표시)
                Text(
                    text = stringResource(R.string.memo_done),
                    fontSize = fontSettings.scaled(16),
                    fontWeight = FontWeight.SemiBold,
                    color = colors.chipText,
                    modifier = Modifier
                        .clickable {
                            onSave(MemoUiState(id = existingMemo.id, name = nameText, categoryId = categoryIndex + 1, content = safeContent(), styles = safeStyles(), youtubeUrl = savedYoutubeUrl, summaryContent = safeSummaryContent(), isSummaryExpanded = isSummaryExpanded))
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // 본문 영역 — 탭하면 바로 편집 가능
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .hazeSource(hazeState)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                // 제목 — 개행 시 내용으로 포커스 이동
                val bodyFocusRequester = remember { FocusRequester() }

                BasicTextField(
                    value = nameText,
                    onValueChange = { newValue ->
                        if (newValue.contains('\n')) {
                            // 개행 차단, 내용 필드로 포커스 이동
                            nameText = newValue.replace("\n", "")
                            bodyFocusRequester.requestFocus()
                        } else {
                            nameText = newValue
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = fontSettings.titleSize,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTitle,
                        fontFamily = fontSettings.fontFamily,
                        lineHeight = (fontSettings.titleSize.value * 1.45f).sp,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Bottom,
                            trim = LineHeightStyle.Trim.None
                        )
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (nameText.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.memo_title_placeholder),
                                    fontSize = fontSettings.titleSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontSettings.fontFamily,
                                    color = colors.textSecondary.copy(alpha = 0.4f),
                                    lineHeight = (fontSettings.titleSize.value * 1.45f).sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // 녹음 결과를 본문에 삽입 + 제목 자동 설정
                LaunchedEffect(transcriptionResult) {
                    if (transcriptionResult != null) {
                        val current = richTextState.annotatedString.text
                        val newText = if (current.isBlank()) transcriptionResult
                        else "$current\n\n$transcriptionResult"
                        richTextState.setHtml(newText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>"))
                        bodyText = newText
            
                        // 제목이 비어있으면 자동 설정
                        if (nameText.isBlank()) {
                            val now = java.text.SimpleDateFormat("yy.MM.dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                            nameText = "$now 녹음"
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 초기 내용 로드 — 본문만 setHtml + initialHtml 캡처
                var contentInitialized by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    val editorContent = existingMemo.content.decodeHtmlEntities()
                    if (editorContent.isNotEmpty()) {
                        val html = if (editorContent.contains("<") && editorContent.contains(">")) {
                            editorContent
                        } else {
                            editorContent.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                .replace("\n", "<br>")
                        }
                        richTextState.setHtml(html)
                    }
                    // setHtml 직후 스냅샷 캡처 — 이후 toHtml()과 비교하여 변경 감지
                    initialHtml = richTextState.toHtml()
                    contentInitialized = true
                }

                // richTextState → bodyText 단순 동기화 (URL 감지/빈 체크용)
                // AI 스트리밍 중에는 동기화 스킵 (bodyText 오염 방지)
                LaunchedEffect(richTextState.annotatedString) {
                    if (!contentInitialized) return@LaunchedEffect
                    if (aiAssistStreamingText != null) return@LaunchedEffect
                    val plainText = richTextState.annotatedString.text
                    if (plainText != bodyText) {
                        bodyText = plainText
                    }
                }

                // ── 유튜브 요약 인라인 카드 ──
                val youtubeUrlDisplay = detectedYoutubeUrl ?: savedYoutubeUrl ?: ""
                val ytVideoId = remember(youtubeUrlDisplay) {
                    Regex("""(?:v=|youtu\.be/|shorts/)([a-zA-Z0-9_-]{11})""").find(youtubeUrlDisplay)?.groupValues?.get(1)
                }
                val showYoutubeInline = summaryEntries.isNotEmpty() || (youtubeUrlDisplay.isNotBlank() && !youtubeChipDismissed && (savedYoutubeUrl != null || summaryResult != null || youtubeTitle != null))
                if (showYoutubeInline) {
                    YouTubeSummaryInlineCard(
                        youtubeUrl = youtubeUrlDisplay,
                        videoId = ytVideoId,
                        title = youtubeTitle,
                        memoTitle = nameText,
                        isExpanded = isSummaryExpanded,
                        onExpandToggle = { isSummaryExpanded = it },
                        summaryEntries = summaryEntries.toList(),
                        streamingText = if (isSummarizing && summaryResult != null) summaryResult else null,
                        isSummarizing = isSummarizing,
                        currentSummaryMode = currentSummaryMode,
                        onSummarize = onYoutubeSummarize,
                        onCancelSummarize = onCancelSummarize,
                        onResummarize = { altMode ->
                            summaryApplied = false
                            currentSummaryMode = altMode
                            detectedYoutubeUrl?.let { onYoutubeSummarize?.invoke(it, altMode) }
                        },
                        onDeleteSummary = { index ->
                            if (index < summaryEntries.size) {
                                summaryEntries.removeAt(index)
                            }
                        },
                        onDeleteAllSummaries = { showSummaryDeleteDialog = "youtube" },
                        onAskAi = if (onAiCustomSend != null) { {
                            showAiCustomInput = true
                        } } else null,
                        colors = colors,
                        context = context,
                        clipboardManager = clipboardManager,
                        onStyleSelect = { showSummaryStyleSheet = true }
                    )
                }

                // 양식 선택 바텀시트
                if (showSummaryStyleSheet) {
                    SummaryStyleBottomSheet(
                        onSelect = { style ->
                            val url = detectedYoutubeUrl ?: savedYoutubeUrl ?: return@SummaryStyleBottomSheet
                            selectedSummaryStyle = style
                            currentSummaryMode = style.summaryMode
                            onSummaryStyleSelected?.invoke(style, url)
                        },
                        onDismiss = { showSummaryStyleSheet = false }
                    )
                }

                // ── 웹 요약 인라인 카드 ──
                val showWebInline = webSummaryText != null || (savedWebUrl != null && !webChipDismissed)
                if (showWebInline && savedWebUrl != null) {
                    WebSummaryInlineCard(
                        webUrl = savedWebUrl!!,
                        pageTitle = webPageTitle,
                        isExpanded = isWebSummaryExpanded,
                        onExpandToggle = { isWebSummaryExpanded = it },
                        summaryText = webSummaryText,
                        isWebSummarizing = isWebSummarizing,
                        currentSummaryMode = currentSummaryMode,
                        onSummarize = onWebSummarize,
                        onCancelSummarize = onCancelSummarize,
                        onResummarize = { altMode ->
                            webSummaryText = null
                            webSummaryApplied = false
                            currentSummaryMode = altMode
                            savedWebUrl?.let { onWebSummarize?.invoke(it, altMode) }
                        },
                        onDeleteSummary = { showSummaryDeleteDialog = "web" },
                        onAskAi = if (onAiCustomSend != null) { {
                            showAiCustomInput = true
                        } } else null,
                        colors = colors,
                        context = context,
                        clipboardManager = clipboardManager
                    )
                }

                // 내용 — YouTube 링크 감지
                var selectedYoutubeUrl by remember { mutableStateOf<String?>(null) }
                val sheetState = rememberModalBottomSheetState()

                com.mohamedrejeb.richeditor.ui.BasicRichTextEditor(
                    state = richTextState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp)
                        .focusRequester(bodyFocusRequester),
                    textStyle = TextStyle(
                        fontSize = fontSettings.bodySize,
                        lineHeight = (fontSettings.bodySize.value * 1.8f).sp,
                        color = colors.textBody,
                        fontFamily = fontSettings.fontFamily,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.None
                        )
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.heightIn(min = 150.dp)) {
                            if (richTextState.annotatedString.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.memo_content_placeholder),
                                    fontSize = fontSettings.bodySize,
                                    fontFamily = fontSettings.fontFamily,
                                    color = colors.textSecondary.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Memozy AI 응답 중 표시 (본문 내 인라인, X 없음)
                if (isAiAssistLoading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.chipBackground)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = colors.chipText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.ai_loading),
                            fontSize = fontSettings.scaled(13),
                            color = colors.chipText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // URL 감지 시 제목 가져오기
                LaunchedEffect(detectedYoutubeUrl) {
                    if (detectedYoutubeUrl != null && onYoutubeDetected != null && youtubeTitle == null) {
                        val videoIdRegex = Regex("""(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)([\w-]+)""")
                        val vid = videoIdRegex.find(detectedYoutubeUrl)?.groupValues?.get(1)
                        if (vid != null) {
                            onYoutubeDetected(vid)
                        }
                    }
                }

                // YouTube 링크 바텀시트
                if (selectedYoutubeUrl != null) {
                    YouTubeLinkBottomSheet(
                        url = selectedYoutubeUrl!!,
                        colors = colors,
                        context = context,
                        clipboardManager = clipboardManager,
                        isSummarizing = isSummarizing,
                        isWebSummarizing = isWebSummarizing,
                        summaryResult = summaryResult,
                        onSummarize = onYoutubeSummarize,
                        onDismiss = { selectedYoutubeUrl = null },
                        onSummarizeAndDismiss = { url, mode ->
                            currentSummaryMode = mode
                            onYoutubeSummarize?.invoke(url, mode)
                            selectedYoutubeUrl = null
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── 상태 메시지 영역 ──
                var transcriptionErrorDismissed by remember { mutableStateOf(false) }
                LaunchedEffect(transcriptionError) { if (transcriptionError != null) transcriptionErrorDismissed = false }
                var webErrorDismissed by remember { mutableStateOf(false) }
                LaunchedEffect(webSummaryError) { if (webSummaryError != null) webErrorDismissed = false }

                // 녹음 중
                if (isRecording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFE24B4A))
                            .clickable { onStopRecording?.invoke() }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("🔴 " + stringResource(R.string.recording_tap_to_stop), fontSize = fontSettings.scaled(13), color = Color(0xFFE24B4A), fontWeight = FontWeight.SemiBold)
                    }
                }
                // 음성 변환 중
                if (isTranscribing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.transcribing), fontSize = fontSettings.scaled(13), color = colors.textSecondary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp).clickable { onCancelSummarize?.invoke() })
                    }
                }
                // 녹음 에러
                if (transcriptionError != null && !transcriptionErrorDismissed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF3E0)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = fontSettings.scaled(16)); Spacer(modifier = Modifier.width(8.dp))
                            Text(transcriptionError, fontSize = fontSettings.scaled(13), color = Color(0xFFE65100), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { transcriptionErrorDismissed = true })
                    }
                }
                // (웹 요약 로딩은 인라인 카드 안으로 이동됨)
                // 웹 요약 에러
                if (webSummaryError != null && !webErrorDismissed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF3E0)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = fontSettings.scaled(16)); Spacer(modifier = Modifier.width(8.dp))
                            Text(webSummaryError, fontSize = fontSettings.scaled(13), color = Color(0xFFE65100), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { webErrorDismissed = true })
                    }
                }
                // (유튜브 요약 로딩은 인라인 카드 안으로 이동됨)
                // 유튜브 요약 에러
                var ytErrorDismissed by remember { mutableStateOf(false) }
                LaunchedEffect(summaryError) { if (summaryError != null) ytErrorDismissed = false }
                if (summaryError != null && !ytErrorDismissed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF3E0)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = fontSettings.scaled(16)); Spacer(modifier = Modifier.width(8.dp))
                            Text(summaryError, fontSize = fontSettings.scaled(13), color = Color(0xFFE65100), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { ytErrorDismissed = true })
                    }
                }

                // (SummaryCard는 본문 위 인라인 카드로 통합됨)

                // 오디오 재생 바 (서식 툴바 아래)
                var audioChipDismissed by remember { mutableStateOf(false) }
                val effectiveAudioPath = audioPath ?: existingMemo.audioPath
                if (effectiveAudioPath != null && java.io.File(effectiveAudioPath).exists() && !audioChipDismissed) {
                    AudioPlayerBar(
                        audioPath = effectiveAudioPath,
                        memoTitle = nameText,
                        colors = colors,
                        context = context,
                        onDismiss = { audioChipDismissed = true }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 서식 툴바 — 키보드 또는 AI 입력바 활성 시 표시
            AnimatedVisibility(
                visible = isKeyboardVisible || showAiCustomInput || isAiAssistLoading,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                val keyboardBarBorder = if (isSystemDark) Color(0xFF3A3A3C) else Color(0xFFBFC1C6)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hazeEffect(state = hazeState, style = glassStyle)
                ) {
                    HorizontalDivider(color = keyboardBarBorder, thickness = 0.5.dp)

                    // Memozy AI 직접 입력바 (툴바 위 고정)
                    if (showAiCustomInput && onAiCustomSend != null) {
                        var aiInputText by remember { mutableStateOf("") }
                        val aiInputFocusRequester = remember { FocusRequester() }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(100)
                            aiInputFocusRequester.requestFocus()
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = aiInputText,
                                onValueChange = { aiInputText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(aiInputFocusRequester)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.chipBackground.copy(alpha = 0.4f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                textStyle = TextStyle(
                                    color = colors.textBody,
                                    fontSize = fontSettings.scaled(14)
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.textTitle),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (aiInputText.isEmpty()) {
                                            Text(
                                                stringResource(R.string.ai_input_placeholder),
                                                color = colors.textBody.copy(alpha = 0.4f),
                                                fontSize = fontSettings.scaled(14)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val canSend = aiInputText.isNotBlank() && !isAiAssistLoading
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (canSend) colors.chipText else colors.textBody.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (canSend) colors.chipBackground else Color.Transparent)
                                    .clickable(enabled = canSend) {
                                        val text = aiInputText.trim()
                                        aiInputText = ""
                                        showAiCustomInput = false
                                        onAiCustomSend(text, nameText, bodyText)
                                    }
                                    .padding(4.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = colors.textBody.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { showAiCustomInput = false }
                            )
                        }
                    }

                    // 액션 버튼 (왼쪽) + 서식 툴바 (오른쪽) — 한 줄 배치
                    Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MemoActionBar(
                                    colors = colors,
                                    isNewMemo = isNewMemo,
                                    existingMemo = existingMemo,
                                    onStartRecording = onStartRecording,
                                    onStopRecording = onStopRecording,
                                    isRecording = isRecording,
                                    isTranscribing = isTranscribing,
                                    onYoutubeSummarize = onYoutubeSummarize,
                                    isSummarizing = isSummarizing,
                                    isWebSummarizing = isWebSummarizing,
                                    detectedYoutubeUrl = detectedYoutubeUrl,
                                    onYoutubeChipClick = {
                                        youtubeChipDismissed = false
                                        currentSummaryMode = SummaryMode.SIMPLE
                                        detectedYoutubeUrl?.let { onYoutubeSummarize?.invoke(it, SummaryMode.SIMPLE) }
                                    },
                                    onYoutubeDialogOpen = { showYoutubeDialog = true },
                                    onWebSummarize = onWebSummarize,
                                    onWebDialogOpen = { showWebDialog = true },
                                    onAiAssistClick = if (onAiPresetAction != null) {
                                        { showAiActionMenu = true }
                                    } else null
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                FormattingToolbar(richTextState = richTextState, colors = colors)
                            }
                        }
                    }
                }
            }
        } // outer Column

    // AI 액션 메뉴 팝업 — AnimatedVisibility 바깥에 배치 (���보드 내려가도 유지)
    if (showAiActionMenu && onAiPresetAction != null) {
        AiActionMenu(
            onPresetSelected = { action ->
                showAiActionMenu = false
                if (action == AiPresetAction.CUSTOM) {
                    showAiCustomInput = true
                } else {
                    onAiPresetAction(action.name, nameText, bodyText)
                }
            },
            onDismiss = { showAiActionMenu = false }
        )
    }

    // 웹 URL 입력 다이얼로그
    if (showWebDialog && onWebSummarize != null) {
        WebUrlDialog(
            clipboardManager = clipboardManager,
            onDismiss = { showWebDialog = false },
            onUrlConfirmed = { url ->
                savedWebUrl = url
                webChipDismissed = false
                showWebDialog = false
            }
        )
    }

    // 웹 링크 바텀시트
    if (selectedWebUrl != null) {
        WebLinkBottomSheet(
            url = selectedWebUrl!!,
            clipboardManager = clipboardManager,
            context = context,
            onDismiss = { selectedWebUrl = null }
        )
    }

    // 유튜브 URL 입력 다이얼로그
    if (showYoutubeDialog && onYoutubeSummarize != null) {
        YouTubeUrlDialog(
            colors = colors,
            clipboardManager = clipboardManager,
            context = context,
            onDismiss = { showYoutubeDialog = false },
            onUrlAdded = { url ->
                bodyText = if (bodyText.isBlank()) url else "$bodyText\n$url"
                savedYoutubeUrl = url
                showYoutubeDialog = false
                // 제목 가져오기 — 다이얼로그에서 URL 추가 시에도 즉시 호출
                val videoIdRegex = Regex("""(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)([\w-]+)""")
                val vid = videoIdRegex.find(url)?.groupValues?.get(1)
                if (vid != null) onYoutubeDetected?.invoke(vid)
            }
        )
    }

    // 요약 삭제 확인 다이얼로그
    if (showSummaryDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showSummaryDeleteDialog = null },
            title = { Text(stringResource(R.string.summary_delete_title)) },
            text = { Text(stringResource(R.string.summary_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    when (showSummaryDeleteDialog) {
                        "youtube" -> {
                            summaryEntries.clear()
                            summaryApplied = false
                        }
                        "web" -> {
                            webSummaryText = null
                            webSummaryApplied = false
                        }
                    }
                    showSummaryDeleteDialog = null
                }) {
                    Text(stringResource(R.string.summary_delete_action), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSummaryDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MemoScreenPreview() {
    DesignSystemTheme {
        MemoScreen(
            onSave = {},
            existingMemo = MemoUiState(1, "테스트 제목", 2, "테스트 내용")
        )
    }
}
