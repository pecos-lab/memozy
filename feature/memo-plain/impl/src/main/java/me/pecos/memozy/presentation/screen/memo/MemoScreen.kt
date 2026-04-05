package me.pecos.memozy.presentation.screen.memo

import android.content.Intent
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoScreen(
    onSave: (MemoUiState) -> Unit,
    onBack: () -> Unit = {},
    onDelete: ((Int) -> Unit)? = null,
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
    if (!initialized && existingMemo.id > 0 && existingMemo.name.isNotEmpty()) {
        nameText = existingMemo.name
        categoryIndex = (existingMemo.categoryId - 1).coerceIn(0, CATEGORY_RES_IDS.size - 1)
        bodyText = existingMemo.content
        initialized = true
    }
    val hasChanges = nameText != existingMemo.name || bodyText != existingMemo.content || categoryIndex != (existingMemo.categoryId - 1).coerceIn(0, CATEGORY_RES_IDS.size - 1)

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
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isNewMemo) {
                    Text(
                        text = stringResource(R.string.add_memo),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.topbarTitle
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // 수정 버튼 (기존 메모일 때 항상 표시)
                if (!isNewMemo) {
                    Text(
                        text = "완료",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.chipText,
                        modifier = Modifier
                            .clickable {
                                onSave(MemoUiState(id = existingMemo.id, name = nameText, categoryId = categoryIndex + 1, content = bodyText))
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 본문 영역 — 탭하면 바로 편집 가능
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 30.dp, vertical = 16.dp)
            ) {
                // 제목
                BasicTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
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

                Spacer(modifier = Modifier.height(16.dp))

                // 내용
                BasicTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = colors.textBody
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (bodyText.isEmpty()) {
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

                Spacer(modifier = Modifier.height(20.dp))

                // 카테고리 (내용 아래 배치, 탭하면 펼침)
                var showCategoryPicker by remember { mutableStateOf(false) }
                Text(
                    text = "${CATEGORY_EMOJIS[categoryIndex]} ${categories[categoryIndex]}",
                    fontSize = 12.sp,
                    color = colors.chipText,
                    modifier = Modifier
                        .background(colors.chipBackground, RoundedCornerShape(50))
                        .clickable { showCategoryPicker = !showCategoryPicker }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
                if (showCategoryPicker) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        maxItemsInEachRow = 3,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEachIndexed { index, category ->
                            val selected = categoryIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) colors.chipBackground else Color.Transparent)
                                    .border(1.dp, if (selected) colors.chipText else colors.cardBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        categoryIndex = index
                                        showCategoryPicker = false
                                    }
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${CATEGORY_EMOJIS[index]} $category",
                                    maxLines = 1,
                                    fontSize = 11.sp,
                                    color = if (selected) colors.chipText else colors.textSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // 하단 액션 바 (새 메모: 저장 버튼 / 기존 메모: 복사·공유·삭제)
            HorizontalDivider(color = colors.cardBorder)
            if (isNewMemo) {
                Box(modifier = Modifier.padding(horizontal = 30.dp, vertical = 12.dp)) {
                    WantedButton(
                        text = stringResource(R.string.save),
                        modifier = Modifier.fillMaxWidth(),
                        type = ButtonType.PRIMARY,
                        variant = ButtonVariant.SOLID,
                        enabled = enabled,
                        onClick = {
                            onSave(MemoUiState(id = existingMemo.id, name = nameText, categoryId = categoryIndex + 1, content = bodyText))
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 복사
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString("${nameText}\n\n${bodyText}"))
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.memo_copy), fontSize = 11.sp, color = colors.textSecondary)
                    }
                    // 공유
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${nameText}\n\n${bodyText}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, null))
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                        Text("공유", fontSize = 11.sp, color = colors.textSecondary)
                    }
                    // 삭제
                    if (onDelete != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onDelete(existingMemo.id) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE24B4A), modifier = Modifier.size(20.dp))
                            Text("삭제", fontSize = 11.sp, color = Color(0xFFE24B4A))
                        }
                    }
                }
            }
        } // outer Column
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
