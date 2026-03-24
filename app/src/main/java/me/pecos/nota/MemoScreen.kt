package me.pecos.nota

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import com.wanted.android.wanted.design.input.textinput.textfield.WantedTextField
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant

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

val CATEGORY_EMOJIS = listOf("📝", "💼", "💡", "✅", "📚", "📅", "💰", "🏃", "🏥", "✈️", "🛒")

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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MemoScreen(
    onSave: (MemoUiState) -> Unit,
    onBack: () -> Unit = {},
    existingMemo: MemoUiState = MemoUiState(0, "", "", "")
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
    var categoryIndex by remember {
        mutableStateOf(
            existingMemo.sex.let { saved ->
                CATEGORY_ALL_TRANSLATIONS.indexOfFirst { saved in it }.takeIf { it >= 0 } ?: 0
            }
        )
    }
    var bodyText by remember { mutableStateOf(existingMemo.killThePecos) }

    val enabled = nameText.isNotBlank() && bodyText.isNotBlank()
    val colors = LocalAppColors.current  // ← CompositionLocal에서 현재 테마 색상 가져옴

    // containerColor 명시 → MaterialTheme.colorScheme.surface 무시
    Scaffold(
        containerColor = colors.screenBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_memo), color = colors.topbarTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colors.topbarTitle
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.screenBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 16.dp)
                .padding(innerPadding)
        ) {

            WantedTextField(
                text = nameText,
                placeholder = stringResource(R.string.memo_title_placeholder),
                title = stringResource(R.string.memo_title_label),
                onValueChange = { nameText = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            WantedTextField(
                text = bodyText,
                placeholder = stringResource(R.string.memo_content_placeholder),
                title = stringResource(R.string.memo_content_label),
                minLines = 4,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                onValueChange = { bodyText = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    val selected = categoryIndex == index
                    FilterChip(
                        selected = selected,
                        onClick = { categoryIndex = index },
                        label = { Text("${CATEGORY_EMOJIS[index]} $category") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.chipText,
                            selectedLabelColor = colors.screenBackground,
                            containerColor = colors.cardBackground,
                            labelColor = colors.textBody
                        ),
                        border = if (selected) {
                            BorderStroke(1.dp, colors.chipText)
                        } else {
                            BorderStroke(1.dp, colors.cardBorder)
                        }
                    )
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
                            sex = categories[categoryIndex],
                            killThePecos = bodyText
                        )
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemoScreenPreview() {
    DesignSystemTheme {
        MemoScreen(
            onSave = {},
            existingMemo = MemoUiState(1, "테스트 제목", "업무", "테스트 내용")
        )
    }
}
