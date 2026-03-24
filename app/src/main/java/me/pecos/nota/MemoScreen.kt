package me.pecos.nota

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import com.wanted.android.wanted.design.input.textinput.textfield.WantedTextField
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant

@get:StringRes
val MemoCategoryUiState.labelResId: Int
    get() = when (this) {
        MemoCategoryUiState.GENERAL -> R.string.category_general
        MemoCategoryUiState.WORK -> R.string.category_work
        MemoCategoryUiState.IDEA -> R.string.category_idea
        MemoCategoryUiState.TODO -> R.string.category_todo
        MemoCategoryUiState.STUDY -> R.string.category_study
        MemoCategoryUiState.PERSONAL -> R.string.category_personal
        MemoCategoryUiState.SCHEDULE -> R.string.category_schedule
        MemoCategoryUiState.BUDGET -> R.string.category_budget
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoScreen(
    onSave: (MemoUiState) -> Unit,
    existingMemo: MemoUiState = MemoUiState(0, "", MemoCategoryUiState.GENERAL, "")
) {
    var nameText by remember { mutableStateOf(existingMemo.name) }
    var selectedCategory by remember { mutableStateOf(existingMemo.category) }
    var bodyText by remember { mutableStateOf(existingMemo.killThePecos) }

    val enabled = nameText.isNotBlank() && bodyText.isNotBlank()
    val colors = LocalAppColors.current

    Scaffold(containerColor = colors.screenBackground) { innerPadding ->
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
                onValueChange = { bodyText = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                MemoCategoryUiState.entries.forEach { category ->
                    val selected = selectedCategory == category
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = category },
                        label = { Text(stringResource(category.labelResId)) },
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
                            category = selectedCategory,
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
            existingMemo = MemoUiState(1, "테스트 제목", MemoCategoryUiState.WORK, "테스트 내용")
        )
    }
}
