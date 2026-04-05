package me.pecos.memozy.presentation.screen.memo

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    existingMemo: MemoUiState = MemoUiState(0, "", 1, "")
) {
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
    var nameText by remember { mutableStateOf(existingMemo.name) }
    var categoryIndex by remember(existingMemo.categoryId) {
        mutableStateOf((existingMemo.categoryId - 1).coerceIn(0, CATEGORY_RES_IDS.size - 1))
    }
    var bodyText by remember { mutableStateOf(existingMemo.content) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 새 메모일 때 자동 포커스 + 키보드 팝업
    val isNewMemo = existingMemo.id <= 0
    LaunchedEffect(isNewMemo) {
        if (isNewMemo) {
            focusRequester.requestFocus()
            keyboardController?.show()
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
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
                Text(
                    text = stringResource(R.string.add_memo),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp, vertical = 16.dp)
            ) {

            Box(modifier = Modifier.focusRequester(focusRequester)) {
                WantedTextField(
                    text = nameText,
                    placeholder = stringResource(R.string.memo_title_placeholder),
                    title = stringResource(R.string.memo_title_label),
                    onValueChange = { nameText = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box {
                WantedTextArea(
                    text = bodyText,
                    placeholder = stringResource(R.string.memo_content_placeholder),
                    title = stringResource(R.string.memo_content_label),
                    onValueChange = { bodyText = it }
                )
            }
            Text(
                text = stringResource(R.string.char_count, bodyText.length),
                fontSize = 11.sp,
                color = colors.textSecondary.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 카테고리 토글 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { categoryExpanded = !categoryExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${CATEGORY_EMOJIS[categoryIndex]} ${categories[categoryIndex]}",
                    fontSize = 13.sp,
                    color = colors.chipText,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (categoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (categoryExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
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
                                    categoryExpanded = false
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

            Spacer(modifier = Modifier.height(20.dp))

            WantedButton(
                text = stringResource(R.string.save),
                modifier = Modifier.fillMaxWidth(),
                type = ButtonType.PRIMARY,
                variant = ButtonVariant.SOLID,
                enabled = enabled,
                onClick = {
                    onSave(
                        MemoUiState(
                            id = existingMemo.id,
                            name = nameText,
                            categoryId = categoryIndex + 1,
                            content = bodyText
                        )
                    )
                }
            )
            } // inner Column
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
