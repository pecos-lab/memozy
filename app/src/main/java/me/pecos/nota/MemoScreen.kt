package me.pecos.nota

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

val CATEGORY_EMOJIS = listOf(
    "📝", // 일반
    "💼", // 업무
    "💡", // 아이디어
    "✅", // 할 일
    "📚", // 공부
    "📅", // 일정
    "💰", // 가계부
    "🏃", // 운동
    "🏥", // 건강
    "✈️", // 여행
    "🛒", // 쇼핑
)

val CATEGORY_RES_IDS = listOf(
    R.string.category_general,
    R.string.category_work,
    R.string.category_idea,
    R.string.category_todo,
    R.string.category_study,
    R.string.category_schedule,
    R.string.category_budget,
    R.string.category_exercise,
    R.string.category_health,
    R.string.category_travel,
    R.string.category_shopping,
)

// 저장된 카테고리 텍스트(한/영/일 모두)를 인덱스로 역매핑하기 위한 룩업 테이블
val CATEGORY_ALL_TRANSLATIONS = listOf(
    listOf("일반", "General", "一般"),
    listOf("업무", "Work", "仕事"),
    listOf("아이디어", "Idea", "アイデア"),
    listOf("할 일", "To-Do", "やること"),
    listOf("공부", "Study", "勉強"),
    listOf("일정", "Schedule", "予定"),
    listOf("가계부", "Budget", "家計簿"),
    listOf("운동", "Exercise", "運動"),
    listOf("건강", "Health", "健康"),
    listOf("여행", "Travel", "旅行"),
    listOf("쇼핑", "Shopping", "ショッピング"),
)

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

            WantedTextField(
                text = nameText,
                placeholder = stringResource(R.string.memo_title_placeholder),
                title = stringResource(R.string.memo_title_label),
                onValueChange = { nameText = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            WantedTextArea(
                text = bodyText,
                placeholder = stringResource(R.string.memo_content_placeholder),
                title = stringResource(R.string.memo_content_label),
                onValueChange = { bodyText = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                            .clickable { categoryIndex = index }
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
                // 11개 카테고리 / 3열 → 마지막 줄 2개만 남으므로 더미 1개로 크기 통일
                Spacer(modifier = Modifier.weight(1f))
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
