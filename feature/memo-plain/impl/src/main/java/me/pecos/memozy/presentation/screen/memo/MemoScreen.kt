package me.pecos.memozy.presentation.screen.memo

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
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
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant
import me.pecos.memozy.presentation.screen.home.model.MemoUiState
import me.pecos.memozy.feature.core.resource.CATEGORY_EMOJIS
import me.pecos.memozy.feature.core.resource.CATEGORY_RES_IDS
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.theme.LocalAppColors

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
    onYoutubeSummarize: ((url: String) -> Unit)? = null,
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
    onWebSummarize: ((url: String) -> Unit)? = null,
    onCancelSummarize: (() -> Unit)? = null,
    isWebSummarizing: Boolean = false,
    webSummaryResult: String? = null,
    webSummaryError: String? = null,
    webPageTitle: String? = null,
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
    // 요약 전 원본 URL 보관 (요약 후 본문에서 URL이 대체되어도 유지)
    var savedYoutubeUrl by remember { mutableStateOf(detectedYoutubeUrl ?: existingMemo.youtubeUrl) }
    if (detectedYoutubeUrl != null && savedYoutubeUrl == null) {
        savedYoutubeUrl = detectedYoutubeUrl
    }

    // 요약 결과 표시 상태
    var showSummary by remember { mutableStateOf(false) }

    // 요약 완료 시 메모 본문의 URL을 요약 텍스트로 대체 + 제목 설정
    var summaryApplied by remember { mutableStateOf(false) }
    LaunchedEffect(summaryResult, isSummarizing) {
        if (summaryResult != null && !isSummarizing && !summaryApplied) {
            // 요약 전 URL 저장
            if (savedYoutubeUrl == null) savedYoutubeUrl = detectedYoutubeUrl
            val newText = YOUTUBE_URL_REGEX.replace(bodyText) { summaryResult }.trim()
            bodyText = newText
            richTextState.setHtml(newText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>"))
            if (youtubeTitle != null && nameText.isBlank()) {
                nameText = youtubeTitle
            }
            summaryApplied = true

        }
    }

    // 웹 요약 완료 시 본문에 삽입
    var webSummaryApplied by remember { mutableStateOf(false) }
    LaunchedEffect(webSummaryResult, isWebSummarizing) {
        if (webSummaryResult != null && !isWebSummarizing && !webSummaryApplied) {
            val newText = if (bodyText.isBlank()) webSummaryResult else "$bodyText\n\n$webSummaryResult"
            bodyText = newText
            richTextState.setHtml(newText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>"))
            if (webPageTitle != null && nameText.isBlank()) {
                nameText = webPageTitle
            }
            webSummaryApplied = true

        }
    }


    fun safeContent(): String = richTextState.toHtml().ifBlank { bodyText }
    fun safeStyles(): String? = richTextState.toHtml().takeIf { it.isNotBlank() }

    val canAutoSave = nameText.isNotBlank() || bodyText.isNotBlank()

    // ON_PAUSE 라이프사이클 저장 — 화면 이탈 시에만 저장 (snapshotFlow 제거)
    if (onAutoSave != null) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE && hasChanges && canAutoSave) {
                    val html = richTextState.toHtml()
                    onAutoSave(MemoUiState(
                        id = existingMemo.id,
                        name = nameText,
                        categoryId = categoryIndex + 1,
                        content = html.ifBlank { bodyText },
                        styles = html.takeIf { it.isNotBlank() },
                        youtubeUrl = savedYoutubeUrl
                    ))
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    val enabled = nameText.isNotBlank() && bodyText.isNotBlank()
    val colors = LocalAppColors.current  // ← CompositionLocal에서 현재 테마 색상 가져옴

    // containerColor 명시 → MaterialTheme.colorScheme.surface 무시
    Scaffold(
        containerColor = colors.screenBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 상단 바
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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

                // 공유 버튼 (기존 메모만 표시)
                if (!isNewMemo) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.memo_share),
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                val shareText = buildString {
                                    if (nameText.isNotBlank()) {
                                        appendLine(nameText)
                                        appendLine()
                                    }
                                    append(safeContent())
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, nameText)
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, null))
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // 마크다운 내보내기
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(R.string.memo_export_md),
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                val mdContent = buildString {
                                    if (nameText.isNotBlank()) {
                                        appendLine("# $nameText")
                                        appendLine()
                                    }
                                    append(safeContent())
                                }
                                val fileName = (nameText.ifBlank { "memo" })
                                    .replace(Regex("[^a-zA-Z0-9가-힣ぁ-ヶ一-龠\\s_-]"), "")
                                    .take(50)
                                    .trim()
                                val file = java.io.File(context.cacheDir, "${fileName}.md")
                                file.writeText(mdContent)
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context, "${context.packageName}.fileprovider", file
                                )
                                val exportIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/markdown"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(exportIntent, null))
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // 완료 버튼 (새 메모 + 기존 메모 모두 표시)
                Text(
                    text = "완료",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.chipText,
                    modifier = Modifier
                        .clickable {
                            onSave(MemoUiState(id = existingMemo.id, name = nameText, categoryId = categoryIndex + 1, content = safeContent(), styles = safeStyles(), youtubeUrl = savedYoutubeUrl))
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // 본문 영역 — 탭하면 바로 편집 가능
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 30.dp, vertical = 16.dp)
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTitle
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (nameText.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.memo_title_placeholder),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary.copy(alpha = 0.4f)
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

                // 초기 내용 로드 — HTML로 변환 후 setHtml + initialHtml 캡처
                var contentInitialized by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    val content = existingMemo.content
                    if (content.isNotEmpty()) {
                        val html = if (content.contains("<") && content.contains(">")) {
                            content
                        } else {
                            content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                .replace("\n", "<br>")
                        }
                        richTextState.setHtml(html)
                    }
                    // setHtml 직후 스냅샷 캡처 — 이후 toHtml()과 비교하여 변경 감지
                    initialHtml = richTextState.toHtml()
                    contentInitialized = true
                }

                // richTextState → bodyText 단순 동기화 (URL 감지/빈 체크용)
                LaunchedEffect(richTextState.annotatedString) {
                    if (!contentInitialized) return@LaunchedEffect
                    val plainText = richTextState.annotatedString.text
                    if (plainText != bodyText) {
                        bodyText = plainText
                    }
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
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = colors.textBody,
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
                                    fontSize = 15.sp,
                                    color = colors.textSecondary.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

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
                    ModalBottomSheet(
                        onDismissRequest = { selectedYoutubeUrl = null },
                        sheetState = sheetState,
                        containerColor = colors.cardBackground
                    ) {
                        val url = selectedYoutubeUrl!!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .padding(bottom = 24.dp)
                        ) {
                            Text(
                                text = "YouTube 링크",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTitle
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = url,
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // 📋 링크 복사
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(url))
                                        selectedYoutubeUrl = null
                                    }
                                    .padding(vertical = 14.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📋", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("링크 복사", fontSize = 16.sp, color = colors.textBody)
                            }

                            // 🌐 브라우저에서 열기
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val fullUrl = if (url.startsWith("http")) url else "https://$url"
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                                        selectedYoutubeUrl = null
                                    }
                                    .padding(vertical = 14.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌐", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("브라우저에서 열기", fontSize = 16.sp, color = colors.textBody)
                            }

                            // ▶ YouTube에서 열기
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val fullUrl = if (url.startsWith("http")) url else "https://$url"
                                        val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)).apply {
                                            setPackage("com.google.android.youtube")
                                        }
                                        try {
                                            context.startActivity(ytIntent)
                                        } catch (_: Exception) {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                                        }
                                        selectedYoutubeUrl = null
                                    }
                                    .padding(vertical = 14.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("▶️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("YouTube에서 열기", fontSize = 16.sp, color = colors.textBody)
                            }

                            // 🤖 AI 요약하기 (1회만 가능)
                            if (onYoutubeSummarize != null && !isSummarizing && summaryResult == null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onYoutubeSummarize(url)
                                            selectedYoutubeUrl = null
                                        }
                                        .padding(vertical = 14.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🤖", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("AI 요약하기", fontSize = 16.sp, color = colors.textBody)
                                }
                            }
                        }
                    }
                }

                // 서식 툴바 (본문 아래)
                var showColorPicker by remember { mutableStateOf(false) }
                var savedColorSelection by remember { mutableStateOf(androidx.compose.ui.text.TextRange.Zero) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeBg = colors.chipBackground
                    val activeTint = colors.chipText

                    val isBold = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (isBold) activeBg else activeBg.copy(alpha = 0.4f))
                            .pointerInput(Unit) { detectTapGestures(onPress = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)); tryAwaitRelease() }) },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.FormatBold, contentDescription = null, tint = activeTint, modifier = Modifier.size(20.dp)) }

                    val isItalic = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (isItalic) activeBg else activeBg.copy(alpha = 0.4f))
                            .pointerInput(Unit) { detectTapGestures(onPress = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)); tryAwaitRelease() }) },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.FormatItalic, contentDescription = null, tint = activeTint, modifier = Modifier.size(20.dp)) }

                    val isStrike = richTextState.currentSpanStyle.textDecoration == TextDecoration.LineThrough
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (isStrike) activeBg else activeBg.copy(alpha = 0.4f))
                            .pointerInput(Unit) { detectTapGestures(onPress = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)); tryAwaitRelease() }) },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.FormatStrikethrough, contentDescription = null, tint = activeTint, modifier = Modifier.size(20.dp)) }

                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (showColorPicker) activeBg else activeBg.copy(alpha = 0.4f))
                            .pointerInput(Unit) { detectTapGestures(onPress = { savedColorSelection = richTextState.selection; showColorPicker = !showColorPicker; tryAwaitRelease() }) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("A", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = activeTint)
                            Box(modifier = Modifier.width(16.dp).height(3.dp).background(Color(0xFFFF0000), RoundedCornerShape(1.dp)))
                        }
                    }
                }

                if (showColorPicker) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("#000000", "#FF0000", "#FF9800", "#FFEB3B", "#4CAF50", "#2196F3", "#9C27B0", "#795548", "#607D8B").forEach { hex ->
                            Box(
                                modifier = Modifier.size(22.dp).clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .pointerInput(hex) {
                                        detectTapGestures(onPress = {
                                            if (savedColorSelection.start != savedColorSelection.end) {
                                                richTextState.addSpanStyle(SpanStyle(color = Color(android.graphics.Color.parseColor(hex))), savedColorSelection)
                                    
                                            }
                                            showColorPicker = false; tryAwaitRelease()
                                        })
                                    }
                            )
                        }
                    }
                }

                // ── 상태 메시지 영역 (서식 툴바 아래, 칩 위) ──
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
                        Text("🔴 녹음 중... 탭하여 중지", fontSize = 13.sp, color = Color(0xFFE24B4A), fontWeight = FontWeight.SemiBold)
                    }
                }
                // 음성 변환 중
                if (isTranscribing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("음성 변환 중...", fontSize = 13.sp, color = colors.textSecondary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp).clickable { onCancelSummarize?.invoke() })
                    }
                }
                // 녹음 에러
                if (transcriptionError != null && !transcriptionErrorDismissed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF3E0)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 16.sp); Spacer(modifier = Modifier.width(8.dp))
                            Text(transcriptionError, fontSize = 13.sp, color = Color(0xFFE65100), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { transcriptionErrorDismissed = true })
                    }
                }
                // 웹 요약 중
                if (isWebSummarizing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("웹페이지 요약 중...", fontSize = 13.sp, color = colors.textSecondary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp).clickable { onCancelSummarize?.invoke() })
                    }
                }
                // 웹 요약 에러
                if (webSummaryError != null && !webErrorDismissed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF3E0)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 16.sp); Spacer(modifier = Modifier.width(8.dp))
                            Text(webSummaryError, fontSize = 13.sp, color = Color(0xFFE65100), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { webErrorDismissed = true })
                    }
                }
                // 유튜브 요약 중
                if (isSummarizing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("유튜브 요약 중...", fontSize = 13.sp, color = colors.textSecondary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp).clickable { onCancelSummarize?.invoke() })
                    }
                }
                // 유튜브 요약 에러
                var ytErrorDismissed by remember { mutableStateOf(false) }
                LaunchedEffect(summaryError) { if (summaryError != null) ytErrorDismissed = false }
                if (summaryError != null && !ytErrorDismissed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF3E0)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 16.sp); Spacer(modifier = Modifier.width(8.dp))
                            Text(summaryError, fontSize = 13.sp, color = Color(0xFFE65100), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { ytErrorDismissed = true })
                    }
                }

                // 유튜브 칩 (서식 툴바 아래)
                val youtubeUrlDisplay = detectedYoutubeUrl ?: savedYoutubeUrl ?: ""
                val hasYoutubeSummaryContent = bodyText.contains("핵심 키워드") || bodyText.contains("한줄 요약") || bodyText.contains("타임라인별")
                if ((detectedYoutubeUrl != null || summaryResult != null || youtubeTitle != null || hasYoutubeSummaryContent) && !youtubeChipDismissed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(colors.chipBackground.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "▶", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val chipTitle = youtubeTitle ?: nameText.takeIf { it.isNotBlank() }
                                    if (chipTitle != null) {
                                        Text(chipTitle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textBody, maxLines = 2)
                                    }
                                    if (youtubeUrlDisplay.isNotBlank()) {
                                        Text(youtubeUrlDisplay, fontSize = 11.sp, color = Color(0xFF2196F3), maxLines = 1,
                                            style = TextStyle(textDecoration = TextDecoration.Underline),
                                            modifier = Modifier.clickable { selectedYoutubeUrl = youtubeUrlDisplay })
                                    } else if (chipTitle == null) {
                                        Text("📺 유튜브 요약", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textBody)
                                    }
                                }
                                if (onYoutubeSummarize != null && !isSummarizing && summaryResult == null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("🤖 요약", fontSize = 12.sp, color = colors.chipText, fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(colors.chipBackground)
                                            .clickable { detectedYoutubeUrl?.let { onYoutubeSummarize(it) } }
                                            .padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                        // X 삭제 버튼
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { youtubeChipDismissed = true; showSummary = false })
                    }
                }

                // 웹 요약 칩 (유튜브 칩 아래)
                if ((webSummaryResult != null || savedWebUrl != null) && !webChipDismissed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(colors.chipBackground.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔗", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val webTitle = webPageTitle ?: nameText.takeIf { it.isNotBlank() }
                                if (webTitle != null) {
                                    Text(webTitle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textBody, maxLines = 2)
                                }
                                if (savedWebUrl != null) {
                                    Text(savedWebUrl!!, fontSize = 11.sp, color = Color(0xFF2196F3), maxLines = 1,
                                        style = TextStyle(textDecoration = TextDecoration.Underline),
                                        modifier = Modifier.clickable { selectedWebUrl = savedWebUrl })
                                } else if (webTitle == null) {
                                    Text("🔗 웹페이지 요약", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textBody)
                                }
                            }
                        }
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { webChipDismissed = true })
                    }
                }

                // 오디오 재생 바 (서식 툴바 아래)
                var audioChipDismissed by remember { mutableStateOf(false) }
                val effectiveAudioPath = audioPath ?: existingMemo.audioPath
                if (effectiveAudioPath != null && java.io.File(effectiveAudioPath).exists() && !audioChipDismissed) {
                    var isPlaying by remember { mutableStateOf(false) }
                    val mediaPlayer = remember {
                        android.media.MediaPlayer().apply {
                            setDataSource(effectiveAudioPath)
                            prepare()
                            setOnCompletionListener { isPlaying = false }
                        }
                    }
                    DisposableEffect(Unit) { onDispose { mediaPlayer.release() } }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(colors.chipBackground.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(colors.chipText)
                                .clickable { if (isPlaying) { mediaPlayer.pause(); isPlaying = false } else { mediaPlayer.start(); isPlaying = true } },
                            contentAlignment = Alignment.Center
                        ) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("🎙️ ${nameText.ifBlank { "녹음 파일" }}", fontSize = 14.sp, color = colors.textBody, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
                        Icon(Icons.Default.Download, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp).clickable {
                                val source = java.io.File(effectiveAudioPath); val dest = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                                val target = java.io.File(dest, "memozy_${System.currentTimeMillis()}.m4a"); source.copyTo(target, overwrite = true)
                                android.widget.Toast.makeText(context, "📁 내 파일 > Download 에 저장됨", android.widget.Toast.LENGTH_LONG).show()
                            })
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Share, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp).clickable {
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", java.io.File(effectiveAudioPath))
                                val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "audio/mp4"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                                context.startActivity(Intent.createChooser(shareIntent, null))
                            })
                    }
                    Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { audioChipDismissed = true })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 하단 AI 액션 바 — 항상 표시
            HorizontalDivider(color = colors.cardBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🎙️ 녹음
                if (onStartRecording != null) {
                    val recordingBusy = isRecording || isTranscribing
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isRecording) Color(0xFFE24B4A)
                                else if (isTranscribing) colors.chipBackground.copy(alpha = 0.3f)
                                else colors.chipBackground
                            )
                            .clickable(enabled = !isTranscribing) {
                                if (isRecording) onStopRecording?.invoke()
                                else onStartRecording()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isRecording) Color.White else colors.chipText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRecording) "중지" else "녹음",
                            fontSize = 12.sp,
                            color = if (isRecording) Color.White else colors.chipText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 📺 유튜브 요약 (항상 표시)
                if (onYoutubeSummarize != null) {
                    val hasYoutubeUrl = detectedYoutubeUrl != null
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSummarizing) colors.chipBackground.copy(alpha = 0.3f)
                                else if (hasYoutubeUrl) Color(0xFF2196F3).copy(alpha = 0.1f)
                                else colors.chipBackground
                            )
                            .clickable(enabled = !isSummarizing) {
                                youtubeChipDismissed = false
                                if (hasYoutubeUrl) {
                                    onYoutubeSummarize(detectedYoutubeUrl ?: return@clickable)
                                } else {
                                    showYoutubeDialog = true
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = null,
                            tint = if (hasYoutubeUrl) Color(0xFF2196F3) else colors.chipText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "유튜브",
                            fontSize = 12.sp,
                            color = if (hasYoutubeUrl) Color(0xFF2196F3) else colors.chipText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 🔗 웹 요약
                if (onWebSummarize != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isWebSummarizing) colors.chipBackground.copy(alpha = 0.3f)
                                else colors.chipBackground
                            )
                            .clickable(enabled = !isWebSummarizing) { showWebDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = colors.chipText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("링크", fontSize = 12.sp, color = colors.chipText, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        } // outer Column
    }

    // 웹 URL 입력 다이얼로그
    if (showWebDialog && onWebSummarize != null) {
        var webUrlInput by remember { mutableStateOf("") }
        val clipText = clipboardManager.getText()?.text ?: ""

        AlertDialog(
            onDismissRequest = { showWebDialog = false },
            title = { Text("웹페이지 요약", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("요약할 웹페이지 URL을 입력해주세요", fontSize = 14.sp, color = LocalAppColors.current.textSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = webUrlInput,
                        onValueChange = { webUrlInput = it },
                        placeholder = { Text("https://...", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (clipText.isNotBlank() && webUrlInput.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📋 붙여넣기: $clipText",
                            fontSize = 13.sp,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { webUrlInput = clipText }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                val isValid = webUrlInput.startsWith("http")
                TextButton(
                    onClick = {
                        if (isValid) {
                            savedWebUrl = webUrlInput.trim()
                            webChipDismissed = false
                            onWebSummarize(webUrlInput.trim())
                            showWebDialog = false
                        }
                    },
                    enabled = isValid
                ) { Text("요약하기", color = if (isValid) Color(0xFF2196F3) else LocalAppColors.current.textSecondary) }
            },
            dismissButton = {
                TextButton(onClick = { showWebDialog = false }) { Text("취소", color = LocalAppColors.current.textSecondary) }
            }
        )
    }

    // 웹 링크 바텀시트
    if (selectedWebUrl != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedWebUrl = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = LocalAppColors.current.cardBackground
        ) {
            val url = selectedWebUrl!!
            val sheetColors = LocalAppColors.current
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).padding(bottom = 24.dp)
            ) {
                Text("웹페이지 링크", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = sheetColors.textTitle)
                Spacer(modifier = Modifier.height(4.dp))
                Text(url, fontSize = 13.sp, color = sheetColors.textSecondary, maxLines = 2)
                Spacer(modifier = Modifier.height(20.dp))

                // 📋 링크 복사
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { clipboardManager.setText(AnnotatedString(url)); selectedWebUrl = null }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📋", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                    Text("링크 복사", fontSize = 16.sp, color = sheetColors.textBody)
                }

                // 🌐 브라우저에서 열기
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val fullUrl = if (url.startsWith("http")) url else "https://$url"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                            selectedWebUrl = null
                        }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌐", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                    Text("브라우저에서 열기", fontSize = 16.sp, color = sheetColors.textBody)
                }
            }
        }
    }

    // 유튜브 URL 입력 다이얼로그
    if (showYoutubeDialog && onYoutubeSummarize != null) {
        var urlInput by remember { mutableStateOf("") }
        val clipText = clipboardManager.getText()?.text ?: ""
        val clipHasYoutube = remember(clipText) { YOUTUBE_URL_REGEX.containsMatchIn(clipText) }

        AlertDialog(
            onDismissRequest = { showYoutubeDialog = false },
            title = { Text("유튜브 영상 요약", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "요약할 YouTube URL을 입력해주세요",
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("https://youtu.be/...", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (clipText.isNotBlank() && urlInput.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📋 붙여넣기: $clipText",
                            fontSize = 13.sp,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    urlInput = clipText
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                val isValid = YOUTUBE_URL_REGEX.containsMatchIn(urlInput)
                TextButton(
                    onClick = {
                        val url = YOUTUBE_URL_REGEX.find(urlInput)?.value ?: return@TextButton
                        bodyText = if (bodyText.isBlank()) url else "$bodyText\n$url"
                        showYoutubeDialog = false
                    },
                    enabled = isValid
                ) {
                    Text("요약하기", color = if (isValid) Color(0xFF2196F3) else colors.textSecondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showYoutubeDialog = false }) {
                    Text("취소", color = colors.textSecondary)
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
