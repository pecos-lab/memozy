package me.pecos.nota

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.input.textinput.textarea.WantedTextArea
import com.wanted.android.wanted.design.input.textinput.textfield.WantedTextField
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant

// ── 메모 카드 ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Greeting(
    memo: MemoUiState,
    onDelete: () -> Unit,
    onSave: (MemoUiState) -> Unit
) {
    val colors = LocalAppColors.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val languageCode = remember {
        context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            .getString("language_code", "ko") ?: "ko"
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditPopup by remember { mutableStateOf(false) }

    // ── 수정 팝업 ─────────────────────────────────────────────────────────────
    if (showEditPopup) {
        var editName by remember { mutableStateOf(memo.name) }
        var editBody by remember { mutableStateOf(memo.content) }
        var editCategoryIndex by remember {
            mutableIntStateOf(
                (memo.categoryId - 1).coerceIn(0, CATEGORY_RES_IDS.size - 1)
            )
        }
        val rawCategories = CATEGORY_RES_IDS.map { resId -> stringResource(resId) }
        val displayCategories = CATEGORY_RES_IDS.mapIndexed { i, resId ->
            "${CATEGORY_EMOJIS[i]} ${stringResource(resId)}"
        }
        val editEnabled = editName.isNotBlank() && editBody.isNotBlank()

        Dialog(
            onDismissRequest = { showEditPopup = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
            ) {
                // Navigation: EMPHASIZED (타이틀 좌측 + X 우측)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.edit_memo),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textTitle,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showEditPopup = false }
                    )
                }

                // Contents (스크롤 가능)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 30.dp)
                    ) {
                        WantedTextField(
                            text = editName,
                            placeholder = stringResource(R.string.memo_title_placeholder),
                            title = stringResource(R.string.memo_title_label),
                            onValueChange = { editName = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        WantedTextArea(
                            text = editBody,
                            placeholder = stringResource(R.string.memo_content_placeholder),
                            title = stringResource(R.string.memo_content_label),
                            onValueChange = { editBody = it }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        FlowRow(
                            maxItemsInEachRow = 3,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            displayCategories.forEachIndexed { index, category ->
                                val selected = editCategoryIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) colors.chipBackground else Color.Transparent)
                                        .border(1.dp, if (selected) colors.chipText else colors.cardBorder, RoundedCornerShape(12.dp))
                                        .clickable { editCategoryIndex = index }
                                        .padding(horizontal = 4.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category,
                                        maxLines = 1,
                                        fontSize = 11.sp,
                                        color = if (selected) colors.chipText else colors.textSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // 저장 버튼
                Box(modifier = Modifier.padding(horizontal = 30.dp, vertical = 16.dp)) {
                    WantedButton(
                        text = stringResource(R.string.save),
                        modifier = Modifier.fillMaxWidth(),
                        type = ButtonType.PRIMARY,
                        variant = ButtonVariant.SOLID,
                        enabled = editEnabled,
                        onClick = {
                            onSave(
                                MemoUiState(
                                    id = memo.id,
                                    name = editName,
                                    categoryId = editCategoryIndex + 1,
                                    content = editBody
                                )
                            )
                            showEditPopup = false
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AppPopup(
            onDismissRequest = { showDeleteDialog = false },
            title = stringResource(R.string.delete_confirm_title),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.yes),
            isPrimaryDestructive = true,
            onPrimaryClick = {
                showDeleteDialog = false
                onDelete()
            },
            secondaryButtonText = stringResource(R.string.no),
            onSecondaryClick = { showDeleteDialog = false }
        ) {
            Text(
                stringResource(R.string.delete_confirm_message),
                color = Color(0xFFE24B4A)
            )
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(memo.name, fontWeight = FontWeight.Bold, color = colors.textTitle)
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatMemoTime(memo.createdAt, languageCode), color = colors.textSecondary)
                    if (memo.categoryId in 1..CATEGORY_RES_IDS.size) {
                        val categoryIndex = memo.categoryId - 1
                        val categoryLabel = "${CATEGORY_EMOJIS[categoryIndex]} ${stringResource(CATEGORY_RES_IDS[categoryIndex])}"
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = categoryLabel,
                            fontSize = 11.sp,
                            color = colors.chipText,
                            modifier = Modifier
                                .background(colors.chipBackground, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            var isExpanded by remember { mutableStateOf(false) }
            var isOverflow by remember { mutableStateOf(false) }
            Text(
                text = memo.content,
                color = colors.textBody,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (!isExpanded) isOverflow = result.hasVisualOverflow
                }
            )
            if (isOverflow || isExpanded) {
                Text(
                    text = if (isExpanded) "접기" else "더보기",
                    color = colors.chipText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            val isDark = isSystemInDarkTheme()
            Row(modifier = Modifier.align(Alignment.End)) {
                // 삭제 버튼
                val deleteBg     = if (isDark) Color(0x2EFF6B4F) else Color(0x00000000)
                val deleteBorder = if (isDark) Color(0x66FF6B4F) else Color(0xFFE5735A)
                val deleteTint   = if (isDark) Color(0xFFFF6B4F) else Color(0xFFE5735A)
                val deleteBorderWidth = if (isDark) 0.5.dp else 1.5.dp
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(deleteBorderWidth, deleteBorder, RoundedCornerShape(10.dp))
                        .background(deleteBg)
                        .clickable { showDeleteDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = deleteTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 수정 버튼
                val editBg     = if (isDark) Color(0x2664B4FF) else Color(0x00000000)
                val editBorder = if (isDark) Color(0x5964B4FF) else Color(0xFF4A9EE8)
                val editTint   = if (isDark) Color(0xFF64B4FF) else Color(0xFF4A9EE8)
                val editBorderWidth = if (isDark) 0.5.dp else 1.5.dp
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(editBorderWidth, editBorder, RoundedCornerShape(10.dp))
                        .background(editBg)
                        .clickable { showEditPopup = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = editTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
